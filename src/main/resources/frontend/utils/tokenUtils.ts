/**
 * Returns a header map as expected by useApi containing a bearer token Authorization header with the given token value.
 *
 * @param token bearer token string
 * @returns header map
 */
export function bearerTokenHeader(token: string): Record<string, string> {
  return { Authorization: `Bearer ${token}` };
}

/**
 * Returns a header map as expected by useApi containing an X-CSRF-Token header with the given token value.
 *
 * @param token CSRF token string
 * @returns header map
 */
export function csrfTokenHeader(token: string): Record<string, string> {
  return { 'X-CSRF-Token': token };
}
