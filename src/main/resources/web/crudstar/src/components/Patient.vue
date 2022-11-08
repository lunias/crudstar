<script setup>
  import { RouterLink } from 'vue-router'
  import { storeToRefs } from 'pinia'

  defineProps(['patient'])
</script>

<template>
  <div>
    <div>
      <h2>{{ patient.firstName }} {{patient.lastName}}</h2>
      <p>{{ patient }}</p>
    </div>
  </div>
</template>
