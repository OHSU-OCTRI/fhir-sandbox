import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import flushPromises from 'flush-promises';
import PractitionerSelector from '../../components/PractitionerSelector.vue';
import type { FhirPractitionerBundle, FhirPractitionerEntry } from '../../types';

const practitioner1: FhirPractitionerEntry = {
  resource: {
    resourceType: 'Practitioner',
    id: 'pr1',
    name: [{ given: ['Jane'], family: 'Doe' }],
    gender: 'female',
    birthDate: '1978-01-07'
  },
};

const practitioner2: FhirPractitionerEntry = {
  resource: {
    resourceType: 'Practitioner',
    id: 'pr2',
    name: [{ given: ['John'], family: 'Smith' }],
    gender: 'male',
    birthDate: '1977-05-25'
  },
};

function makeBundle(entries: FhirPractitionerEntry[]): FhirPractitionerBundle {
  return { resourceType: 'Bundle', total: entries.length, link: [], entry: entries };
}

function stubFetch(bundle: FhirPractitionerBundle) {
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
  selectedPractitioner?: FhirPractitionerEntry | null;
} = {}) {
  return mount(PractitionerSelector, {
    props: {
      bearerToken: 'test-token',
      fhirApi: 'https://fhir.example.com/',
      selectedPractitioner: null,
      ...overrides,
    },
  });
}

describe('PractitionerSelector', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  describe('initial render', () => {
    test('renders "Select Practitioner" heading', async () => {
      stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      expect(wrapper.find('h1').text()).toBe('Select Practitioner');
    });

    test('renders table headers: Name and ID', async () => {
      stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      const headers = wrapper.findAll('th');
      expect(headers.map((h) => h.text())).toEqual(['Name', 'ID']);
    });

    test('renders the filter input with the correct placeholder', async () => {
      stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      const input = wrapper.find('#practitioner-filter');
      expect(input.attributes('placeholder')).toBe('Filter by name');
    });

    test('renders a close button', async () => {
      stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      expect(wrapper.find('.btn-close').exists()).toBe(true);
    });
  });

  describe('practitioner loading', () => {
    test('fetches practitioners on mount without a name filter', async () => {
      const fetchMock = stubFetch(makeBundle([practitioner1, practitioner2]));
      mountComponent();
      await flushPromises();

      expect(fetchMock).toHaveBeenCalledOnce();
      const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(url).toContain('Practitioner?');
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

    test('renders a row for each practitioner returned by the API', async () => {
      stubFetch(makeBundle([practitioner1, practitioner2]));
      const wrapper = mountComponent();
      await flushPromises();

      expect(wrapper.findAll('tbody tr')).toHaveLength(2);
    });

    test('displays practitioner name and ID', async () => {
      stubFetch(makeBundle([practitioner1]));
      const wrapper = mountComponent();
      await flushPromises();

      const cells = wrapper.find('tbody tr').findAll('td');
      expect(cells[0].text()).toBe('Jane Doe');
      expect(cells[1].text()).toBe('pr1');
    });

    test('renders no rows when the bundle has no entries', async () => {
      stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();

      expect(wrapper.findAll('tbody tr')).toHaveLength(0);
    });

    test('handles a bundle whose entry field is absent', async () => {
      const bundle = { resourceType: 'Bundle', total: 0, link: [] } as unknown as FhirPractitionerBundle;
      stubFetch(bundle);
      const wrapper = mountComponent();
      await flushPromises();

      expect(wrapper.findAll('tbody tr')).toHaveLength(0);
    });
  });

  describe('name filter', () => {
    test('appends name:contains param after debounce when filter text is typed', async () => {
      const fetchMock = stubFetch(makeBundle([practitioner1]));
      const wrapper = mountComponent();
      await flushPromises();
      fetchMock.mockClear();

      await wrapper.find('#practitioner-filter').setValue('Jane');
      vi.advanceTimersByTime(300);
      await flushPromises();

      expect(fetchMock).toHaveBeenCalledOnce();
      const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(url).toContain('name:contains=Jane');
    });

    test('does not fire before the 300 ms debounce elapses', async () => {
      const fetchMock = stubFetch(makeBundle([]));
      const wrapper = mountComponent();
      await flushPromises();
      fetchMock.mockClear();

      await wrapper.find('#practitioner-filter').setValue('Jo');
      vi.advanceTimersByTime(200);
      await flushPromises();

      expect(fetchMock).not.toHaveBeenCalled();
    });

    test('omits name:contains when the filter is cleared', async () => {
      const fetchMock = stubFetch(makeBundle([practitioner1, practitioner2]));
      const wrapper = mountComponent();
      await flushPromises();

      await wrapper.find('#practitioner-filter').setValue('Jane');
      vi.advanceTimersByTime(300);
      await flushPromises();
      fetchMock.mockClear();

      await wrapper.find('#practitioner-filter').setValue('');
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

      await wrapper.find('#practitioner-filter').setValue('Smith');
      vi.advanceTimersByTime(300);
      await flushPromises();

      const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(url).toContain('name:contains=Smith');
    });
  });

  describe('practitioner selection', () => {
    test('emits practitioner-selected with the correct practitioner when a row is clicked', async () => {
      stubFetch(makeBundle([practitioner1, practitioner2]));
      const wrapper = mountComponent();
      await flushPromises();

      await wrapper.findAll('tbody tr')[1].trigger('click');

      const emitted = wrapper.emitted('practitioner-selected');
      expect(emitted).toHaveLength(1);
      expect(emitted![0]).toEqual([practitioner2]);
    });

    test('emits practitioner-selected for the first row', async () => {
      stubFetch(makeBundle([practitioner1, practitioner2]));
      const wrapper = mountComponent();
      await flushPromises();

      await wrapper.find('tbody tr').trigger('click');

      expect(wrapper.emitted('practitioner-selected')![0]).toEqual([practitioner1]);
    });

    test('emits a separate event for each row click', async () => {
      stubFetch(makeBundle([practitioner1, practitioner2]));
      const wrapper = mountComponent();
      await flushPromises();

      const rows = wrapper.findAll('tbody tr');
      await rows[0].trigger('click');
      await rows[1].trigger('click');

      expect(wrapper.emitted('practitioner-selected')).toHaveLength(2);
    });
  });

  describe('active practitioner highlighting', () => {
    test('applies table-active to the row matching the selectedPractitioner prop', async () => {
      stubFetch(makeBundle([practitioner1, practitioner2]));
      const wrapper = mountComponent({ selectedPractitioner: practitioner1 });
      await flushPromises();

      const rows = wrapper.findAll('tbody tr');
      expect(rows[0].classes()).toContain('table-active');
      expect(rows[1].classes()).not.toContain('table-active');
    });

    test('applies table-active to the row containing the selected practitioner', async () => {
      stubFetch(makeBundle([practitioner1, practitioner2]));
      const wrapper = mountComponent({ selectedPractitioner: practitioner2 });
      await flushPromises();

      const rows = wrapper.findAll('tbody tr');
      expect(rows[0].classes()).not.toContain('table-active');
      expect(rows[1].classes()).toContain('table-active');
    });

    test('no row has table-active when selectedPractitioner is null', async () => {
      stubFetch(makeBundle([practitioner1, practitioner2]));
      const wrapper = mountComponent({ selectedPractitioner: null });
      await flushPromises();

      for (const row of wrapper.findAll('tbody tr')) {
        expect(row.classes()).not.toContain('table-active');
      }
    });
  });
});
