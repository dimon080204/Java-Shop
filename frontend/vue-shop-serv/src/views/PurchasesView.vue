<script setup>
import { ref, onMounted } from 'vue';
import api from '../api';

const purchases = ref([]);
const products = ref([]);
const staff = ref([]);
const loading = ref(true);
const viewMode = ref('list');

const filterProduct = ref('');
const filterStaff = ref('');
const filterDateStart = ref('');
const filterDateEnd = ref('');

const form = ref({ id: null, productId: null, staffId: null, quantity: 1, price: 0 });

const fetchPurchases = async () => {
  try {
    loading.value = true;
    let response;
    if (filterProduct.value) {
      response = await api.get(`/purchases/search/product?productName=${filterProduct.value}`);
    } else if (filterStaff.value) {
      response = await api.get(`/purchases/search/staff?staffLastName=${filterStaff.value}`);
    } else if (filterDateStart.value && filterDateEnd.value) {
      const start = new Date(filterDateStart.value).toISOString();
      const end = new Date(filterDateEnd.value).toISOString();
      response = await api.get(`/purchases/search/dates?start=${start}&end=${end}`);
    } else {
      response = await api.get('/purchases');
    }
    purchases.value = response.data;
  } catch (err) { console.error(err); } finally { loading.value = false; }
};

const loadDependencies = async () => {
  const [pRes, sRes] = await Promise.all([api.get('/products'), api.get('/staff')]);
  products.value = pRes.data; staff.value = sRes.data;
};

onMounted(() => { fetchPurchases(); loadDependencies(); });

const openCreate = () => {
  form.value = { id: null, productId: null, staffId: null, quantity: 1, price: 0 };
  viewMode.value = 'create';
};

const openEdit = (p) => {
  form.value = { id: p.id, productId: p.productId, staffId: p.staffId, quantity: p.quantity, price: p.price };
  viewMode.value = 'edit';
};

const savePurchase = async () => {
  try {
    if (viewMode.value === 'create') await api.post('/purchases', form.value);
    else await api.put(`/purchases/${form.value.id}`, form.value);
    viewMode.value = 'list';
    fetchPurchases();
  } catch (err) { alert("Помилка при збереженні даних"); }
};

const deletePurchase = async (id) => {
  if (confirm("Видалити запис?")) { await api.delete(`/purchases/${id}`); fetchPurchases(); }
};

const resetFilters = () => {
  filterProduct.value = ''; filterStaff.value = ''; filterDateStart.value = ''; filterDateEnd.value = '';
  fetchPurchases();
};
</script>

