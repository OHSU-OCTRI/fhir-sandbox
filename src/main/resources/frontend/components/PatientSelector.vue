<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { watchDebounced } from '@vueuse/core';
import { useApi } from '../composables/useApi';
import { getName } from '../utils/fhirUtils';
import { formatDate } from '../utils/dateUtils';
import { bearerTokenHeader } from '../utils/tokenUtils';
import type { FhirPatientEntry, FhirPatientBundle } from '../types';

const props = defineProps<{
  bearerToken: string;
  fhirApi: string;
  selectedPatient: FhirPatientEntry | null;
}>();

const emit = defineEmits<{
  'patient-selected': [patient: FhirPatientEntry];
}>();

const api = useApi();
const patientArray = ref<FhirPatientEntry[]>([]);
const nameFilter = ref<string>('');

async function loadPatients(filter: string = nameFilter.value): Promise<void> {
  nameFilter.value = filter;
  let url = `${props.fhirApi}Patient?_sort:asc=family&_sort:asc=given`;
  if (filter !== '') {
    url += `&name:contains=${filter}`;
  }

  const authHeaders = bearerTokenHeader(props.bearerToken);
  const data = await api.get<FhirPatientBundle>(url, authHeaders);
  patientArray.value = data?.entry ?? [];
}

function handleSelectedPatient(patient: FhirPatientEntry): void {
  emit('patient-selected', patient);
}

function isActivePatient(patient: FhirPatientEntry): boolean {
  return props.selectedPatient?.resource.id === patient.resource.id;
}

onMounted(async () => {
  await loadPatients('');
});

watchDebounced(nameFilter, () => loadPatients(nameFilter.value), { debounce: 300 });
</script>

<template>
  <div class="launcher-panel">
    <div class="d-flex flex-row justify-content-between">
      <h1 class="fs-5">Select Patient</h1>
      <button
        type="button"
        class="btn-close"
        data-bs-dismiss="modal"
        aria-label="Close"
      ></button>
    </div>
    <div class="launcher-panel-body">
      <input
        id="patient-filter"
        type="text"
        class="form-control"
        aria-label="Filter by name"
        placeholder="Filter by name"
        v-model="nameFilter"
      />
      <div class="launcher-panel-table-wrapper">
        <table class="table">
          <thead class="sticky-top">
            <tr>
              <th>Name</th>
              <th>Birth Date</th>
              <th>Gender</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="patient in patientArray"
              :key="patient.resource.id"
              class="patient-table-row"
              :class="{ 'table-active': isActivePatient(patient) }"
              @click="handleSelectedPatient(patient)"
            >
              <td>{{ getName(patient.resource) }}</td>
              <td>{{ formatDate(patient.resource.birthDate) }}</td>
              <td>{{ patient.resource.gender }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
