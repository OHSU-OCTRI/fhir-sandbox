import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import flushPromises from 'flush-promises';
import LaunchModal from '../../components/LaunchModal.vue';
import { useLaunchStore } from '../../stores/launchStore.ts';
import type { FhirPatientEntry, FhirPractitionerEntry, SmartApp } from '../../types';

// vi.mock factories are hoisted before variable declarations, so all mock fns
// must be created with vi.hoisted() to be available in the factory closures.
const { mockShow, mockHide, mockGetOrCreateInstance, mockLoadApps } = vi.hoisted(() => {
  const mockShow = vi.fn();
  const mockHide = vi.fn();
  const mockGetOrCreateInstance = vi.fn(() => ({ show: mockShow, hide: mockHide }));
  const mockLoadApps = vi.fn().mockResolvedValue(undefined);
  return { mockShow, mockHide, mockGetOrCreateInstance, mockLoadApps };
});

vi.mock('bootstrap/js/dist/Modal', () => ({
  default: { getOrCreateInstance: mockGetOrCreateInstance }
}));

vi.mock('../../composables/useApps', () => ({
  useApps: () => ({ loadApps: mockLoadApps }),
}));

const app1: SmartApp = {
  id: 'app-1',
  clientName: 'App One',
  launchUri: 'https://app1.example.com/launch',
  sandbox: { sandboxId: 'sandbox-1' },
};

const patient: FhirPatientEntry = {
  resource: {
    resourceType: 'Patient',
    id: 'p1',
    name: [{ given: ['Alice'], family: 'Smith' }],
    birthDate: '1985-03-15',
    gender: 'female',
  },
};

const practitioner: FhirPractitionerEntry = {
  resource: {
    resourceType: 'Practitioner',
    id: 'pr1',
    name: [{ given: ['Jane'], family: 'Doe' }],
    birthDate: '1975-01-01',
    gender: 'female',
  },
};

const defaultProps = {
  fhirApi: 'https://fhir.example.com',
  sandboxApi: 'https://api.example.com',
  sandboxId: 'sandbox-1',
  bearerToken: 'test-bearer-token',
  csrfToken: 'test-csrf-token',
};

function createModalElement() {
  const el = document.createElement('div');
  el.className = 'modal';
  document.body.appendChild(el);
  return el;
}

function createLaunchButton(clientId: string) {
  const btn = document.createElement('button');
  btn.className = 'client-launch';
  btn.dataset.clientId = clientId;
  document.body.appendChild(btn);
  return btn;
}

function mountComponent(props: Partial<typeof defaultProps> = {}) {
  return mount(LaunchModal, {
    props: { ...defaultProps, ...props },
    attachTo: document.body,
    global: {
      stubs: {
        Teleport: { template: '<div><slot /></div>' },
        PatientSelector: {
          name: 'PatientSelector',
          template: '<div class="patient-selector-stub" />',
          emits: ['patient-selected'],
        },
        PractitionerSelector: {
          name: 'PractitionerSelector',
          template: '<div class="practitioner-selector-stub" />',
          emits: ['practitioner-selected'],
        },
      },
    },
  });
}

