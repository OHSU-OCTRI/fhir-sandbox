import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useLaunchStore } from '../../stores/launchStore';
import type {
  CdsCard,
  FhirPatientEntry,
  FhirPractitionerEntry,
  LaunchData,
  SmartApp,
  UserPersona,
} from '../../types';

const launchDataBase: LaunchData = {
  bearerToken: 'test-token',
  sandboxApiUrl: 'https://api.example.com',
  sandboxId: 'sandbox-1',
  fhirApi: 'https://fhir.example.com',
};

const patient: FhirPatientEntry = {
  resource: {
    resourceType: 'Patient',
    id: 'p1',
    name: [{ given: ['Jane'], family: 'Doe' }],
    birthDate: '1990-01-01',
    gender: 'female',
  },
};

const practitioner: FhirPractitionerEntry = {
  resource: {
    resourceType: 'Practitioner',
    id: 'pr1',
    name: [{ given: ['John'], family: 'Smith' }],
    birthDate: '1980-06-15',
    gender: 'male',
  },
};

const persona: UserPersona = {
  id: 1,
  personaName: 'Nurse',
  personaUserId: 'nurse1',
  password: 'secret',
  fhirId: 'fhir-nurse-1',
  resourceUrl: 'https://fhir.example.com/Practitioner/fhir-nurse-1',
};

const app: SmartApp = {
  id: 'app1',
  clientName: 'My App',
  launchUri: 'https://myapp.example.com/launch',
  sandbox: { sandboxId: 'sandbox-1' },
};

const card: CdsCard = {
  summary: 'Alert',
  indicator: 'info',
};

