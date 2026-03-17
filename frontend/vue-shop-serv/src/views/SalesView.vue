<script setup>
import { ref, onMounted } from 'vue';
import api from '../api';

const sales = ref([]);
const products = ref([]);
const staff = ref([]);
const loading = ref(true);
const viewMode = ref('list');

const filterProduct = ref('');
const filterStaff = ref('');
const filterDateStart = ref('');
const filterDateEnd = ref('');

const form = ref({
  id: null,
  productId: null,
  staffId: null,
  quantity: 1,
  price: 0,
  discount: 0,
  paymentMethod: 'card'
});

const fetchSales = async () => {
  try {
    loading.value = true;
    let response;
    if (filterProduct.value) {
      response = await api.get(`/sales/search/product?name=${filterProduct.value}`);
    } else if (filterStaff.value) {
      response = await api.get(`/sales/search/staff?staffLastName=${filterStaff.value}`);
    } else if (filterDateStart.value && filterDateEnd.value) {
      const start = new Date(filterDateStart.value).toISOString();
      const end = new Date(filterDateEnd.value).toISOString();
      response = await api.get(`/sales/search/dates?start=${start}&end=${end}`);
    } else {
      response = await api.get('/sales');
    }
    sales.value = response.data;
  } catch (err) {
    console.error("Помилка завантаження продажів", err);
  } finally {
    loading.value = false;
  }
};

const loadDependencies = async () => {
  const [pRes, sRes] = await Promise.all([api.get('/products'), api.get('/staff')]);
  products.value = pRes.data;
  staff.value = sRes.data;
};

onMounted(() => {
  fetchSales();
  loadDependencies();
});

const openCreate = () => {
  form.value = { id: null, productId: null, staffId: null, quantity: 1, price: 0, discount: 0, paymentMethod: 'card' };
  viewMode.value = 'create';
};

const openEdit = (s) => {
  form.value = {
    id: s.id,
    productId: s.productId,
    staffId: s.staffId,
    quantity: s.quantity,
    price: s.price,
    discount: s.discount,
    paymentMethod: s.paymentMethod
  };
  viewMode.value = 'edit';
};

const saveSale = async () => {
  try {
    if (viewMode.value === 'create') await api.post('/sales', form.value);
    else await api.put(`/sales/${form.value.id}`, form.value);
    viewMode.value = 'list';
    fetchSales();
  } catch (err) {
    alert("Помилка при збереженні продажу. Перевірте залишки на складі!");
  }
};

const deleteSale = async (id) => {
  if (confirm("Видалити запис про продаж?")) {
    await api.delete(`/sales/${id}`);
    fetchSales();
  }
};

const resetFilters = () => {
  filterProduct.value = ''; filterStaff.value = ''; filterDateStart.value = ''; filterDateEnd.value = '';
  fetchSales();
};
</script>

