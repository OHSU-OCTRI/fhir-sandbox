/**
 * Extracts CSRF token string from a meta tag if it is present.
 *
 * @returns CSRF token string; null if not found
 */
export default function getCsrfToken() {
  const metaTag = document.querySelector('meta[name="csrf"]');
  const csrfToken = metaTag?.getAttribute('content');
  if (!metaTag || !csrfToken) {
    console.error('CSRF token not found');
    return null;
  }

  return csrfToken;
}
