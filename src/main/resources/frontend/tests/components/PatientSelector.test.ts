import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import flushPromises from 'flush-promises';
import PatientSelector from '../../components/PatientSelector.vue';
import type { FhirPatientBundle, FhirPatientEntry } from '../../types';

const patient1: FhirPatientEntry = {
  resource: {
    resourceType: 'Patient',
    id: 'p1',
    name: [{ given: ['Alice'], family: 'Smith' }],
    birthDate: '1985-03-15',
    gender: 'female',
  },
};

const patient2: FhirPatientEntry = {
  resource: {
    resourceType: 'Patient',
    id: 'p2',
    name: [{ given: ['Bob'], family: 'Jones' }],
    birthDate: '1992-07-22',
    gender: 'male',
  },
};

function makeBundle(entries: FhirPatientEntry[]): FhirPatientBundle {
  return { resourceType: 'Bundle', total: entries.length, link: [], entry: entries };
}

function stubFetch(bundle: FhirPatientBundle) {
  const fetchMock = vi.fn().mockResolvedValue({
    ok: true,
    json: vi.fn().mockResolvedValue(bundle),
  });
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

function mountComponent(overrides: {
  bearerToken?: string;
  fhirApi?: string;
  selectedPatient?: FhirPatientEntry | null;
} = {}) {
  return mount(PatientSelector, {
    props: {
      bearerToken: 'test-token',
      fhirApi: 'https://fhir.example.com/',
      selectedPatient: null,
      ...overrides,
    },
  });
}

describe('PatientSelector', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  describe('initial render', () => {
    test('renders "Select Patient" heading', async () => {
      stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      expect(wrapper.find('h1').text()).toBe('Select Patient');
    });

    test('renders table headers: Name, Birth Date, Gender', async () => {
      stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      const headers = wrapper.findAll('th');
      expect(headers.map((h) => h.text())).toEqual(['Name', 'Birth Date', 'Gender']);
    });

    test('renders the filter input with the correct placeholder', async () => {
      stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      const input = wrapper.find('#patient-filter');
      expect(input.attributes('placeholder')).toBe('Filter by name');
    });

    test('renders a close button', async () => {
      stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      expect(wrapper.find('.btn-close').exists()).toBe(true);
    });
  });

  describe('patient loading', () => {
    test('fetches patients on mount without a name filter', async () => {
      const fetchMock = stubFetch(makeBundle([patient1, patient2]));
      mountComponent();
      await flushPromises();

      expect(fetchMock).toHaveBeenCalledOnce();
      const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(url).toContain('Patient?');
      expect(url).not.toContain('name:contains');
    });

    test('includes the fhirApi base URL in the fetch request', async () => {
      const fetchMock = stubFetch(makeBundle([]));
      mountComponent({ fhirApi: 'https://my-fhir.example.com/' });
      await flushPromises();

      const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(url).toMatch(/^https:\/\/my-fhir\.example\.com\//);
    });

    test('sends the bearer token in the Authorization header', async () => {
      const fetchMock = stubFetch(makeBundle([]));
      mountComponent({ bearerToken: 'my-secret-token' });
      await flushPromises();

      const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
      const headers = init.headers as Record<string, string>;
      expect(headers['Authorization']).toBe('Bearer my-secret-token');
    });

    test('renders a row for each patient returned by the API', async () => {
      stubFetch(makeBundle([patient1, patient2]));
      const wrapper = mountComponent();
      await flushPromises();

      expect(wrapper.findAll('tbody tr')).toHaveLength(2);
    });

    test('displays patient name, formatted birth date, and gender', async () => {
      stubFetch(makeBundle([patient1]));
      const wrapper = mountComponent();
      await flushPromises();

      const cells = wrapper.find('tbody tr').findAll('td');
      expect(cells[0].text()).toBe('Alice Smith');
      expect(cells[1].text()).toBe('15 Mar 1985');
      expect(cells[2].text()).toBe('female');
    });

    test('renders no rows when the bundle has no entries', async () => {
      stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();

      expect(wrapper.findAll('tbody tr')).toHaveLength(0);
    });

    test('handles a bundle whose entry field is absent', async () => {
      const bundle = { resourceType: 'Bundle', total: 0, link: [] } as unknown as FhirPatientBundle;
      stubFetch(bundle);
      const wrapper = mountComponent();
      await flushPromises();

      expect(wrapper.findAll('tbody tr')).toHaveLength(0);
    });
  });

  describe('name filter', () => {
    test('appends name:contains param after debounce when filter text is typed', async () => {
      const fetchMock = stubFetch(makeBundle([patient1]));
      const wrapper = mountComponent();
      await flushPromises();
      fetchMock.mockClear();

      await wrapper.find('#patient-filter').setValue('Alice');
      vi.advanceTimersByTime(300);
      await flushPromises();

      expect(fetchMock).toHaveBeenCalledOnce();
      const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(url).toContain('name:contains=Alice');
    });

    test('does not fire before the 300 ms debounce elapses', async () => {
      const fetchMock = stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      fetchMock.mockClear();

      await wrapper.find('#patient-filter').setValue('Bo');
      vi.advanceTimersByTime(200);
      await flushPromises();

      expect(fetchMock).not.toHaveBeenCalled();
    });

    test('omits name:contains when the filter is cleared', async () => {
      const fetchMock = stubFetch(makeBundle([patient1, patient2]));
      const wrapper = mountComponent();
      await flushPromises();

      await wrapper.find('#patient-filter').setValue('Alice');
      vi.advanceTimersByTime(300);
      await flushPromises();
      fetchMock.mockClear();

      await wrapper.find('#patient-filter').setValue('');
      vi.advanceTimersByTime(300);
      await flushPromises();

      const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(url).not.toContain('name:contains');
    });

    test('sends the updated filter text in the request URL', async () => {
      const fetchMock = stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      fetchMock.mockClear();

      await wrapper.find('#patient-filter').setValue('John');
      vi.advanceTimersByTime(300);
      await flushPromises();

      const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(url).toContain('name:contains=John');
    });
  });

  describe('patient selection', () => {
    test('emits patient-selected with the correct patient when a row is clicked', async () => {
      stubFetch(makeBundle([patient1, patient2]));
      const wrapper = mountComponent();
      await flushPromises();

      await wrapper.findAll('tbody tr')[1].trigger('click');

      const emitted = wrapper.emitted('patient-selected');
      expect(emitted).toHaveLength(1);
      expect(emitted![0]).toEqual([patient2]);
    });

    test('emits patient-selected for the first row', async () => {
      stubFetch(makeBundle([patient1, patient2]));
      const wrapper = mountComponent();
      await flushPromises();

      await wrapper.find('tbody tr').trigger('click');

      expect(wrapper.emitted('patient-selected')![0]).toEqual([patient1]);
    });

    test('emits a separate event for each row click', async () => {
      stubFetch(makeBundle([patient1, patient2]));
      const wrapper = mountComponent();
      await flushPromises();

      const rows = wrapper.findAll('tbody tr');
      await rows[0].trigger('click');
      await rows[1].trigger('click');

      expect(wrapper.emitted('patient-selected')).toHaveLength(2);
    });
  });

  describe('active patient highlighting', () => {
    test('applies table-active to the row matching the selectedPatient prop', async () => {
      stubFetch(makeBundle([patient1, patient2]));
      const wrapper = mountComponent({ selectedPatient: patient1 });
      await flushPromises();

      const rows = wrapper.findAll('tbody tr');
      expect(rows[0].classes()).toContain('table-active');
      expect(rows[1].classes()).not.toContain('table-active');
    });

    test('applies table-active to the row containing the selected patient', async () => {
      stubFetch(makeBundle([patient1, patient2]));
      const wrapper = mountComponent({ selectedPatient: patient2 });
      await flushPromises();

      const rows = wrapper.findAll('tbody tr');
      expect(rows[0].classes()).not.toContain('table-active');
      expect(rows[1].classes()).toContain('table-active');
    });

    test('no row has table-active when selectedPatient is null', async () => {
      stubFetch(makeBundle([patient1, patient2]));
      const wrapper = mountComponent({ selectedPatient: null });
      await flushPromises();

      for (const row of wrapper.findAll('tbody tr')) {
        expect(row.classes()).not.toContain('table-active');
      }
    });
  });
});
