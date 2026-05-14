<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-header">
        <span class="logo-icon">🔐</span>
        <h2>Авторизація</h2>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>Логін</label>
          <input
            v-model="username"
            type="text"
            placeholder="Введіть ваш логін"
            required
          />
        </div>

        <div class="form-group">
          <label>Пароль</label>
          <input
            v-model="password"
            type="password"
            placeholder="••••••••"
            required
          />
        </div>

        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? 'Перевірка...' : 'Увійти' }}
        </button>

        <p v-if="error" class="error-text">{{ error }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api';

const router = useRouter();
const username = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);

const handleLogin = async () => {
  try {
    loading.value = true;
    error.value = '';

    // Send login request to the server
    const response = await api.post('/auth/login', {
      username: username.value,
      password: password.value
    });

    // Save the received token
    localStorage.setItem('token', response.data.token);

    // User name will also be useful for the interface
    localStorage.setItem('username', username.value);

    // Redirect to the main page
    router.push('/');
  } catch (err) {
    console.error(err);
    alert('Invalid username or password. Please try again.');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-wrapper {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #121212;
}
.login-card {
  background: #1a1a1a;
  padding: 40px;
  border-radius: 12px;
  border: 1px solid #333;
  width: 100%;
  max-width: 360px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
}
.login-header { text-align: center; margin-bottom: 30px; }
.logo-icon { font-size: 3rem; display: block; margin-bottom: 10px; }
h2 { color: #42b983; margin: 0; font-weight: 500; }
.form-group { margin-bottom: 20px; }
label { display: block; color: #888; margin-bottom: 8px; font-size: 0.9rem; }
input {
  width: 100%;
  background: #111;
  border: 1px solid #444;
  padding: 12px;
  border-radius: 6px;
  color: white;
  transition: border-color 0.3s;
}
input:focus { border-color: #42b983; outline: none; }
.login-btn {
  width: 100%;
  padding: 12px;
  background: #42b983;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.3s;
}
.login-btn:hover { background: #33a06f; }
.error-text { color: #ff5252; font-size: 0.85rem; margin-top: 15px; text-align: center; }
</style>
