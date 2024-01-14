<script setup>
  import { RouterView } from 'vue-router'
  import { ref } from 'vue'
  import { usePatientStore } from './stores/patient'

  const { keywordSearch } = usePatientStore()

  const items = ref([{
    label: 'File',
    icon: 'pi pi-fw pi-file',
    items: [{
        label: 'New',
        icon: 'pi pi-fw pi-plus',
        items: [{
            label: 'Patient',
            icon: 'pi pi-fw pi-id-card'
          },
          {
            label: 'Follow Up',
            icon: 'pi pi-fw pi-calendar-plus'
          },
          {
            label: 'Procedure',
            icon: 'pi pi-fw pi-calendar-plus'
          },
          {
            label: 'Medication',
            icon: 'pi pi-fw pi-info-circle'
          }
        ]
      },
      {
        separator: true
      },
      {
        label: 'Export',
        icon: 'pi pi-fw pi-external-link'
      }
    ]
  },
  {
    label: 'Patients',
    icon: 'pi pi-fw pi-id-card'
  },
  {
    label: 'Case Days',
    icon: 'pi pi-fw pi-calendar'
  },
  {
    label: 'Reports',
    icon: 'pi pi-fw pi-chart-bar'
  },
  {
    label: 'Manage',
    icon: 'pi pi-fw pi-cog'
  },
  {
    label: 'Admin',
    icon: 'pi pi-fw pi-server'
  }
  ])

  const patientQuery = ref()

  const searchPatients = () => {
    return keywordSearch(patientQuery.value)
  }

  const searchPatientsIfEmpty = (event) => {
    if (!patientQuery.value) {
      return keywordSearch()
    }
  }

</script>

<template>
<div class="box">
  <div class="row header">
    <Menubar :model="items">
      <template #start>
        <router-link to="/">
          <img alt="logo" src="https://www.primefaces.org/wp-content/uploads/2020/05/placeholder.png" height="40" class="mr-2">
        </router-link>
      </template>
      <template #end>
        <span class="p-input-icon-right">
          <i id="search-icon" class="pi pi-search" @click="searchPatients"/>
          <InputText class="p-inputtext-lg" v-model="patientQuery"
          placeholder="Search patient data" type="text"
          @keydown.enter="searchPatients" @blur="searchPatientsIfEmpty"/>
        </span>
      </template>
    </Menubar>
  </div>
  <div class="row content">
    <Toast />
    <main>
      <RouterView />
    </main>
  </div>
  <div class="flex align-items-center justify-content-center">
    <span>
      <h5>Built by ethanaa</h5>
    </span>
  </div>
</div>
</template>

<style scoped>
  .box {
    display: flex;
    flex-flow: column;
    height: 100%;
  }

  .box .row.header {
    flex: 0 1 auto;
  }

  .box .row.content {
    flex: 1 1 auto;
  }

  .box .row.footer {
    flex: 0 1 40px;
  }
  .row >>> .p-submenu-list {
    z-index: 2;
  }

  #search-icon {
    cursor: pointer;
  }
</style>
