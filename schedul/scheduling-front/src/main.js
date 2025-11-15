import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

import { createApp } from 'vue'
import { createPinia } from 'pinia'


import App from './App.vue'
import router from './router'

// 1. 引入Element Plus核心
import ElementPlus from 'element-plus'
// 2. 引入Element Plus全局样式（必须）
import 'element-plus/dist/index.css'
// 3. 引入图标库（全局注册所有图标，方便使用）
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

//注册elementplus图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(ElementPlus)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)
app.use(router)
app.mount('#app')
