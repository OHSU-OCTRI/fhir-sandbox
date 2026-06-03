<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { watchDebounced } from '@vueuse/core';
import { useApi } from '../composables/useApi';
import { getName } from '../utils/fhirUtils';
import { bearerTokenHeader } from '../utils/tokenUtils';
import type { FhirPractitionerBundle, FhirPractitionerEntry } from '../types';

const props = defineProps<{
  bearerToken: string;
  fhirApi: string;
  selectedPractitioner: FhirPractitionerEntry | null;
}>();

const emit = defineEmits<{
  'practitioner-selected': [practitioner: FhirPractitionerEntry];
}>();

const api = useApi();
const practitionerArray = ref<FhirPractitionerEntry[]>([]);
const nameFilter = ref<string>('');

async function loadPractitioners(filter: string = nameFilter.value): Promise<void> {
  nameFilter.value = filter;
  let url = `${props.fhirApi}Practitioner?_sort:asc=family&_sort:asc=given`;
  if (filter !== '') {
    url += `&name:contains=${filter}`;
  }

  const authHeaders = bearerTokenHeader(props.bearerToken);
  const data = await api.get<FhirPractitionerBundle>(url, authHeaders);
  practitionerArray.value = data?.entry ?? [];
}

function handleSelectedPractitioner(practitioner: FhirPractitionerEntry): void {
  emit('practitioner-selected', practitioner);
}

function isActivePractitioner(practitioner: FhirPractitionerEntry): boolean {
  return props.selectedPractitioner?.resource.id === practitioner.resource.id;
}

onMounted(() => {
  loadPractitioners('');
});

watchDebounced(nameFilter, () => loadPractitioners(nameFilter.value), { debounce: 300 });
</script>

<template>
  <div class="launcher-panel">
    <div class="d-flex flex-row justify-content-between">
      <h1 class="fs-5">Select Practitioner</h1>
      <button
        type="button"
        class="btn-close"
        data-bs-dismiss="modal"
        aria-label="Close"
      ></button>
    </div>
    <div class="launcher-panel-body">
      <input
        id="practitioner-filter"
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
              <th>ID</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="practitioner in practitionerArray"
              :key="practitioner.resource.id"
              :class="{ 'table-active': isActivePractitioner(practitioner) }"
              @click="handleSelectedPractitioner(practitioner)"
            >
              <td>{{ getName(practitioner.resource) }}</td>
              <td>{{ practitioner.resource.id }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