<template>
  <div class="page-wrapper">
    <div class="container">

      <div v-if="viewMode === 'list'">
        <div class="header-section">
          <h1>🧾 Журнал закупок</h1>
          <button @click="openCreate" class="btn-add-main">+ Оформить закупку</button>
        </div>

        <div class="filters-bar">
          <input v-model="filterProduct" @input="fetchPurchases" placeholder="Товар..." />
          <input v-model="filterStaff" @input="fetchPurchases" placeholder="Співробітник..." />
          <div class="date-group">
            <input type="date" v-model="filterDateStart" @change="fetchPurchases" />
            <input type="date" v-model="filterDateEnd" @change="fetchPurchases" />
          </div>
          <button @click="resetFilters" class="btn-refresh">Сброс</button>
        </div>

        <div v-if="loading" class="status">Завантаження...</div>
        <div v-else class="table-wrapper">
          <table class="styled-table">
            <thead>
              <tr>
                <th>Дата</th>
                <th>Товар</th>
                <th>Співробітник</th>
                <th>Кількість</th>
                <th>Ціна</th>
                <th>Сума</th>
                <th style="text-align: center">Дії</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in purchases" :key="p.id">
                <td class="sku-cell">{{ new Date(p.purchaseDate).toLocaleDateString() }}</td>
                <td class="name-cell">{{ p.productName }}</td>
                <td>{{ p.staffLastName }}</td>
                <td>{{ p.quantity }} шт.</td>
                <td>{{ p.price }}</td>
                <td class="price-cell">{{ p.cost }} грн</td>
                <td class="actions">
                  <button @click="openEdit(p)" class="btn-icon btn-edit">✏️</button>
                  <button @click="deletePurchase(p.id)" class="btn-icon btn-delete">🗑️</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="viewMode === 'create' || viewMode === 'edit'" class="centered-content">
        <div class="form-card">
          <h2 class="form-title">{{ viewMode === 'create' ? '📦 Нова закупка' : '✏️ Редагування' }}</h2>
          <div class="form-grid single-col">
            <div class="input-group">
              <label>Товар</label>
              <div class="select-with-btn">
                <select v-model="form.productId">
                  <option :value="null">Выберите товар...</option>
                  <option v-for="prod in products" :key="prod.id" :value="prod.id">{{ prod.name }}</option>
                </select>
              </div>
            </div>
            <div class="input-group">
              <label>Співробітник</label>
              <select v-model="form.staffId">
                <option :value="null">Виберіть співробітника...</option>
                <option v-for="s in staff" :key="s.id" :value="s.id">{{ s.lastName }}</option>
              </select>
            </div>
            <div class="form-row-flex">
              <div class="input-group"><label>Кількість</label><input type="number" v-model="form.quantity" /></div>
              <div class="input-group"><label>Ціна</label><input type="number" v-model="form.price" /></div>
            </div>
            <div class="total-preview">Итого: <span>{{ form.quantity * form.price }} грн</span></div>
          </div>
          <div class="form-footer">
            <button @click="savePurchase" class="btn-save">Зберегти</button>
            <button @click="viewMode = 'list'" class="btn-cancel">Відмінити</button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.page-wrapper { margin-top: 20px; padding: 0 20px; }
.container { max-width: 1200px; margin: 0 auto; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.btn-add-main { background: #42b983; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.filters-bar { display: flex; gap: 10px; background: #1e1e1e; padding: 15px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #333; }
.filters-bar input { background: #111; border: 1px solid #444; color: white; padding: 8px; border-radius: 5px; }
.table-wrapper { background: #1a1a1a; border-radius: 8px; border: 1px solid #333; overflow: hidden; }
.styled-table { width: 100%; border-collapse: collapse; }
.styled-table th { background: #252525; color: #42b983; padding: 14px; text-align: left; }
.styled-table td { padding: 12px; border-bottom: 1px solid #2a2a2a; color: #ccc; }
.price-cell { font-weight: bold; color: #42b983; }
.actions { display: flex; gap: 8px; justify-content: center; }
.btn-icon { border: none; width: 32px; height: 32px; border-radius: 4px; cursor: pointer; color: white; }
.btn-edit { background: #f39c12; }
.btn-delete { background: #c0392b; }
.btn-side { background: #333; border: 1px solid #444; color: #42b983; width: 40px; border-radius: 5px; cursor: pointer; }
.centered-content { display: flex; justify-content: center; }
.form-card { background: #1e1e1e; padding: 25px; border-radius: 10px; border: 1px solid #333; width: 450px; }
.input-group label { display: block; color: #888; font-size: 0.8rem; margin-bottom: 5px; }
.input-group select, .input-group input { width: 100%; padding: 10px; background: #111; border: 1px solid #444; color: white; border-radius: 5px; }
.select-with-btn { display: flex; gap: 5px; }
.total-preview { background: #252525; padding: 15px; border-radius: 6px; text-align: center; margin: 15px 0; }
.total-preview span { color: #42b983; font-weight: bold; }
.btn-save { background: #42b983; color: white; border: none; padding: 12px; border-radius: 6px; width: 100%; cursor: pointer; }
.btn-cancel { background: #444; color: white; border: none; padding: 12px; border-radius: 6px; width: 100%; margin-top: 10px; cursor: pointer; }
.date-group { display: flex; gap: 5px; }
.btn-refresh { background: #333; border: 1px solid #444; color: #ccc; padding: 0 15px; border-radius: 5px; cursor: pointer; }
</style>
