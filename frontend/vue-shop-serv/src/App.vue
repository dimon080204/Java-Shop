<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header>
    <nav class="main-nav">
      <RouterLink to="/">Склад</RouterLink>
      <RouterLink to="/employees">Персонал</RouterLink>
      <RouterLink to="/purchases">Закупівлі</RouterLink>
      <RouterLink to="/sales">Продажі</RouterLink>
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
.content {
  padding: 20px;
}
body { margin: 0; font-family: sans-serif; }
</style>
