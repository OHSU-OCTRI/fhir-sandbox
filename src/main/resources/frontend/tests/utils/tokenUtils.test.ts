import { describe, expect, test } from 'vitest';
import { bearerTokenHeader, csrfTokenHeader } from '../../utils/tokenUtils';

describe('bearerTokenHeader', () => {
  test('returns an Authorization header with a Bearer token', () => {
    expect(bearerTokenHeader('abc123')).toEqual({ Authorization: 'Bearer abc123' });
  });

  test('includes the full token value in the header', () => {
    const token = 'eyJhbGciOiJSUzI1NiJ9.payload.signature';
    expect(bearerTokenHeader(token)).toEqual({ Authorization: `Bearer ${token}` });
  });

  test('handles an empty token string', () => {
    expect(bearerTokenHeader('')).toEqual({ Authorization: 'Bearer ' });
  });
});

describe('csrfTokenHeader', () => {
  test('returns an X-CSRF-Token header with the given token', () => {
    expect(csrfTokenHeader('csrf-token-value')).toEqual({ 'X-CSRF-Token': 'csrf-token-value' });
  });

  test('includes the full token value in the header', () => {
    const token = 'a1b2c3d4e5f6';
    expect(csrfTokenHeader(token)).toEqual({ 'X-CSRF-Token': token });
  });

  test('handles an empty token string', () => {
    expect(csrfTokenHeader('')).toEqual({ 'X-CSRF-Token': '' });
  });
});
