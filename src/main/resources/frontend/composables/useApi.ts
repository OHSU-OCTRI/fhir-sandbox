async function request<T>(
  url: string,
  method: string,
  headers?: HeadersInit,
  body?: unknown,
  credentials?: RequestCredentials
): Promise<T | null> {
  let mergedHeaders: HeadersInit = {
    Accept: 'application/json',
    'Content-Type': 'application/json;charset=UTF-8',
  };

  if (headers) {
    mergedHeaders = { ...mergedHeaders, ...headers };
  }

  const init: RequestInit = { method, headers: mergedHeaders };
  if (body) {
    init.body = JSON.stringify(body);
  }

  if (credentials) {
    init.credentials = credentials;
  }

  const response = await fetch(url, init);
  if (!response.ok) {
    console.error(`API error ${response.status} for ${url}`);
    return null;
  }
  return response.json() as Promise<T>;
}

/**
 * Returns a typed wrapper for JSON requests using the fetch API.
 *
 * @returns typed wrapper for the fetch API
 */
export function useApi() {
  return {
    get<T>(url: string, headers?: HeadersInit, credentials?: RequestCredentials): Promise<T | null> {
      return request<T>(url, 'GET', headers, undefined, credentials);
    },
    post<T>(url: string, headers?: HeadersInit, body?: unknown, credentials?: RequestCredentials): Promise<T | null> {
      return request<T>(url, 'POST', headers, body, credentials);
    },
    put<T>(url: string, headers?: HeadersInit, body?: unknown, credentials?: RequestCredentials): Promise<T | null> {
      return request<T>(url, 'PUT', headers, body, credentials);
    },
    delete<T>(url: string, headers?: HeadersInit, credentials?: RequestCredentials): Promise<T | null> {
      return request<T>(url, 'DELETE', headers, undefined, credentials);
    }
  };
}
