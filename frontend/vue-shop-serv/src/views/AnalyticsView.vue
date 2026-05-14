<script setup>
import { ref, onMounted } from 'vue';
import api from '../api';

const abcData = ref([]);
const staffKPI = ref([]);
const loading = ref(true);

const filterDateStart = ref(new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0]);
const filterDateEnd = ref(new Date().toISOString().split('T')[0]);

const fetchAnalytics = async () => {
  try {
    loading.value = true;

    const start = `${filterDateStart.value}T00:00:00`;
    const end = `${filterDateEnd.value}T23:59:59`;

    const [abcRes, kpiRes] = await Promise.all([
      api.get(`/analytics/abc?start=${start}&end=${end}`),
      api.get(`/analytics/staff-kpi?start=${start}&end=${end}`)
    ]);

    abcData.value = abcRes.data;
    staffKPI.value = kpiRes.data;
  } catch (err) {
    console.error("Ошибка запроса:", err);
  } finally {
    loading.value = false;
  }
};

onMounted(fetchAnalytics);

const resetFilters = () => {
  filterDateStart.value = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0];
  filterDateEnd.value = new Date().toISOString().split('T')[0];
  fetchAnalytics();
};
</script>

<template>
  <div class="page-wrapper">
    <div class="container">
      <div class="header-section">
        <h1>📊 Аналітичний модуль</h1>
      </div>

      <div class="filters-bar">
        <div class="date-group">
          <label class="filter-label">Період:</label>
          <input type="date" v-model="filterDateStart" @change="fetchAnalytics" />
          <input type="date" v-model="filterDateEnd" @change="fetchAnalytics" />
        </div>
        <button @click="resetFilters" class="btn-refresh">Скинути</button>
      </div>

      <div v-if="loading" class="status">Обробка даних на сервері...</div>

      <div v-else class="analytics-grid">
        <div class="analytics-card">
          <div class="card-header">
            <h3>ABC-аналіз товарів</h3>
            <span class="info-tag">По прибутку</span>
          </div>
          <div class="table-wrapper">
            <table class="styled-table">
              <thead>
                <tr>
                  <th>Група</th>
                  <th>Товар</th>
                  <th style="text-align: right">Прибуток</th>
                  <th style="text-align: right">Частка %</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in abcData" :key="item.productId" :class="'row-group-' + item.abcCategory">
                  <td><span :class="['abc-badge', item.abcCategory]">{{ item.abcCategory }}</span></td>
                  <td class="name-cell">{{ item.productName }}</td>
                  <td class="price-cell" style="text-align: right">{{ item.profit.toLocaleString() }} грн</td>
                  <td style="text-align: right; color: #888; font-size: 0.8rem;">{{ item.share.toFixed(1) }}%</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="analytics-card">
          <div class="card-header">
            <h3>Ефективність персоналу</h3>
            <span class="info-tag">KPI Рейтинг</span>
          </div>
          <div class="table-wrapper">
            <table class="styled-table">
              <thead>
                <tr>
                  <th>Співробітник</th>
                  <th>Продажі</th>
                  <th style="text-align: right">KPI</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in staffKPI" :key="s.staffId">
                  <td class="name-cell">{{ s.fullName }}</td>
                  <td>
                    <div style="font-size: 0.85rem">{{ s.totalRevenue.toLocaleString() }} грн</div>
                    <div style="font-size: 0.7rem; color: #777;">{{ s.salesCount }} чек. / сер. {{ Math.round(s.averageCheck) }}</div>
                  </td>
                  <td class="kpi-column">
                    <div class="kpi-container">
                      <div class="kpi-value" :class="getKpiClass(s.kpiScore)">{{ s.kpiScore }}</div>
                      <div class="kpi-track">
                        <div class="kpi-fill" :style="{ width: Math.min(s.kpiScore * 40, 100) + '%', backgroundColor: getKpiColor(s.kpiScore) }"></div>
                      </div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="abcData.length === 0" class="empty-placeholder">
          <div class="icon">🔍</div>
            <p>За обраний період операцій не знайдено</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    getKpiColor(score) {
      if (score >= 1.2) return '#27ae60'; // Green (better than target)
      if (score >= 0.8) return '#3498db'; // Blue (normal)
      return '#e74c3c'; // Red (below normal)
    },
    getKpiClass(score) {
      if (score >= 1.2) return 'high-kpi';
      if (score < 0.8) return 'low-kpi';
      return '';
    }
  }
}
</script>

<style scoped>
.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
}
.empty-placeholder .icon { font-size: 2rem; margin-bottom: 10px; }

.analytics-grid { display: grid; grid-template-columns: 1.1fr 0.9fr; gap: 20px; align-items: start; }
.high-kpi { color: #27ae60 !important; }
.low-kpi { color: #e74c3c !important; }
.abc-badge.A { background: #27ae60; color: white; }
.abc-badge.B { background: #f39c12; color: white; }
.abc-badge.C { background: #c0392b; color: white; }
.row-group-A { background: rgba(39, 174, 96, 0.05); }

.page-wrapper { margin-top: 20px; padding: 0 20px; }
.container { max-width: 1200px; margin: 0 auto; }
.filters-bar { display: flex; justify-content: space-between; align-items: center; background: #1e1e1e; padding: 15px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #333; }
.date-group { display: flex; align-items: center; gap: 10px; }
.date-group input { background: #111; border: 1px solid #444; color: white; padding: 8px; border-radius: 5px; }
.analytics-card { background: #1a1a1a; border-radius: 8px; border: 1px solid #333; overflow: hidden; }
.card-header { padding: 15px; background: #222; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #333; }
.styled-table { width: 100%; border-collapse: collapse; }
.styled-table th { background: #252525; color: #42b983; padding: 12px; text-align: left; font-size: 0.85rem; }
.styled-table td { padding: 12px; border-bottom: 1px solid #2a2a2a; color: #ccc; font-size: 0.9rem; }
.price-cell { font-weight: bold; color: #42b983 !important; }
.kpi-container { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.kpi-track { width: 60px; height: 4px; background: #333; border-radius: 2px; }
.kpi-fill { height: 100%; border-radius: 2px; transition: width 0.5s ease; }
.btn-refresh { background: #333; border: 1px solid #444; color: #ccc; padding: 8px 15px; border-radius: 5px; cursor: pointer; }
</style>
