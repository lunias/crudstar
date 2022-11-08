import { defineStore } from 'pinia'
import { FilterMatchMode, FilterOperator } from "primevue/api";

export const usePatientStore = defineStore({
  id: 'patient',
  state: () => ({
    patients: [],
    patient: null,
    loading: false,
    error: null,
    pageNumber: 0,
    pageSize: 20,
    sortField: 'lastName',
    sortOrder: 1,
    totalRecords: 0,
    totalPages: 0,
    filters: {
      global: { value: null, matchMode: FilterMatchMode.CONTAINS },
      firstName: { operator: FilterOperator.OR, constraints: [{ value: '', matchMode: FilterMatchMode.STARTS_WITH }] },
      lastName: { operator: FilterOperator.OR, constraints: [{ value: '', matchMode: FilterMatchMode.STARTS_WITH }] },
      dateOfBirth: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.DATE_IS }] },
      medicalRecordNumber: { operator: FilterOperator.OR, constraints: [{ value: '', matchMode: FilterMatchMode.CONTAINS }] }
    }
  }),
  getters: {

  },
  actions: {
    async fetchPatients(requestParams = { page: 0, size: 20, sortField: 'lastName', sortOrder: 1, filters: this.filters }) {
      this.patients = []
      this.loading = true
      try {
        let sort = ''
        if (requestParams.sortField) {
           sort = '&sort=' + requestParams.sortField + ',' + (requestParams.sortOrder > 0 ? 'asc' : 'desc')
        }
        let filters = []
        if (requestParams.filters) {
          for (let key in requestParams.filters) {
            const constraints = requestParams.filters[key].constraints || []
            if (!constraints.length) {
              continue
            }
            const operator = requestParams.filters[key].operator || 'or'
            const populatedConstraints = []
            for (let constraint of constraints) {
              if (!constraint.value) {
                continue
              }
              populatedConstraints.push({ value: constraint.value, matchMode: constraint.matchMode })
            }
            if (populatedConstraints.length) {
              filters.push({ key: key, operator: operator, constraints: populatedConstraints })
            }
          }
        }
        let filterParam = ''
        if (filters.length) {
          filterParam = '&filters=' + encodeURI(JSON.stringify(filters))
        }
        const patients = await fetch(
          '/api/patient?page=' + requestParams.page +
            '&size=' + requestParams.size + sort + filterParam)
        .then((response) => response.json())
        const patientModels = patients._embedded.patientModelList
        for (let i = 0; i < patientModels.length; i++) {
          const selfLink = patientModels[i]._links.self.href
          patientModels[i].id = selfLink.substring(selfLink.lastIndexOf('/') + 1)
        }
        this.patients = patientModels
        this.pageNumber = patients.page.number
        this.pageSize = patients.page.size
        this.totalRecords = patients.page.totalElements
        this.totalPages = patients.page.totalPages
        this.sortField = requestParams.sortField
        this.sortOrder = requestParams.sortOrder
      } catch (error) {
        this.error = error
      } finally {
        this.loading = false
      }
    },
    async fetchPatient(id) {
      this.patient = null
      this.loading = true
      try {
        const patient = await fetch(`/api/patient/${id}`)
        .then((response) => response.json())
        const selfLink = patient._links.self.href
        patient.id = selfLink.substring(selfLink.lastIndexOf('/') + 1)
        this.patient = patient
      } catch (error) {
        this.error = error
      } finally {
        this.loading = false
      }
    },
    async onPage(event) {
      this.fetchPatients({ page: event.page, size: event.rows, sortField: event.sortField, sortOrder: event.sortOrder, filters: event.filters })
    },
    async onSort(event) {
      this.fetchPatients({ page: 0, size: this.pageSize, sortField: event.sortField, sortOrder: event.sortOrder, filters: event.filters })
    },
    async onFilter(event) {
      this.fetchPatients({ page: 0, size: this.pageSize, sortField: event.sortField, sortOrder: event.sortOrder, filters: event.filters })
    },
    async keywordSearch() {
      console.log(this.filters.global.value)
    },
    async clearFilters() {
      const initFilters = {
        global: { value: null, matchMode: FilterMatchMode.CONTAINS },
        firstName: { operator: FilterOperator.OR, constraints: [{ value: '', matchMode: FilterMatchMode.STARTS_WITH }] },
        lastName: { operator: FilterOperator.OR, constraints: [{ value: '', matchMode: FilterMatchMode.STARTS_WITH }] },
        dateOfBirth: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.DATE_IS }] },
        medicalRecordNumber: { operator: FilterOperator.OR, constraints: [{ value: '', matchMode: FilterMatchMode.CONTAINS }] }
      }
      this.filters = initFilters
      this.fetchPatients({ page: 0, size: this.pageSize, sortField: this.sortField, sortOrder: this.sortOrder, filters: initFilters })
    }
  }
})
