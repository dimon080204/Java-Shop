<script setup>
import { ref, onMounted } from 'vue';
import api from '../api';

const employees = ref([]);
const loading = ref(true);
const error = ref(null);

const viewMode = ref('list'); // list, create, edit, details
const currentEmployee = ref(null);

const form = ref({
  id: null,
  firstName: '',
  lastName: '',
  surname: '',
  phone: '',
  email: ''
});

const fetchEmployees = async () => {
  try {
    loading.value = true;
    const response = await api.get('/staff');
    employees.value = response.data;
  } catch (err) {
    error.value = "Ошибка загрузки списка персонала.";
  } finally {
    loading.value = false;
  }
};

onMounted(fetchEmployees);

const deleteEmployee = async (id) => {
  if (!confirm("Удалить сотрудника из базы?")) return;
  try {
    await api.delete(`/staff/${id}`);
    employees.value = employees.value.filter(e => e.id !== id);
  } catch (err) {
    alert("Ошибка при удалении.");
  }
};

const openCreate = () => {
  form.value = { id: null, firstName: '', lastName: '', surname: '', phone: '', email: '' };
  viewMode.value = 'create';
};

const openEdit = (emp) => {
  // Заполняем форму, включая отчество (оно придет из полной модели объекта)
  form.value = { ...emp };
  viewMode.value = 'edit';
};

const openDetails = (emp) => {
  currentEmployee.value = emp;
  viewMode.value = 'details';
};

const saveEmployee = async () => {
  const payload = {
    firstName: form.value.firstName,
    lastName: form.value.lastName,
    surname: form.value.surname,
    phone: form.value.phone,
    email: form.value.email
  };

  try {
    if (viewMode.value === 'create') {
      await api.post('/staff', payload);
    } else {
      await api.put(`/staff/${form.value.id}`, payload);
    }
    fetchEmployees();
    viewMode.value = 'list';
  } catch (err) {
    alert("Ошибка при сохранении данных сотрудника.");
  }
};
</script>

