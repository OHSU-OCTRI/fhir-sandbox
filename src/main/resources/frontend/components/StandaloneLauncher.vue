<script setup lang="ts">
import { ref } from 'vue';

import PatientSelector from './PatientSelector.vue';
import PractitionerSelector from './PractitionerSelector.vue';
import { useApi } from '../composables/useApi.ts';
import { csrfTokenHeader } from '../utils/tokenUtils.ts';
import { getPersonId } from '../utils/fhirUtils.ts';
import type { FhirPatientEntry, FhirPractitionerEntry } from '../types';

const props = defineProps<{
  fhirApi: string;
  accessToken: string;
  clientId: string;
  sessionKey: string;
  csrfToken: string;
  completeUrl: string;
}>();

const api = useApi();

const selectedPatient = ref<FhirPatientEntry | null>(null);
const selectedPractitioner = ref<FhirPractitionerEntry | null>(null);
const showPatientSelector = ref(false);
const errorMessage = ref<string | null>(null);
const launching = ref(false);

const onLaunch = async () => {
  if (!selectedPatient.value || launching.value) return;

  launching.value = true;
  errorMessage.value = null;

  const body = {
    key: props.sessionKey,
    clientId: props.clientId,
    patientId: selectedPatient.value.resource.id,
    fhirUser: selectedPractitioner.value
      ? getPersonId(selectedPractitioner.value.resource)
      : undefined,
  };

  const result = await api.post<{ authorizeUrl?: string; error?: string }>(
    props.completeUrl,
    csrfTokenHeader(props.csrfToken),
    body,
    'same-origin'
  );

  if (result?.authorizeUrl) {
    window.location.href = result.authorizeUrl;
  } else {
    errorMessage.value = result?.error ?? 'An error occurred during launch. Please try again.';
    launching.value = false;
  }
};
</script>

<template>
  <div class="standalone-launcher">
    <div v-if="errorMessage" class="alert alert-danger" role="alert">
      {{ errorMessage }}
    </div>

    <template v-if="!showPatientSelector">
      <PractitionerSelector
        :bearer-token="accessToken"
        :fhir-api="fhirApi"
        :selected-practitioner="selectedPractitioner"
        @practitioner-selected="selectedPractitioner = $event"
      />
      <div class="d-flex justify-content-end mt-3 gap-2">
        <button type="button" class="btn btn-outline-secondary" @click="showPatientSelector = true">
          Skip
        </button>
        <button
          type="button"
          class="btn btn-primary"
          :disabled="!selectedPractitioner"
          @click="showPatientSelector = true"
        >
          Next
        </button>
      </div>
    </template>

    <template v-else>
      <PatientSelector
        :bearer-token="accessToken"
        :fhir-api="fhirApi"
        :selected-patient="selectedPatient"
        @patient-selected="selectedPatient = $event"
      />
      <div class="d-flex justify-content-end mt-3 gap-2">
        <button type="button" class="btn btn-outline-secondary" @click="showPatientSelector = false">
          Back
        </button>
        <button
          type="button"
          class="btn btn-primary"
          :disabled="!selectedPatient || launching"
          @click="onLaunch"
        >
          {{ launching ? 'Launching...' : 'Launch' }}
        </button>
      </div>
    </template>
  </div>
</template>
