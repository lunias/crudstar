import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import PrimeVue from 'primevue/config'

import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Row from 'primevue/row'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Panel from 'primevue/panel'
import Menubar from 'primevue/menubar'
import Menu from 'primevue/menu'
import Toast from 'primevue/toast'
import ToastService from 'primevue/toastservice'

import 'primevue/resources/themes/mdc-dark-deeppurple/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'
import 'primeflex/primeflex.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(PrimeVue, {ripple: false})
app.use(ToastService)

app.component('DataTable', DataTable)
app.component('Column', Column)
app.component('Row', Row)
app.component('InputText', InputText)
app.component('Button', Button)
app.component('Calendar', Calendar)
app.component('Panel', Panel)
app.component('Menubar', Menubar)
app.component('Menu', Menu)
app.component('Toast', Toast)

app.mount('#app')
