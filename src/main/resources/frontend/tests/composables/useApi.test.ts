import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import { useApi } from '../../composables/useApi';

describe('useApi', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  function mockFetch(body: unknown, ok = true, status = 200) {
    vi.mocked(fetch).mockResolvedValue({
      ok,
      status,
      json: () => Promise.resolve(body),
    } as Response);
  }

  function mockAuthHeaders(): HeadersInit {
    return {
      Authorization: 'Bearer my-token'
    };
  }

  describe('get', () => {
    test('sends a GET request to the given URL', async () => {
      mockFetch({ id: 1 });
      const api = useApi();
      await api.get('/api/resource');
      expect(fetch).toHaveBeenCalledWith('/api/resource', expect.objectContaining({ method: 'GET' }));
    });

    test('returns parsed JSON on success', async () => {
      mockFetch({ id: 42 });
      const api = useApi();
      const result = await api.get<{ id: number }>('/api/resource');
      expect(result).toEqual({ id: 42 });
    });

    test('includes custom headers when provided', async () => {
      mockFetch({});
      const api = useApi();
      await api.get('/api/resource', mockAuthHeaders());
      expect(fetch).toHaveBeenCalledWith(
        '/api/resource',
        expect.objectContaining({
          headers: expect.objectContaining({ Authorization: 'Bearer my-token' })
        })
      );
    });

    test('returns null and logs an error when response is not ok', async () => {
      mockFetch(null, false, 404);
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const api = useApi();
      const result = await api.get('/api/missing');
      expect(result).toBeNull();
      expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining('404'));
      consoleSpy.mockRestore();
    });
  });

  describe('post', () => {
    test('sends a POST request with a JSON body', async () => {
      mockFetch({ created: true });
      const api = useApi();
      await api.post('/api/resource', mockAuthHeaders(), { name: 'test' });
      expect(fetch).toHaveBeenCalledWith(
        '/api/resource',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ name: 'test' }),
        })
      );
    });

    test('sends a POST request without a body when none is provided', async () => {
      mockFetch({ created: true });
      const api = useApi();
      await api.post('/api/resource');
      const [, init] = vi.mocked(fetch).mock.calls[0];
      expect(init?.body).toBeUndefined();
    });

    test('includes custom headers when provided', async () => {
      mockFetch({ created: true });
      const api = useApi();
      await api.post('/api/resource', mockAuthHeaders(), { name: 'test' });
      expect(fetch).toHaveBeenCalledWith(
        '/api/resource',
        expect.objectContaining({
          headers: expect.objectContaining({ Authorization: 'Bearer my-token'})
        })
      );
    });

    test('returns null when response is not ok', async () => {
      mockFetch(null, false, 500);
      vi.spyOn(console, 'error').mockImplementation(() => {});
      const api = useApi();
      const result = await api.post('/api/resource');
      expect(result).toBeNull();
    });
  });

  describe('put', () => {
    test('sends a PUT request with a JSON body', async () => {
      mockFetch({ updated: true });
      const api = useApi();
      await api.put('/api/resource/1', mockAuthHeaders(), { name: 'updated' });
      expect(fetch).toHaveBeenCalledWith(
        '/api/resource/1',
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ name: 'updated' }),
        })
      );
    });

    test('includes custom headers when provided', async () => {
      mockFetch({ updated: true });
      const api = useApi();
      await api.put('/api/resource/1', mockAuthHeaders(), { name: 'updated' });
      expect(fetch).toHaveBeenCalledWith(
        '/api/resource/1',
        expect.objectContaining({
          headers: expect.objectContaining({ Authorization: 'Bearer my-token' })
        })
      );
    });

    test('returns parsed JSON on success', async () => {
      mockFetch({ updated: true });
      const api = useApi();
      const result = await api.put<{ updated: boolean }>('/api/resource/1');
      expect(result).toEqual({ updated: true });
    });
  });

  describe('delete', () => {
    test('sends a DELETE request to the given URL', async () => {
      mockFetch({ deleted: true });
      const api = useApi();
      await api.delete('/api/resource/1', mockAuthHeaders());
      expect(fetch).toHaveBeenCalledWith(
        '/api/resource/1',
        expect.objectContaining({ method: 'DELETE' })
      );
    });

    test('includes custom headers when provided', async () => {
      mockFetch({});
      const api = useApi();
      await api.delete('/api/resource/1', mockAuthHeaders());
      expect(fetch).toHaveBeenCalledWith(
        '/api/resource/1',
        expect.objectContaining({
          headers: expect.objectContaining({ Authorization: 'Bearer my-token' }),
        })
      );
    });

    test('returns null when response is not ok', async () => {
      mockFetch(null, false, 403);
      vi.spyOn(console, 'error').mockImplementation(() => {});
      const api = useApi();
      const result = await api.delete('/api/resource/1');
      expect(result).toBeNull();
    });
  });

  describe('common headers', () => {
    test('always sends Accept and Content-Type headers', async () => {
      mockFetch({});
      const api = useApi();
      await api.get('/api/resource');
      expect(fetch).toHaveBeenCalledWith(
        '/api/resource',
        expect.objectContaining({
          headers: expect.objectContaining({
            Accept: 'application/json',
            'Content-Type': 'application/json;charset=UTF-8',
          }),
        })
      );
    });
  });
});
