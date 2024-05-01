import { createApp } from 'vue'
import './index.css'
import './style.css'
import 'normalize.css'
import 'animate.css';
import App from './App.vue'

import { createWebHistory, createRouter } from 'vue-router'


const routes = [
    { path: '/', component: App },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

createApp(App)
    .use(router)
    .mount('#app')
