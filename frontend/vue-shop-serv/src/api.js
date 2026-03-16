import axios from 'axios';

const instance = axios.create({
  baseURL: 'http://localhost:8080/api', // Базовый URL твоего бэкенда
  timeout: 5000
});

export default instance;
