import { createRouter, createWebHistory } from 'vue-router'
import PatientTableView from '../views/PatientTableView.vue'
import PatientView from '../views/PatientView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'patients',
      component: PatientTableView
    },
    {
      path: '/patients/:id',
      name: 'patient',
      component: PatientView
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue')
    }
  ]
})

export default router