describe('useEhrStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('initial state', () => {
    test('auth and API fields start empty', () => {
      const store = useLaunchStore();
      expect(store.bearerToken).toBe('');
      expect(store.sandboxApi).toBe('');
      expect(store.sandboxId).toBe('');
      expect(store.fhirApi).toBe('');
    });

    test('domain state starts null or empty', () => {
      const store = useLaunchStore();
      expect(store.selectedPatient).toBeNull();
      expect(store.selectedPractitioner).toBeNull();
      expect(store.selectedPersona).toBeNull();
      expect(store.loadedApps).toEqual([]);
      expect(store.currentApp).toBeNull();
      expect(store.launchUrl).toBeNull();
      expect(store.cards).toEqual([]);
      expect(store.launchParams).toEqual({});
    });

    test('dialog visibility flags start false', () => {
      const store = useLaunchStore();
      expect(store.showPatientSelector).toBe(false);
      expect(store.showPractitionerSelector).toBe(false);
      expect(store.showPersonaSelector).toBe(false);
    });
  });

  describe('computed URLs', () => {
    test('launchCodeUrl, personaAuthUrl, and registeredAppsUrl derive from sandboxApi', () => {
      const store = useLaunchStore();
      store.sandboxApi = 'https://api.example.com';
      expect(store.launchCodeUrl).toBe('https://api.example.com/create_context');
      expect(store.personaAuthUrl).toBe('https://api.example.com/userPersona/authenticate');
      expect(store.registeredAppsUrl).toBe('https://api.example.com/registered_apps');
    });

    test('computed URLs update when sandboxApi changes', () => {
      const store = useLaunchStore();
      store.sandboxApi = 'https://api.example.com';
      expect(store.launchCodeUrl).toBe('https://api.example.com/create_context');
      store.sandboxApi = 'https://other.example.com';
      expect(store.launchCodeUrl).toBe('https://other.example.com/create_context');
    });
  });

  describe('initFromLaunchData', () => {
    test('sets bearer, sandboxApi, sandboxId, and fhirApi', () => {
      const store = useLaunchStore();
      store.initFromLaunchData(launchDataBase);
      expect(store.bearerToken).toBe('test-token');
      expect(store.sandboxApi).toBe('https://api.example.com');
      expect(store.sandboxId).toBe('sandbox-1');
      expect(store.fhirApi).toBe('https://fhir.example.com');
    });

    test('maps optional launch data fields into launchParams', () => {
      const store = useLaunchStore();
      store.initFromLaunchData({
        ...launchDataBase,
        encounter: 'enc-1',
        location: 'loc-1',
        resource: 'res-1',
        smartStyleUrl: 'https://style.example.com',
        intent: 'order',
      });
      expect(store.launchParams).toEqual({
        encounter: 'enc-1',
        location: 'loc-1',
        resource: 'res-1',
        smartStyleUrl: 'https://style.example.com',
        intent: 'order',
      });
    });

    test('spreads contextParams into launchParams', () => {
      const store = useLaunchStore();
      store.initFromLaunchData({
        ...launchDataBase,
        contextParams: [
          { name: 'foo', value: 'bar' },
          { name: 'baz', value: 'qux' },
        ],
      });
      expect(store.launchParams).toMatchObject({ foo: 'bar', baz: 'qux' });
    });

    test('omits optional fields from launchParams when absent', () => {
      const store = useLaunchStore();
      store.initFromLaunchData(launchDataBase);
      expect(store.launchParams).toEqual({});
    });

    test('shows persona selector when personaId is absent', () => {
      const store = useLaunchStore();
      store.initFromLaunchData(launchDataBase);
      expect(store.showPersonaSelector).toBe(true);
    });

    test('hides persona selector when personaId is present', () => {
      const store = useLaunchStore();
      store.initFromLaunchData({ ...launchDataBase, personaId: 42 });
      expect(store.showPersonaSelector).toBe(false);
    });
  });

  describe('setSelectedPatient', () => {
    test('stores the selected patient', () => {
      const store = useLaunchStore();
      store.setSelectedPatient(patient);
      expect(store.selectedPatient).toEqual(patient);
    });
  });

  describe('setSelectedPractitioner', () => {
    test('stores the selected practitioner', () => {
      const store = useLaunchStore();
      store.setSelectedPractitioner(practitioner);
      expect(store.selectedPractitioner).toEqual(practitioner);
    });
  });

  describe('setSelectedPersona', () => {
    test('stores the selected persona', () => {
      const store = useLaunchStore();
      store.setSelectedPersona(persona);
      expect(store.selectedPersona).toEqual(persona);
    });
  });

  describe('setLoadedApps', () => {
    test('stores the apps list', () => {
      const store = useLaunchStore();
      store.setLoadedApps([app]);
      expect(store.loadedApps).toEqual([app]);
    });
  });

  describe('setCurrentApp', () => {
    test('stores the current app', () => {
      const store = useLaunchStore();
      store.setCurrentApp(app);
      expect(store.currentApp).toEqual(app);
    });

    test('clears launchUrl when a new app is set', () => {
      const store = useLaunchStore();
      store.setLaunchUrl('https://old.example.com/launch');
      store.setCurrentApp(app);
      expect(store.launchUrl).toBeNull();
    });

    test('accepts null to clear the current app', () => {
      const store = useLaunchStore();
      store.setCurrentApp(app);
      store.setCurrentApp(null);
      expect(store.currentApp).toBeNull();
    });
  });

  describe('setLaunchUrl', () => {
    test('stores the launch URL', () => {
      const store = useLaunchStore();
      store.setLaunchUrl('https://myapp.example.com/launch?iss=x');
      expect(store.launchUrl).toBe('https://myapp.example.com/launch?iss=x');
    });
  });

  describe('setCards / appendCards / clearCards', () => {
    test('setCards replaces the cards list', () => {
      const store = useLaunchStore();
      store.setCards([card]);
      expect(store.cards).toEqual([card]);
      const other: CdsCard = { summary: 'Warning', indicator: 'warning' };
      store.setCards([other]);
      expect(store.cards).toEqual([other]);
    });

    test('appendCards adds to the existing list', () => {
      const store = useLaunchStore();
      store.setCards([card]);
      const extra: CdsCard = { summary: 'Critical', indicator: 'critical' };
      store.appendCards([extra]);
      expect(store.cards).toEqual([card, extra]);
    });

    test('clearCards empties the list', () => {
      const store = useLaunchStore();
      store.setCards([card]);
      store.clearCards();
      expect(store.cards).toEqual([]);
    });
  });

  describe('refreshLaunchParams', () => {
    test('updates launchParams from sessionStorage', () => {
      const store = useLaunchStore();
      const data: LaunchData = {
        ...launchDataBase,
        encounter: 'enc-99',
        location: 'loc-99',
      };
      vi.stubGlobal('sessionStorage', {
        getItem: vi.fn().mockReturnValue(JSON.stringify(data)),
      });
      store.refreshLaunchParams();
      expect(store.launchParams).toMatchObject({ encounter: 'enc-99', location: 'loc-99' });
      vi.unstubAllGlobals();
    });

    test('does nothing when sessionStorage has no launchData', () => {
      const store = useLaunchStore();
      store.initFromLaunchData({ ...launchDataBase, encounter: 'enc-1' });
      vi.stubGlobal('sessionStorage', {
        getItem: vi.fn().mockReturnValue(null),
      });
      store.refreshLaunchParams();
      // launchParams should be unchanged from initFromLaunchData
      expect(store.launchParams).toMatchObject({ encounter: 'enc-1' });
      vi.unstubAllGlobals();
    });

    test('spreads contextParams from sessionStorage into launchParams', () => {
      const store = useLaunchStore();
      const data: LaunchData = {
        ...launchDataBase,
        contextParams: [{ name: 'custom', value: 'value' }],
      };
      vi.stubGlobal('sessionStorage', {
        getItem: vi.fn().mockReturnValue(JSON.stringify(data)),
      });
      store.refreshLaunchParams();
      expect(store.launchParams).toMatchObject({ custom: 'value' });
      vi.unstubAllGlobals();
    });
  });
});