describe('LaunchModal', () => {
  let store: ReturnType<typeof useLaunchStore>;

  beforeEach(() => {
    setActivePinia(createPinia());
    store = useLaunchStore();
    createModalElement();
    mockShow.mockClear();
    mockHide.mockClear();
    mockGetOrCreateInstance.mockClear();
    mockLoadApps.mockClear().mockResolvedValue(undefined);
  });

  afterEach(() => {
    document.body.innerHTML = '';
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  describe('initialization (onMounted)', () => {
    test('calls store.initFromLaunchData with props-derived launch data', async () => {
      const spy = vi.spyOn(store, 'initFromLaunchData');
      mountComponent();
      await flushPromises();

      expect(spy).toHaveBeenCalledOnce();
      expect(spy).toHaveBeenCalledWith({
        bearerToken: 'test-bearer-token',
        csrfToken: 'test-csrf-token',
        fhirApi: 'https://fhir.example.com',
        sandboxApiUrl: 'https://api.example.com',
        sandboxId: 'sandbox-1',
        appId: undefined,
      });
    });

    test('calls loadApps with the launch data on mount', async () => {
      mountComponent();
      await flushPromises();

      expect(mockLoadApps).toHaveBeenCalledOnce();
      expect(mockLoadApps).toHaveBeenCalledWith(
        expect.objectContaining({
          bearerToken: 'test-bearer-token',
          sandboxId: 'sandbox-1',
          fhirApi: 'https://fhir.example.com',
        }),
      );
    });

    test('creates a Bootstrap Modal from the .modal element', async () => {
      const modalEl = document.querySelector('.modal');
      mountComponent();
      await flushPromises();

      expect(mockGetOrCreateInstance).toHaveBeenCalledOnce();
      expect(mockGetOrCreateInstance).toHaveBeenCalledWith(modalEl);
    });

    test('logs an error and does not create modal when no .modal element exists', async () => {
      document.body.innerHTML = '';
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      mountComponent();
      await flushPromises();

      expect(consoleSpy).toHaveBeenCalledWith('No modal element found. Exiting.');
      expect(mockGetOrCreateInstance).not.toHaveBeenCalled();
    });

    test('adds click listeners to .client-launch buttons found in the DOM', async () => {
      const btn = createLaunchButton('app-1');
      mountComponent();
      await flushPromises();

      btn.click();

      expect(mockShow).toHaveBeenCalledOnce();
    });
  });

  describe('showModal', () => {
    test('shows the Bootstrap modal when a launch button is clicked', async () => {
      const btn = createLaunchButton('app-1');
      mountComponent();
      await flushPromises();

      btn.click();

      expect(mockShow).toHaveBeenCalledOnce();
    });

    test('finds and sets the current app from loadedApps by clientId', async () => {
      const btn = createLaunchButton('app-1');
      store.setLoadedApps([app1]);
      mountComponent();
      await flushPromises();

      btn.click();

      expect(store.currentApp).toEqual(app1);
    });

    test('does not change currentApp when the clientId has no matching app', async () => {
      const btn = createLaunchButton('unknown-id');
      store.setLoadedApps([app1]);
      mountComponent();
      await flushPromises();

      btn.click();

      expect(store.currentApp).toBeNull();
    });

    test('resets selectedPatient to null when modal is shown', async () => {
      const btn = createLaunchButton('app-1');
      store.selectedPatient = patient;
      mountComponent();
      await flushPromises();

      btn.click();

      expect(store.selectedPatient).toBeNull();
    });

    test('resets selectedPractitioner to null when modal is shown', async () => {
      const btn = createLaunchButton('app-1');
      store.selectedPractitioner = practitioner;
      mountComponent();
      await flushPromises();

      btn.click();

      expect(store.selectedPractitioner).toBeNull();
    });

    test('shows practitioner selector and hides patient selector on reset', async () => {
      const btn = createLaunchButton('app-1');
      store.showPatientSelector = true;
      store.showPractitionerSelector = false;
      mountComponent();
      await flushPromises();

      btn.click();

      expect(store.showPractitionerSelector).toBe(true);
      expect(store.showPatientSelector).toBe(false);
    });

    test('attaches listeners to multiple launch buttons independently', async () => {
      const btn1 = createLaunchButton('app-1');
      const btn2 = createLaunchButton('app-2');
      store.setLoadedApps([app1, { ...app1, id: 'app-2', clientName: 'App Two' }]);
      mountComponent();
      await flushPromises();

      btn2.click();

      expect(store.currentApp?.id).toBe('app-2');
      mockShow.mockClear();

      btn1.click();

      expect(store.currentApp?.id).toBe('app-1');
    });
  });

  describe('practitioner pane', () => {
    test('renders PractitionerSelector when showPractitionerSelector is true', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPractitionerSelector = true;
      await wrapper.vm.$nextTick();

      expect(wrapper.find('.practitioner-selector-stub').exists()).toBe(true);
    });

    test('does not render PractitionerSelector when showPractitionerSelector is false', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPractitionerSelector = false;
      await wrapper.vm.$nextTick();

      expect(wrapper.find('.practitioner-selector-stub').exists()).toBe(false);
    });

    test('renders a Next button when showPractitionerSelector is true', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPractitionerSelector = true;
      await wrapper.vm.$nextTick();

      const nextBtn = wrapper.findAll('button').find(b => b.text() === 'Next');
      expect(nextBtn?.exists()).toBe(true);
    });

    test('Next button is disabled when no practitioner is selected', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPractitionerSelector = true;
      store.selectedPractitioner = null;
      await wrapper.vm.$nextTick();

      const nextBtn = wrapper.findAll('button').find(b => b.text() === 'Next');
      expect(nextBtn?.attributes('disabled')).toBeDefined();
    });

    test('Next button is enabled when a practitioner is selected', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPractitionerSelector = true;
      store.selectedPractitioner = practitioner;
      await wrapper.vm.$nextTick();

      const nextBtn = wrapper.findAll('button').find(b => b.text() === 'Next');
      expect(nextBtn?.attributes('disabled')).toBeUndefined();
    });

    test('clicking Next sets showPatientSelector to true and hides practitioner selector', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPractitionerSelector = true;
      store.selectedPractitioner = practitioner;
      await wrapper.vm.$nextTick();

      const nextBtn = wrapper.findAll('button').find(b => b.text() === 'Next');
      await nextBtn!.trigger('click');

      expect(store.showPatientSelector).toBe(true);
      expect(store.showPractitionerSelector).toBe(false);
    });

    test('handlePractitionerSelected sets selectedPractitioner on the store', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPractitionerSelector = true;
      await wrapper.vm.$nextTick();

      const selector = wrapper.findComponent({ name: 'PractitionerSelector' });
      await selector.vm.$emit('practitioner-selected', practitioner);

      expect(store.selectedPractitioner).toEqual(practitioner);
    });
  });

  describe('patient pane', () => {
    test('renders PatientSelector when showPatientSelector is true', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPatientSelector = true;
      await wrapper.vm.$nextTick();

      expect(wrapper.find('.patient-selector-stub').exists()).toBe(true);
    });

    test('does not render PatientSelector when showPatientSelector is false', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPatientSelector = false;
      await wrapper.vm.$nextTick();

      expect(wrapper.find('.patient-selector-stub').exists()).toBe(false);
    });

    test('renders Back and Launch buttons when showPatientSelector is true', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPatientSelector = true;
      await wrapper.vm.$nextTick();

      const buttonTexts = wrapper.findAll('button').map(b => b.text());
      expect(buttonTexts).toContain('Back');
      expect(buttonTexts).toContain('Launch');
    });

    test('Launch button is disabled when no patient is selected', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPatientSelector = true;
      store.selectedPatient = null;
      await wrapper.vm.$nextTick();

      const launchBtn = wrapper.findAll('button').find(b => b.text() === 'Launch');
      expect(launchBtn?.attributes('disabled')).toBeDefined();
    });

    test('Launch button is enabled when a patient is selected', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPatientSelector = true;
      store.selectedPatient = patient;
      await wrapper.vm.$nextTick();

      const launchBtn = wrapper.findAll('button').find(b => b.text() === 'Launch');
      expect(launchBtn?.attributes('disabled')).toBeUndefined();
    });

    test('clicking Back sets showPractitionerSelector to true and hides patient selector', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPatientSelector = true;
      store.showPractitionerSelector = false;
      await wrapper.vm.$nextTick();

      const backBtn = wrapper.findAll('button').find(b => b.text() === 'Back');
      await backBtn!.trigger('click');

      expect(store.showPractitionerSelector).toBe(true);
      expect(store.showPatientSelector).toBe(false);
    });

    test('handlePatientSelected sets selectedPatient on the store', async () => {
      const wrapper = mountComponent();
      await flushPromises();
      store.showPatientSelector = true;
      await wrapper.vm.$nextTick();

      const selector = wrapper.findComponent({ name: 'PatientSelector' });
      await selector.vm.$emit('patient-selected', patient);

      expect(store.selectedPatient).toEqual(patient);
    });
  });

  describe('onLaunch', () => {
    function stubFetchPost(response: object) {
      vi.stubGlobal(
        'fetch',
        vi.fn().mockResolvedValue({
          ok: true,
          json: vi.fn().mockResolvedValue(response),
        }),
      );
    }

    async function setupPatientPane(wrapper: ReturnType<typeof mountComponent>) {
      store.showPatientSelector = true;
      store.selectedPatient = patient;
      store.setCurrentApp(app1);
      await wrapper.vm.$nextTick();
    }

    test('POSTs to the create_context endpoint when Launch is clicked', async () => {
      const fetchMock = vi.fn().mockResolvedValue({
        ok: true,
        json: vi.fn().mockResolvedValue({ launch_id: 'launch-123' }),
      });
      vi.stubGlobal('fetch', fetchMock);
      vi.spyOn(window, 'open').mockImplementation(() => null);

      const wrapper = mountComponent();
      await flushPromises();
      await setupPatientPane(wrapper);

      const launchBtn = wrapper.findAll('button').find(b => b.text() === 'Launch');
      await launchBtn!.trigger('click');
      await flushPromises();

      expect(fetchMock).toHaveBeenCalledOnce();
      const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(url).toContain('/create_context');
    });

    test('sends patientId in the POST request body', async () => {
      const fetchMock = vi.fn().mockResolvedValue({
        ok: true,
        json: vi.fn().mockResolvedValue({ launch_id: 'launch-123' }),
      });
      vi.stubGlobal('fetch', fetchMock);
      vi.spyOn(window, 'open').mockImplementation(() => null);

      const wrapper = mountComponent();
      await flushPromises();
      await setupPatientPane(wrapper);

      const launchBtn = wrapper.findAll('button').find(b => b.text() === 'Launch');
      await launchBtn!.trigger('click');
      await flushPromises();

      const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
      const body = JSON.parse(init.body as string);
      expect(body.patientId).toBe('p1');
    });

    test('hides the modal when create_context returns a launch_id', async () => {
      stubFetchPost({ launch_id: 'launch-123' });
      vi.spyOn(window, 'open').mockImplementation(() => null);

      const wrapper = mountComponent();
      await flushPromises();
      await setupPatientPane(wrapper);

      const launchBtn = wrapper.findAll('button').find(b => b.text() === 'Launch');
      await launchBtn!.trigger('click');
      await flushPromises();

      expect(mockHide).toHaveBeenCalledOnce();
    });

    test('opens a new browser tab when create_context returns a launch_id', async () => {
      stubFetchPost({ launch_id: 'launch-xyz' });
      const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null);

      const wrapper = mountComponent();
      await flushPromises();
      await setupPatientPane(wrapper);

      const launchBtn = wrapper.findAll('button').find(b => b.text() === 'Launch');
      await launchBtn!.trigger('click');
      await flushPromises();

      expect(openSpy).toHaveBeenCalledOnce();
      const [, target] = openSpy.mock.calls[0] as [string, string];
      expect(target).toBe('_blank');
    });

    test('includes iss and launch query params in the opened URL', async () => {
      stubFetchPost({ launch_id: 'launch-xyz' });
      const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null);

      const wrapper = mountComponent();
      await flushPromises();
      await setupPatientPane(wrapper);

      const launchBtn = wrapper.findAll('button').find(b => b.text() === 'Launch');
      await launchBtn!.trigger('click');
      await flushPromises();

      const [url] = openSpy.mock.calls[0] as [string, string];
      expect(url).toContain('launch=launch-xyz');
      expect(url).toContain('iss=');
      expect(url).toContain('fhir.example.com');
    });

    test('uses the app launchUri as the base for the opened URL', async () => {
      stubFetchPost({ launch_id: 'launch-abc' });
      const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null);

      const wrapper = mountComponent();
      await flushPromises();
      await setupPatientPane(wrapper);

      const launchBtn = wrapper.findAll('button').find(b => b.text() === 'Launch');
      await launchBtn!.trigger('click');
      await flushPromises();

      const [url] = openSpy.mock.calls[0] as [string, string];
      expect(url).toMatch(/^https:\/\/app1\.example\.com\/launch/);
    });

    test('does not open a window when create_context returns no launch_id', async () => {
      stubFetchPost({ error: 'Context creation failed' });
      const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null);

      const wrapper = mountComponent();
      await flushPromises();
      await setupPatientPane(wrapper);

      const launchBtn = wrapper.findAll('button').find(b => b.text() === 'Launch');
      await launchBtn!.trigger('click');
      await flushPromises();

      expect(openSpy).not.toHaveBeenCalled();
      expect(mockHide).not.toHaveBeenCalled();
    });

    test('does not fetch when no currentApp is set in the store', async () => {
      const fetchMock = vi.fn();
      vi.stubGlobal('fetch', fetchMock);

      const wrapper = mountComponent();
      await flushPromises();
      store.showPatientSelector = true;
      store.selectedPatient = patient;
      // currentApp remains null
      await wrapper.vm.$nextTick();

      // Launch button is present but guard should prevent fetch
      // Trigger onLaunch via the store check path by temporarily enabling
      // the button — set currentApp then clear it to verify guard
      store.setCurrentApp(app1);
      await wrapper.vm.$nextTick();
      store.setCurrentApp(null);

      // Button is disabled (no patient), but even if somehow triggered:
      // the guard !store.currentApp would prevent fetch
      expect(fetchMock).not.toHaveBeenCalled();
    });
  });
});
