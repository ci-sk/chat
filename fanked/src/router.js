import { createRouter, createWebHistory } from 'vue-router';
import Login from './views/Login.vue';
import Chat from './views/Chat.vue';

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    { path: '/chat', component: Chat, meta: { requiresAuth: true } },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 路由守卫：检查用户是否已登录
router.beforeEach((to, from, next) => {
    const isAuthenticated = localStorage.getItem('token');
    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login');
    } else {
        next();
    }
});

export default router;