<template>
  <div class="page-wrapper">
    <div class="container">

      <div v-if="viewMode === 'list'">
        <div class="header-section">
          <h1>👥 Управление персоналом</h1>
          <div class="header-actions">
            <button @click="openCreate" class="btn-add-main">+ Нанять сотрудника</button>
            <button @click="fetchEmployees" class="btn-refresh" title="Обновить">🔄</button>
          </div>
        </div>

        <div v-if="loading" class="status">Загрузка данных...</div>
        <div v-else class="table-wrapper">
          <table class="styled-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Имя</th>
                <th>Фамилия</th>
                <th>Телефон</th>
                <th>Email</th>
                <th style="text-align: center">Действия</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="emp in employees" :key="emp.id">
                <td class="sku-cell">{{ emp.id }}</td>
                <td class="name-cell">{{ emp.firstName }}</td>
                <td class="name-cell">{{ emp.lastName }}</td>
                <td>{{ emp.phone }}</td>
                <td class="email-text">{{ emp.email }}</td>
                <td class="actions">
                  <button @click="openDetails(emp)" class="btn-icon btn-info" title="Подробнее">👁️</button>
                  <button @click="openEdit(emp)" class="btn-icon btn-edit" title="Править">✏️</button>
                  <button @click="deleteEmployee(emp.id)" class="btn-icon btn-delete" title="Удалить">🗑️</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="!loading && employees.length === 0" class="status">Список сотрудников пуст</div>
      </div>

      <div v-if="viewMode === 'create' || viewMode === 'edit'" class="centered-content">
        <div class="form-card">
          <h2 class="form-title">{{ viewMode === 'create' ? '✨ Регистрация сотрудника' : '✏️ Изменение данных' }}</h2>
          <div class="form-grid single-col">
            <div class="input-group"><label>Фамилия</label><input v-model="form.lastName" placeholder="Иванов" /></div>
            <div class="input-group"><label>Имя</label><input v-model="form.firstName" placeholder="Иван" /></div>
            <div class="input-group"><label>Отчество</label><input v-model="form.surname" placeholder="Иванович" /></div>
            <div class="input-group"><label>Телефон</label><input v-model="form.phone" placeholder="+380..." /></div>
            <div class="input-group"><label>Email</label><input v-model="form.email" placeholder="ivanov@example.com" /></div>
          </div>

          <div class="form-footer">
            <button @click="saveEmployee" class="btn-save">Сохранить</button>
            <button @click="viewMode = 'list'" class="btn-cancel">Отмена</button>
          </div>
        </div>
      </div>

      <div v-if="viewMode === 'details'" class="centered-content">
        <div class="details-card">
          <div class="details-header">
            <button @click="viewMode = 'list'" class="btn-back">← К списку</button>
            <h2>Карточка сотрудника</h2>
          </div>

          <div class="details-content single-view">
            <div class="info-item">
              <span class="info-key">Полное ФИО:</span>
              <span class="info-val">{{ currentEmployee.lastName }} {{ currentEmployee.firstName }} {{ currentEmployee.surname || '' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">Телефон:</span>
              <span class="info-val">{{ currentEmployee.phone }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">Email:</span>
              <span class="info-val email-text">{{ currentEmployee.email }}</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* Используем ту же базу стилей для единообразия */
.page-wrapper { margin-top: 20px; padding: 0 20px; }
.container { max-width: 1200px; margin: 0 auto; }

.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; border-bottom: 1px solid #444; padding-bottom: 15px; }
.btn-add-main { background: #42b983; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.btn-refresh { background: #333 !important; border: 1px solid #444 !important; color: #ccc !important; border-radius: 6px; padding: 5px 12px; cursor: pointer; }

/* Таблица */
.table-wrapper { background: #1a1a1a; border-radius: 8px; border: 1px solid #333; overflow: hidden; }
.styled-table { width: 100%; border-collapse: collapse; }
.styled-table th { background: #252525; color: #42b983; padding: 14px; text-align: left; border-bottom: 2px solid #333; }
.styled-table td { padding: 12px; border-bottom: 1px solid #2a2a2a; color: #ccc; }
.sku-cell { font-family: monospace; color: #888; }
.name-cell { font-weight: bold; color: #fff; }
.email-text { color: #3498db; }

/* Кнопки */
.actions { display: flex; gap: 8px; justify-content: center; }
.btn-icon { border: none; width: 32px; height: 32px; border-radius: 4px; cursor: pointer; color: white; }
.btn-info { background: #2980b9; }
.btn-edit { background: #f39c12; }
.btn-delete { background: #c0392b; }

/* Формы */
.centered-content { display: flex; justify-content: center; width: 100%; }
.form-card, .details-card { background: #1e1e1e; padding: 30px; border-radius: 10px; border: 1px solid #333; width: 100%; max-width: 600px; }
.form-title { color: #42b983; margin-top: 0; margin-bottom: 20px; }
.single-col { display: flex; flex-direction: column; gap: 15px; }
.input-group { display: flex; flex-direction: column; gap: 5px; }
.input-group label { font-size: 0.8rem; color: #888; }
.input-group input { padding: 12px; background: #111; border: 1px solid #444; color: white; border-radius: 5px; }
.form-footer { margin-top: 25px; display: flex; gap: 10px; }
.btn-save { background: #42b983; color: white; border: none; padding: 12px 25px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.btn-cancel, .btn-back { background: #444; color: white; border: none; padding: 12px 20px; border-radius: 6px; cursor: pointer; }

/* Детали */
.details-header { display: flex; align-items: center; gap: 20px; margin-bottom: 20px; }
.info-item { background: #252525; padding: 15px; border-radius: 6px; margin-bottom: 10px; display: flex; flex-direction: column; gap: 5px; }
.info-key { color: #42b983; font-size: 0.8rem; font-weight: bold; }
.info-val { font-size: 1.1rem; }

.status { text-align: center; padding: 50px; color: #666; }
</style>
