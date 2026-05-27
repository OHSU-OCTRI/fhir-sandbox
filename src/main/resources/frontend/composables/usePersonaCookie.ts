import Cookies from 'js-cookie'

const PERSONA_COOKIE = 'hspc-persona-token'

export function usePersonaCookie() {
  function getBaseUrl(): string {
    return window.location.host.split(':')[0].split('.').slice(-2).join('.')
  }

  function setPersonaCookie(jwt: string): void {
    const date = new Date()
    date.setTime(date.getTime() + 24 * 60 * 60 * 1000)
    Cookies.set(PERSONA_COOKIE, jwt, { path: '/', expires: date, domain: getBaseUrl() })
  }

  function removePersonaCookie(): void {
    Cookies.remove(PERSONA_COOKIE, { path: '/', domain: getBaseUrl() })
  }

  return { setPersonaCookie, removePersonaCookie }
}
