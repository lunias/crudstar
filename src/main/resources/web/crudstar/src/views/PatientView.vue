<script setup>
  import { useRoute, useRouter } from 'vue-router'
  import { storeToRefs } from 'pinia'
  import { usePatientStore } from '../stores/patient'
  import Patient from '../components/Patient.vue'

  const route = useRoute()
  const { patient, loading, error } = storeToRefs(usePatientStore())
  const { fetchPatient } = usePatientStore()
  import { ref } from 'vue'
  import { useToast } from 'primevue/usetoast'

  const router = useRouter()

  fetchPatient(route.params.id)

  const toast = useToast()
  const menu = ref(null)

  const items = ref([{
    label: 'Options',
    items: [{
          label: 'Refresh',
          icon: 'pi pi-refresh',
          command: () => {
            toast.add({
              severity: 'success',
              summary: 'Refresh',
              detail: 'Patient refreshed',
              life: 1000
            })
            fetchPatient(route.params.id)
          }
        },
        {
          label: 'Delete',
          icon: 'pi pi-times',
          command: () => {
            toast.add({
              severity: 'warn',
              summary: 'Delete',
              detail: 'Patient deleted',
              life: 1000
            })
            router.push('/')
          }
        }
      ]
    }
  ])

  const toggle = (event) => {
    menu.value.toggle(event)
  }
</script>

<template>
  <div>
    <Panel v-if="patient" :header="patient.firstName + ' ' + patient.lastName" :toggleable="false">
      <template #icons>
        <button class="p-panel-header-icon p-link mr-2" @click="toggle">
          <span class="pi pi-cog"></span>
        </button>
        <Menu id="patient_menu" ref="menu" :model="items" :popup="true" />
      </template>
      <p v-if="patient">
        <Patient :patient="patient"></Patient>
      </p>
    </Panel>
    <h3 v-if="loading">Loading patient...</h3>
    <p v-if="error">{{ error.message }}</p>
  </div>
</template>
