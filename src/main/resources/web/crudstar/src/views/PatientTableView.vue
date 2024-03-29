<script setup>
  import { useRouter } from 'vue-router'
  import { storeToRefs } from 'pinia'
  import { usePatientStore } from '../stores/patient'
  import { FilterMatchMode, FilterOperator } from "primevue/api";
  import { useToast } from 'primevue/usetoast'

  const { patients, loading, error, filters, query, pageNumber, pageSize, sortField, sortOrder, totalRecords } = storeToRefs(usePatientStore())
  const { fetchPatients, onPage, onSort, onFilter, keywordSearch, clearFilters } = usePatientStore()

  const router = useRouter()
  const toast = useToast()

  fetchPatients({
    page: pageNumber.value,
    size: pageSize.value,
    sortField: sortField.value,
    sortOrder: sortOrder.value,
    filters: filters.value,
    query: query.value
  })

  const editPatient = (patient) => {
    router.push(`/patients/${patient.id}`)
  }

  const deletePatient = (patient) => {
    toast.add({
      severity: 'warn',
      summary: 'Delete',
      detail: 'Patient deleted',
      life: 1000
    })
  }

  const formatDate = (value) => {
    return new Date(value).toLocaleDateString("en-US", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    })
  }

  const onRowDblClick = (event) => {
    router.push(`/patients/${event.data.id}`)
  }
</script>

<template>
  <div class="patient-table-wrapper">
    <p v-if="error">{{ error.message }}</p>
    <DataTable class="p-datatable-patients p-datatable-lg"
               scrollHeight="flex" :scrollable="true" removableSort :sortField="sortField"
               :sortOrder="sortOrder" :value="patients" :lazy="true" :paginator="true"
               :rows="pageSize" :first="pageNumber*pageSize" v-model:filters="filters" ref="dt"
               dataKey="id" :totalRecords="totalRecords" :loading="loading"
               @page="onPage($event)" @sort="onSort($event)"
               @filter="onFilter($event)" filterDisplay="menu"
               :globalFilterFields="['firstName','lastName', 'dateOfBirth',
                                    'medicalRecordNumber']"
               responsiveLayout="scroll" :rowHover="true"
               paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink
                                  PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
               :rowsPerPageOptions="[10,20,50]"
               currentPageReportTemplate="Showing {first} to {last} of
                                          {totalRecords} patients"
               @row-dblclick="onRowDblClick($event, rowData, index)">
      <template #header>
        <div class="table-header">
          <h2 class="m-0">Patients</h2>
          <Button type="button" icon="pi pi-filter-slash" label="Clear Filters" class="p-button-outlined" @click="clearFilters()"/>
        </div>
      </template>
      <template #empty>
        No patients found.
      </template>
      <template #loading>
        Loading patient data. Please wait.
      </template>
      <Column field="lastName" header="Last Name" filterMatchMode="startsWith" ref="lastName" :sortable="true">
        <template #filter="{filterModel}">
          <InputText type="text" v-model="filterModel.value"
                     @keydown.enter="filterCallback()" class="p-column-filter"
                     placeholder="Search by last name"/>
        </template>
      </Column>
      <Column field="firstName" header="First Name" filterField="firstName"
              filterMatchMode="contains" ref="firstName"
              :sortable="true">
        <template #filter="{filterModel}">
          <InputText type="text" v-model="filterModel.value"
                     @keydown.enter="filterCallback()" class="p-column-filter"
                     placeholder="Search by first name"/>

        </template>
      </Column>
      <Column field="dateOfBirth" header="Date of Birth" :sortable="true"
              dataType="date" style="min-width: 8rem">
        <template #body="{data}">
          {{formatDate(data.dateOfBirth)}}
        </template>
        <template #filter="{filterModel}">
          <Calendar v-model="filterModel.value" dateFormat="mm/dd/yy"
                    placeholder="mm/dd/yyyy" @select="filterCallback()"
                    class="p-column-filter"/>
        </template>
      </Column>
      <Column field="medicalRecordNumber" header="MRN"
              filterField="medicalRecordNumber" ref="medicalRecordNumber"
              :sortable="true">
        <template #filter="{filterModel}">
          <InputText type="text" v-model="filterModel.value"
                     @keydown.enter="filterCallback()" class="p-column-filter"
                     placeholder="Search by MRN"/>

        </template>
      </Column>
      <Column :exportable="false" style="min-width:8rem">
        <template #body="slotProps">
          <Button icon="pi pi-pencil" class="p-button-rounded p-button-success mr-2" @click="editPatient(slotProps.data)" />
          <Button icon="pi pi-trash" class="p-button-rounded p-button-warning" @click="deletePatient(slotProps.data)" />
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped>
  .table-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .patient-table-wrapper {
    height: 88vh;
  }
  /* DataTable styling */
  :deep(.p-datatable) {
    background-color: #202020;
    border-color: #00ffb7;
    color: #c8c8c8;
    font-size: 14px;
  }

  :deep(.p-datatable .p-datatable-thead th) {
    background-color: #00ffb7;
    border-color: #00ffb7;
    color: #202020;
    font-weight: bold;
    text-shadow: 1px 1px 1px rgba(0,0,0,0.5);
  }

  :deep(.p-datatable .p-datatable-tbody td) {
    background-color: #202020;
    border-color: #00ffb7;
    color: #c8c8c8;
    text-shadow: 1px 1px 1px rgba(0,0,0,0.5);
  }

  :deep(.p-datatable .p-datatable-tbody tr:hover) {
    background-color: #00ffb7;
    color: #202020;
  }

  /* Pagination styling */
  :deep(.p-paginator) {
    background-color: #202020;
    border-color: #00ffb7;
    color: #c8c8c8;
    font-size: 14px;
  }

  :deep(.p-paginator.p-component.p-paginator-bottom) {
    text-align: center;
    display: inline-block;
  }

  :deep(.p-paginator .p-paginator-pages .p-paginator-page.p-highlight) {
    background-color: #00ffb7;
    border-color: #00ffb7;
    color: #202020;
    text-shadow: 1px 1px 1px rgba(0,0,0,0.5);
  }

  /* Dropdown styling */
  :deep(.p-dropdown) {
    background-color: #202020;
    border-color: #00ffb7;
    color: #c8c8c8;
    font-size: 14px;
  }

  :deep(.p-dropdown .p-dropdown-trigger) {
    border-color: #00ffb7;
  }

  :deep(.p-dropdown-items-wrapper) {
    background-color: #202020;
    border-color: #00ffb7;
    color: #c8c8c8;
  }

  :deep(.p-dropdown-items-wrapper .p-dropdown-item) {
    color: #c8c8c8;
    font-size: 14px;
    text-shadow: 1px 1px 1px rgba(0,0,0,0.5);
  }

  :deep(.p-dropdown-items-wrapper .p-highlight) {
    background-color: #00ffb7;
    color: #202020;
    font-weight: bold;
    text-shadow: 1px 1px 1px rgba(0,0,0,0.5);
  }

  :deep(.p-dropdown-items-wrapper .p-dropdown-item:hover) {
    background-color: #00ffb7;
    color: #202020;
  }
</style>
