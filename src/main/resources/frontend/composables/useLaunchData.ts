import type { LaunchData } from '../types'
import { usePersonaCookie } from './usePersonaCookie'

const COOKIE_NAME = 'hspc-launch-token='

export function useLaunchData(): { getLaunchData: () => LaunchData | null } {
  const { removePersonaCookie } = usePersonaCookie()

  function getLaunchData(): LaunchData | null {
    const decoded = decodeURIComponent(document.cookie)

    if (decoded.indexOf(COOKIE_NAME) >= 0) {
      const parts = decoded.split(';')
      let raw: string | null = null
      for (const part of parts) {
        const trimmed = part.trimStart()
        if (trimmed.startsWith(COOKIE_NAME)) {
          raw = trimmed.substring(COOKIE_NAME.length)
          break
        }
      }
      if (raw) {
        sessionStorage.setItem('launchData', raw)
        removePersonaCookie()
        return JSON.parse(raw) as LaunchData
      }
    }

    const stored = sessionStorage.getItem('launchData')
    if (stored) {
      return JSON.parse(stored) as LaunchData
    }

    return null
  }

  return { getLaunchData }
}
