<script setup>
import { ref, onMounted } from 'vue';
import api from '../api';

const products = ref([]);
const loading = ref(true);
const error = ref(null);

const viewMode = ref('list');
const currentProduct = ref(null);

const externalSearchQuery = ref('');
const externalResults = ref([]);
const isExternalLoading = ref(false);

const form = ref({
  id: null,
  sku: '',
  name: '',
  category: '',
  quantity: 1,
  price: 0,
  descriptionEntries: []
});

const fetchProducts = async () => {
  try {
    loading.value = true;
    const response = await api.get('/products');
    products.value = response.data;
  } catch (err) {
    error.value = "Помилка завантаження даних.";
  } finally {
    loading.value = false;
  }
};

onMounted(fetchProducts);

const searchExternal = async () => {
  if (!externalSearchQuery.value) return;
  isExternalLoading.value = true;
  try {
    const response = await api.get(`/import/search-external?query=${externalSearchQuery.value}`);
    externalResults.value = response.data;
  } catch (err) {
    alert("Помилка зовнішнього пошуку");
  } finally {
    isExternalLoading.value = false;
  }
};

const fillFormFromExternal = (item) => {
  form.value.name = item.title;
  form.value.sku = item.sku;
  form.value.category = item.category;
  form.value.price = item.price;
  form.value.descriptionEntries = [
    { key: 'Джерело', value: 'DummyJSON' },
    { key: 'Опис', value: item.description }
  ];
};

const deleteProduct = async (id) => {
  if (!confirm("Видалити цей товар?")) return;
  try {
    await api.delete(`/products/${id}`);
    products.value = products.value.filter(p => p.id !== id);
  } catch (err) {
    alert("Помилка при видаленні.");
  }
};

const openCreate = () => {
  form.value = { id: null, sku: '', name: '', category: '', quantity: 1, price: 0, descriptionEntries: [{ key: '', value: '' }] };
  externalResults.value = [];
  externalSearchQuery.value = '';
  viewMode.value = 'create';
};


const openEdit = (product) => {
  const entries = product.description
    ? Object.entries(product.description).map(([key, value]) => ({ key, value }))
    : [{ key: '', value: '' }];
  form.value = { ...product, descriptionEntries: entries };
  viewMode.value = 'edit';
};

const openDetails = (product) => {
  currentProduct.value = product;
  viewMode.value = 'details';
};

const addDescriptionField = () => form.value.descriptionEntries.push({ key: '', value: '' });
const removeDescriptionField = (index) => form.value.descriptionEntries.splice(index, 1);

const saveProduct = async () => {
  const descriptionObj = {};
  form.value.descriptionEntries.forEach(item => {
    if (item.key) descriptionObj[item.key] = item.value;
  });

  const payload = {
    sku: form.value.sku,
    name: form.value.name,
    category: form.value.category,
    quantity: form.value.quantity,
    price: form.value.price,
    description: descriptionObj,
    updatedAt: new Date().toISOString()
  };

  try {
    if (viewMode.value === 'create') {
      await api.post('/products', payload);
    } else {
      await api.put(`/products/${form.value.id}`, payload);
    }
    fetchProducts();
    viewMode.value = 'list';
  } catch (err) {
    alert("Помилка при збереженні.");
  }
};
</script>

