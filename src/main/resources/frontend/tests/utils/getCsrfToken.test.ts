import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import getCsrfToken from '../../utils/getCsrfToken';

describe('getCsrfToken', () => {
  let consoleSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    document.head.innerHTML = '';
  });

  afterEach(() => {
    consoleSpy.mockRestore();
  });

  test('returns the CSRF token when the meta tag is present', () => {
    document.head.innerHTML = '<meta name="csrf" content="test-csrf-token">';
    expect(getCsrfToken()).toBe('test-csrf-token');
  });

  test('returns null and logs an error when the meta tag is absent', () => {
    expect(getCsrfToken()).toBeNull();
    expect(consoleSpy).toHaveBeenCalledWith('CSRF token not found');
  });

  test('returns null and logs an error when the meta tag has no content attribute', () => {
    document.head.innerHTML = '<meta name="csrf">';
    expect(getCsrfToken()).toBeNull();
    expect(consoleSpy).toHaveBeenCalledWith('CSRF token not found');
  });

  test('returns null and logs an error when the content attribute is empty', () => {
    document.head.innerHTML = '<meta name="csrf" content="">';
    expect(getCsrfToken()).toBeNull();
    expect(consoleSpy).toHaveBeenCalledWith('CSRF token not found');
  });
});
