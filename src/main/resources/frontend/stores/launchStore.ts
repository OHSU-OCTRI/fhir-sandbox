import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

import { bearerTokenHeader, csrfTokenHeader } from '../utils/tokenUtils';

import type {
  FhirPatientEntry,
  UserPersona,
  SmartApp,
  CdsCard,
  LaunchParams,
  LaunchData,
  FhirPractitionerEntry
} from '../types';

/**
 * Returns the Pinia store for SMART on FHIR App launch state.
 */
export const useLaunchStore = defineStore('launch', () => {
  // API Endpoints
  const sandboxApi = ref<string>('');
  const sandboxId = ref<string>('');
  const fhirApi = ref<string>('');

  // Auth
  const bearerToken = ref<string>('');
  const csrfToken = ref<string | undefined>('');
  const fhirApiHeaders = computed(() => {
    return bearerTokenHeader(bearerToken.value);
  });
  const sandboxApiHeaders = computed(() => {
    const headers: HeadersInit = {};
    if (csrfToken.value) {
      return csrfTokenHeader(csrfToken.value);
    } else {
      return headers;
    }
  });

  // Derived URLs
  const launchCodeUrl = computed(() => `${sandboxApi.value}/create_context`);
  const personaAuthUrl = computed(() => `${sandboxApi.value}/userPersona/authenticate`);
  const registeredAppsUrl = computed(() => `${sandboxApi.value}/registered_apps`);

  // Domain State
  const selectedPatient = ref<FhirPatientEntry | null>(null);
  const selectedPractitioner = ref<FhirPractitionerEntry | null>(null);
  const selectedPersona = ref<UserPersona | null>(null);
  const loadedApps = ref<SmartApp[]>([]);
  const currentApp = ref<SmartApp | null>(null);
  const launchUrl = ref<string | null>(null);
  const cards = ref<CdsCard[]>([]);
  const launchParams = ref<LaunchParams>({});

  // UI State
  const showPatientSelector = ref<boolean>(false);
  const showPractitionerSelector = ref<boolean>(false);
  const showPersonaSelector = ref<boolean>(false);

  // Actions
  function initFromLaunchData(data: LaunchData): void {
    bearerToken.value = data.bearerToken;
    csrfToken.value = data.csrfToken;
    sandboxApi.value = data.sandboxApiUrl;
    sandboxId.value = data.sandboxId;
    fhirApi.value = data.fhirApi;

    const params: LaunchParams = {};
    if (data.encounter) params.encounter = data.encounter;
    if (data.location) params.location = data.location;
    if (data.resource) params.resource = data.resource;
    if (data.smartStyleUrl) params.smartStyleUrl = data.smartStyleUrl;
    if (data.intent) params.intent = data.intent;
    if (data.contextParams) {
      data.contextParams.forEach(c => {
        params[c.name] = c.value;
      });
    }
    launchParams.value = params;

    showPersonaSelector.value = !data.personaId;
  }

  function setSelectedPatient(patient: FhirPatientEntry): void {
    selectedPatient.value = patient;
  }

  function setSelectedPractitioner(practitioner: FhirPractitionerEntry): void {
    selectedPractitioner.value = practitioner;
  }

  function setSelectedPersona(persona: UserPersona): void {
    selectedPersona.value = persona;
  }

  function setLoadedApps(apps: SmartApp[]): void {
    loadedApps.value = apps;
  }

  function setCurrentApp(app: SmartApp | null): void {
    currentApp.value = app;
    launchUrl.value = null;
  }

  function setLaunchUrl(url: string): void {
    launchUrl.value = url;
  }

  function setCards(newCards: CdsCard[]): void {
    cards.value = newCards;
  }

  function appendCards(newCards: CdsCard[]): void {
    cards.value = cards.value.concat(newCards);
  }

  function clearCards(): void {
    cards.value = [];
  }

  function refreshLaunchParams(): void {
    const raw = sessionStorage.getItem('launchData');
    if (!raw) return;
    const data = JSON.parse(raw) as LaunchData;
    const params: LaunchParams = {};
    if (data.encounter) params.encounter = data.encounter;
    if (data.location) params.location = data.location;
    if (data.resource) params.resource = data.resource;
    if (data.smartStyleUrl) params.smartStyleUrl = data.smartStyleUrl;
    if (data.intent) params.intent = data.intent;
    if (data.contextParams) {
      data.contextParams.forEach(c => {
        params[c.name] = c.value;
      });
    }
    launchParams.value = params;
  }

  return {
    bearerToken,
    csrfToken,
    sandboxApi,
    sandboxId,
    fhirApi,
    launchCodeUrl,
    personaAuthUrl,
    registeredAppsUrl,
    selectedPatient,
    selectedPractitioner,
    selectedPersona,
    loadedApps,
    currentApp,
    launchUrl,
    cards,
    launchParams,
    showPatientSelector,
    showPractitionerSelector,
    showPersonaSelector,
    initFromLaunchData,
    setSelectedPatient,
    setSelectedPractitioner,
    setSelectedPersona,
    setLoadedApps,
    setCurrentApp,
    setLaunchUrl,
    setCards,
    appendCards,
    clearCards,
    refreshLaunchParams,
    fhirApiHeaders,
    sandboxApiHeaders
  };
});
