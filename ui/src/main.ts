import { createApp, h } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import { createPinia } from 'pinia'

import '@featherds/styles'
import '@featherds/styles/themes/open-light.css'

createApp({
  render: () => h(App)
})
  .use(router)
  .use(createPinia())
  .use(store)
  .mount('#app')
