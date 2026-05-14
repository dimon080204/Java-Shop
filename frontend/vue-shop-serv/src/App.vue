<script setup>
import { RouterLink, RouterView } from 'vue-router'

import { useRouter } from 'vue-router';

const router = useRouter();

const handleLogout = () => {
  // 1. Удаляем токен и данные пользователя
  localStorage.removeItem('token');
  localStorage.removeItem('username');

  // 2. Перенаправляем на страницу логина
  router.push('/login');
};
</script>

<template>
  <header>
    <nav class="main-nav">
      <RouterLink to="/login">Авторизація</RouterLink>
      <RouterLink to="/">Склад</RouterLink>
      <RouterLink to="/employees">Персонал</RouterLink>
      <RouterLink to="/purchases">Закупівлі</RouterLink>
      <RouterLink to="/sales">Продажі</RouterLink>
      <RouterLink to="/analytics">Аналітика</RouterLink>

      <button @click="handleLogout" class="logout-btn">
        <span>Вихід</span>
        <i class="icon-exit">🚪</i>
      </button>
    </nav>
  </header>

  <main class="content">
    <RouterView />
  </main>
</template>

<style>
.main-nav {
  background: #333;
  padding: 1rem;
  display: flex;
  gap: 20px;
}
.main-nav a {
  color: white;
  text-decoration: none;
  font-weight: bold;
}
.main-nav a.router-link-active {
  color: #42b983;
}
.logout-btn {
  margin-left: auto; /* Прижмет кнопку к правому краю */
  background: transparent;
  border: 1px solid #ff5252;
  color: #ff5252;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}
.logout-btn:hover {
  background: #ff5252;
  color: white;
}
.content {
  padding: 20px;
}
body { margin: 0; font-family: sans-serif; }
</style>
