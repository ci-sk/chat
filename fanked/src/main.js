import { createApp } from 'vue'
import './assets/css/tailwindcss.css'
import "tailwindcss/tailwind.css";
// import './style.css'
import App from './App.vue'
import axios from "axios";
import router from './router';


axios.defaults.baseURL = "http://localhost:8080";
const app = createApp(App);
app.use(router);
app.mount('#app');