<template>
  <div class="page-wrapper">
    <div class="container">

      <div v-if="viewMode === 'list'">
        <div class="header-section">
          <h1>💰 Журнал продаж</h1>
          <button @click="openCreate" class="btn-add-main">+ Нова продаж</button>
        </div>

        <div class="filters-bar">
          <input v-model="filterProduct" @input="fetchSales" placeholder="Товар..." />
          <input v-model="filterStaff" @input="fetchSales" placeholder="Продавець..." />
          <div class="date-group">
            <input type="date" v-model="filterDateStart" @change="fetchSales" />
            <input type="date" v-model="filterDateEnd" @change="fetchSales" />
          </div>
          <button @click="resetFilters" class="btn-refresh">Сброс</button>
        </div>

        <div v-if="loading" class="status">Завантаження даних...</div>
        <div v-else class="table-wrapper">
          <table class="styled-table">
            <thead>
              <tr>
                <th>Дата</th>
                <th>Товар</th>
                <th>Продавець</th>
                <th>Кількість</th>
                <th>Знижка</th>
                <th>Оплата</th>
                <th>Сума</th>
                <th style="text-align: center">Дії</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in sales" :key="s.id">
                <td class="sku-cell">{{ new Date(s.saleDate).toLocaleDateString() }}</td>
                <td class="name-cell">{{ s.productName }}</td>
                <td>{{ s.staffLastName }}</td>
                <td>{{ s.quantity }} шт.</td>
                <td style="color: #e74c3c">-{{ s.discount }}</td>
                <td>
                  <span :class="['pay-badge', s.paymentMethod]">
                    {{ s.paymentMethod === 'card' ? '💳 Карта' : '💵 Готівка' }}
                  </span>
                </td>
                <td class="price-cell">{{ s.totalPrice.toLocaleString() }} грн</td>
                <td class="actions">
                  <button @click="openEdit(s)" class="btn-icon btn-edit">✏️</button>
                  <button @click="deleteSale(s.id)" class="btn-icon btn-delete">🗑️</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="viewMode === 'create' || viewMode === 'edit'" class="centered-content">
        <div class="form-card">
          <h2 class="form-title">{{ viewMode === 'create' ? '🛍️ Оформлення продажу' : '✏️ Редагування продажу' }}</h2>
          <div class="form-grid single-col">
            <div class="input-group">
              <label>Товар</label>
              <select v-model="form.productId">
                <option :value="null">Виберіть товар...</option>
                <option v-for="prod in products" :key="prod.id" :value="prod.id">
                  {{ prod.name }} ({{ prod.quantity }} в наявності)
                </option>
              </select>
            </div>

            <div class="input-group">
              <label>Продавець</label>
              <select v-model="form.staffId">
                <option :value="null">Виберіть продавця...</option>
                <option v-for="st in staff" :key="st.id" :value="st.id">{{ st.lastName }} {{ st.firstName }}</option>
              </select>
            </div>

            <div class="form-row-flex">
              <div class="input-group"><label>Кількість</label><input type="number" v-model="form.quantity" /></div>
              <div class="input-group"><label>Ціна одиниці</label><input type="number" v-model="form.price" /></div>
            </div>

            <div class="form-row-flex">
              <div class="input-group"><label>Знижка (загальна)</label><input type="number" v-model="form.discount" /></div>
              <div class="input-group">
                <label>Метод оплаты</label>
                <select v-model="form.paymentMethod">
                  <option value="card">Карта</option>
                  <option value="cash">Готівка</option>
                </select>
              </div>
            </div>

            <div class="total-preview sale-total">
              К оплате: <span>{{ (form.quantity * form.price) - form.discount }} грн</span>
            </div>
          </div>

          <div class="form-footer">
            <button @click="saveSale" class="btn-save" :disabled="!form.productId || !form.staffId">Підтвердити</button>
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
.filters-bar input { background: #111; border: 1px solid #444; color: white; padding: 8px; border-radius: 5px; flex: 1; }

.table-wrapper { background: #1a1a1a; border-radius: 8px; border: 1px solid #333; overflow: hidden; }
.styled-table { width: 100%; border-collapse: collapse; }
.styled-table th { background: #252525; color: #42b983; padding: 14px; text-align: left; }
.styled-table td { padding: 12px; border-bottom: 1px solid #2a2a2a; color: #ccc; }

.price-cell { font-weight: bold; color: #42b983 !important; }
.pay-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.8rem; }
.pay-badge.card { background: #2980b9; color: white; }
.pay-badge.cash { background: #27ae60; color: white; }

.actions { display: flex; gap: 8px; justify-content: center; }
.btn-icon { border: none; width: 32px; height: 32px; border-radius: 4px; cursor: pointer; color: white; }
.btn-edit { background: #f39c12; }
.btn-delete { background: #c0392b; }

.centered-content { display: flex; justify-content: center; }
.form-card { background: #1e1e1e; padding: 25px; border-radius: 10px; border: 1px solid #333; width: 500px; }
.form-grid.single-col { display: flex; flex-direction: column; gap: 15px; }
.input-group label { display: block; color: #888; font-size: 0.8rem; margin-bottom: 5px; }
.input-group select, .input-group input { width: 100%; padding: 10px; background: #111; border: 1px solid #444; color: white; border-radius: 5px; }
.form-row-flex { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }

.total-preview { background: #252525; padding: 15px; border-radius: 6px; text-align: center; margin: 15px 0; border: 1px dashed #42b983; }
.total-preview span { color: #42b983; font-weight: bold; font-size: 1.3rem; }

.btn-save { background: #42b983; color: white; border: none; padding: 12px; border-radius: 6px; width: 100%; cursor: pointer; font-weight: bold; }
.btn-cancel { background: #444; color: white; border: none; padding: 12px; border-radius: 6px; width: 100%; margin-top: 10px; cursor: pointer; }
.date-group { display: flex; gap: 5px; }
.btn-refresh { background: #333; border: 1px solid #444; color: #ccc; padding: 0 15px; border-radius: 5px; cursor: pointer; }
</style>