<template>
  <div class="page-wrapper">
    <div class="container">

      <div v-if="viewMode === 'list'">
        <div class="header-section">
          <h1>📦 Склад товарів</h1>
          <div class="header-actions">
            <button @click="openCreate" class="btn-add-main">+ Додати товар</button>
            <button @click="fetchProducts" class="btn-refresh" title="Обновить">🔄</button>
          </div>
        </div>

        <div v-if="loading" class="status">Завантаження даних...</div>
        <div v-else class="table-wrapper">
          <table class="styled-table">
            <thead>
              <tr>
                <th>SKU</th>
                <th>Назва</th>
                <th>Категорія</th>
                <th>Кількість</th>
                <th>Ціна</th>
                <th style="text-align: center">Дії</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in products" :key="product.id">
                <td class="sku-cell">{{ product.sku }}</td>
                <td class="name-cell">{{ product.name }}</td>
                <td><span class="badge">{{ product.category }}</span></td>
                <td>{{ product.quantity }} шт.</td>
                <td class="price-cell">{{ product.price.toLocaleString() }} грн</td>
                <td class="actions">
                  <button @click="openDetails(product)" class="btn-icon btn-info">👁️</button>
                  <button @click="openEdit(product)" class="btn-icon btn-edit">✏️</button>
                  <button @click="deleteProduct(product.id)" class="btn-icon btn-delete">🗑️</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="viewMode === 'create' || viewMode === 'edit'" class="create-layout">

        <div class="form-card">
          <h2 class="form-title">{{ viewMode === 'create' ? '✨ Новий товар' : '✏️ Редагування' }}</h2>
          <div class="form-grid">
            <div class="input-group"><label>SKU</label><input v-model="form.sku" /></div>
            <div class="input-group"><label>Назва</label><input v-model="form.name" /></div>
            <div class="input-group"><label>Категорія</label><input v-model="form.category" /></div>
            <div class="input-group"><label>Кількість</label><input type="number" v-model="form.quantity" /></div>
            <div class="input-group"><label>Ціна (грн)</label><input type="number" v-model="form.price" /></div>
          </div>

          <h3 class="sub-title">Характеристики (JSON)</h3>
          <div v-for="(entry, index) in form.descriptionEntries" :key="index" class="json-row">
            <input v-model="entry.key" placeholder="Ключ" />
            <input v-model="entry.value" placeholder="Значення" />
            <button @click="removeDescriptionField(index)" class="btn-remove-json">✕</button>
          </div>
          <button @click="addDescriptionField" class="btn-add-json">+ Додати поле</button>

          <div class="form-footer">
            <button @click="saveProduct" class="btn-save">Зберегти</button>
            <button @click="viewMode = 'list'" class="btn-cancel">Відмінити</button>
          </div>
        </div>

        <div v-if="viewMode === 'create'" class="external-search-card">
          <h3 class="search-title">🔍 Импорт (DummyJSON)</h3>
          <div class="search-input-wrapper">
            <input v-model="externalSearchQuery" @keyup.enter="searchExternal" placeholder="Пошук (н-р: laptop)..." />
            <button @click="searchExternal" class="btn-search-ext">Знайти</button>
          </div>
          <div v-if="isExternalLoading" class="status-sm">Пошук...</div>
          <div class="external-results">
            <div v-for="item in externalResults" :key="item.sku" class="external-item" @click="fillFormFromExternal(item)">
              <img :src="item.thumbnail" class="ext-thumb" />
              <div class="ext-info">
                <div class="ext-name">{{ item.title }}</div>
                <div class="ext-price">{{ item.price }} грн</div>
              </div>
              <div class="ext-plus">+</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="viewMode === 'details'" class="details-card">
        <div class="details-header">
          <button @click="viewMode = 'list'" class="btn-add-json">← Назад</button>
          <h2>{{ currentProduct.name }}</h2>
        </div>
        <div class="details-content">
          <div class="main-info">
            <p><strong>SKU:</strong> {{ currentProduct.sku }}</p>
            <p><strong>Категорія:</strong> {{ currentProduct.category }}</p>
            <p><strong>Ціна:</strong> {{ currentProduct.price }} грн</p>
            <p><strong>Дата он.:</strong> {{ new Date(currentProduct.updatedAt).toLocaleString() }}</p>
          </div>
          <div class="json-info">
            <h3>Характеристики:</h3>
            <div v-for="(val, key) in currentProduct.description" :key="key" class="info-item">
              <span class="info-key">{{ key }}:</span> {{ val }}
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.price-cell { font-weight: bold; color: #42b983 !important; }
.ext-price { color: #42b983 !important; }

.btn-refresh { background: #333 !important; border: 1px solid #444 !important; color: #ccc !important; border-radius: 6px; padding: 5px 12px; cursor: pointer; }
.btn-add-json { background: #333 !important; color: #ccc !important; border: 1px solid #444 !important; padding: 8px 15px; border-radius: 5px; cursor: pointer; margin-top: 5px; }
.btn-remove-json { background: none !important; border: 1px solid #c0392b !important; color: #c0392b !important; padding: 0 12px; border-radius: 5px; cursor: pointer; }

.page-wrapper { margin-top: 20px; padding: 0 20px; }
.container { max-width: 1200px; margin: 0 auto; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; border-bottom: 1px solid #444; padding-bottom: 15px; }
.btn-add-main { background: #42b983; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.table-wrapper { background: #1a1a1a; border-radius: 8px; border: 1px solid #333; overflow: hidden; }
.styled-table { width: 100%; border-collapse: collapse; }
.styled-table th { background: #252525; color: #42b983; padding: 14px; text-align: left; border-bottom: 2px solid #333; }
.styled-table td { padding: 12px; border-bottom: 1px solid #2a2a2a; color: #ccc; }
.actions { display: flex; gap: 8px; }
.btn-icon { border: none; width: 32px; height: 32px; border-radius: 4px; cursor: pointer; color: white; }
.btn-info { background: #2980b9; }
.btn-edit { background: #f39c12; }
.btn-delete { background: #c0392b; }
.create-layout { display: grid; grid-template-columns: 1fr 400px; gap: 30px; align-items: start; }
.form-card { background: #1e1e1e; padding: 25px; border-radius: 10px; border: 1px solid #333; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.external-search-card { background: #1a1a1a; padding: 20px; border-radius: 10px; border: 1px dashed #42b983; }
.search-input-wrapper { display: flex; gap: 10px; margin-bottom: 15px; }
.search-input-wrapper input { flex: 1; padding: 10px; background: #000; border: 1px solid #444; color: white; border-radius: 5px; }
.btn-search-ext { background: #42b983; border: none; color: white; padding: 0 15px; border-radius: 5px; cursor: pointer; }
.external-results { display: flex; flex-direction: column; gap: 10px; max-height: 500px; overflow-y: auto; }
.external-item { display: flex; align-items: center; gap: 12px; background: #252525; padding: 10px; border-radius: 8px; cursor: pointer; border: 1px solid transparent; }
.external-item:hover { border-color: #42b983; background: #2a2a2a; }
.ext-thumb { width: 45px; height: 45px; border-radius: 4px; object-fit: cover; }
.ext-info { flex: 1; overflow: hidden; }
.ext-name { font-size: 0.85rem; font-weight: bold; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #eee; }
.ext-plus { font-size: 1.2rem; color: #42b983; padding-right: 5px; }
.input-group { display: flex; flex-direction: column; gap: 5px; margin-bottom: 10px; }
.input-group label { font-size: 0.8rem; color: #888; }
.input-group input { padding: 10px; background: #111; border: 1px solid #444; color: white; border-radius: 5px; }
.json-row { display: flex; gap: 10px; margin-bottom: 8px; }
.json-row input { flex: 1; padding: 8px; background: #111; border: 1px solid #444; color: white; border-radius: 5px; }
.form-footer { margin-top: 25px; display: flex; gap: 10px; }
.btn-save { background: #42b983; color: white; border: none; padding: 12px 25px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.btn-cancel { background: #444; color: white; border: none; padding: 12px 20px; border-radius: 6px; cursor: pointer; }
.details-card { background: #1e1e1e; padding: 30px; border-radius: 10px; border: 1px solid #333; }
.info-item { background: #252525; padding: 10px; border-radius: 5px; margin-bottom: 8px; }
.info-key { color: #42b983; font-weight: bold; margin-right: 10px; }
.status { text-align: center; padding: 50px; color: #666; }
</style>
