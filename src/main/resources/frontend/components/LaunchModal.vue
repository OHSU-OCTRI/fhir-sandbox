<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import Modal from 'bootstrap/js/dist/Modal';

import PatientSelector from './PatientSelector.vue';
import PractitionerSelector from './PractitionerSelector.vue';

import { useLaunchStore } from '../stores/launchStore.ts';
import { useApi } from '../composables/useApi.ts';
import { useApps } from '../composables/useApps';
import { getPersonId } from '../utils/fhirUtils.ts';
import type {
  FhirPatientEntry,
  FhirPractitionerEntry,
  SmartLaunchContextProperties
} from '../types';

const props = defineProps<{
  fhirApi: string;
  sandboxApi: string;
  sandboxId: string;
  bearerToken: string;
  csrfToken: string;
}>();

const store = useLaunchStore();
const api = useApi();
const { loadApps } = useApps();

const launchButtons = document.querySelectorAll('.client-launch');
const modalElement = document.querySelector('.modal');
let modal: Modal | undefined;

const currentAppId = ref<string | undefined>(undefined);

const launchData = computed(() => {
  return {
    bearerToken: props.bearerToken,
    csrfToken: props.csrfToken,
    fhirApi: props.fhirApi,
    sandboxApiUrl: props.sandboxApi,
    sandboxId: props.sandboxId,
    appId: currentAppId.value
  };
});

const resetState = () => {
  store.selectedPatient = null;
  store.selectedPractitioner = null;
  store.showPractitionerSelector = true;
  store.showPatientSelector = false;
};

const showModal = (evt: Event) => {
  evt.preventDefault();
  if (evt.currentTarget instanceof HTMLElement && modal) {
    currentAppId.value = evt.currentTarget.dataset?.clientId ?? '';
    const found = store.loadedApps.find(app => app.id === currentAppId.value);
    if (found) {
      store.setCurrentApp(found);
    }
    resetState();
    modal.show();
  }
};

const handlePatientSelected = (patient: FhirPatientEntry) => {
  store.selectedPatient = patient;
};

const handlePractitionerSelected = (practitioner: FhirPractitionerEntry) => {
  store.selectedPractitioner = practitioner;
};

const showPractitionerPane = () => {
  store.showPatientSelector = false;
  store.showPractitionerSelector = true;
};

const showPatientPane = () => {
  store.showPractitionerSelector = false;
  store.showPatientSelector = true;
};

const onLaunch = async () => {
  if (!modal || !store.currentApp || !store.selectedPatient) {
    return;
  }

  const requestBody: SmartLaunchContextProperties = {
    clientId: currentAppId.value,
    patientId: store.selectedPatient.resource.id
  };

  if (store.selectedPractitioner) {
    requestBody.fhirUser = getPersonId(store.selectedPractitioner.resource);
  }

  const contextResponse = await api.post<{ launch_id?: string; error?: string }>(
    `${props.sandboxApi}/create_context`,
    store.sandboxApiHeaders,
    requestBody,
    'same-origin'
  );

  if (contextResponse?.launch_id) {
    modal.hide();
    const launchUrl = new URL(store.currentApp.launchUri);
    launchUrl.searchParams.set('iss', props.fhirApi);
    launchUrl.searchParams.set('launch', contextResponse.launch_id);
    window.open(launchUrl.href, '_blank');
  }
};

onMounted(async () => {
  if (!modalElement) {
    console.error('No modal element found. Exiting.');
    return;
  }

  modal = Modal.getOrCreateInstance(modalElement);
  launchButtons.forEach(element => {
    element.addEventListener('click', showModal);
  });

  store.initFromLaunchData(launchData.value);
  await loadApps(launchData.value);
});
</script>

<template>
  <Teleport to="#launcher-modal-body">
    <template v-if="store.showPractitionerSelector">
      <PractitionerSelector
        :bearer-token="store.bearerToken"
        :fhir-api="store.fhirApi"
        :selected-practitioner="store.selectedPractitioner"
        @practitioner-selected="handlePractitionerSelected"
      />
      <button
        type="button"
        class="btn btn-primary ms-auto"
        @click="showPatientPane"
        :disabled="!store.selectedPractitioner"
      >
        Next
      </button>
    </template>
    <template v-if="store.showPatientSelector">
      <PatientSelector
        :bearer-token="store.bearerToken"
        :fhir-api="store.fhirApi"
        :selected-patient="store.selectedPatient"
        @patient-selected="handlePatientSelected"
      />
      <div class="ms-auto">
        <button
          type="button"
          class="btn btn-outline-secondary me-2"
          @click="showPractitionerPane"
        >
          Back
        </button>
        <button
          type="button"
          class="btn btn-primary"
          @click="onLaunch"
          :disabled="!store.selectedPatient"
        >
          Launch
        </button>
      </div>
    </template>
  </Teleport>
</template>
