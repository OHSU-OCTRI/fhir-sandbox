import { useLaunchStore } from '../stores/launchStore'
import { useApi } from './useApi'
import type { CdsCard, CdsHookRequest } from '../types'

const CHARS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'

function randomString(length: number): string {
  let result = ''
  for (let i = length; i > 0; --i) {
    result += CHARS[Math.round(Math.random() * (CHARS.length - 1))]
  }
  return result
}

function getCookieValue(name: string): string | undefined {
  const nameEq = name + '='
  const decoded = decodeURIComponent(document.cookie)
  for (const part of decoded.split(';')) {
    const c = part.trimStart()
    if (c.startsWith(nameEq)) return c.substring(nameEq.length)
  }
  return undefined
}

export function useCDSHooks() {
  const store = useLaunchStore()
  const api = useApi()

  async function triggerHooks(patientId: string): Promise<void> {
    store.setCards([])

    const hookInstance = randomString(64)
    const servicesRaw = getCookieValue('hspc-hooks-list')
    const tokenRaw = getCookieValue('hspc-launch-token')

    if (!servicesRaw || !tokenRaw) return

    const services = JSON.parse(servicesRaw) as Array<{
      cdsHooks: Array<{
        hook: string
        hookUrl: string
        prefetch?: Record<string, string>
      }>
    }>
    const token = JSON.parse(tokenRaw) as { sandboxApiUrl: string }
    const persona = store.selectedPersona

    for (const service of services) {
      for (const hook of service.cdsHooks) {
        if (hook.hook !== 'patient-view') continue
        if (!persona) continue

        const context: Record<string, string> = {
          patientId,
          userId: persona.fhirId,
        }

        const authData = await api.post<{ jwt: string }>(
          `${window.location.protocol}//${token.sandboxApiUrl}/userPersona/authenticate`,
          undefined,
          { username: persona.personaUserId, password: persona.password },
        )

        if (!authData) continue

        const requestData: CdsHookRequest = {
          hookInstance,
          hook: hook.hook,
          fhirServer: `${window.location.protocol}//${store.fhirApi}/${store.sandboxId}`,
          context,
          fhirAuthorization: {
            access_token: store.bearerToken,
            token_type: 'Bearer',
            scope: 'patient/*.read user/*.read',
            subject: hook.hook,
          },
          prefetch: {},
        }

        if (hook.prefetch) {
          const keys = Object.keys(hook.prefetch)
          await Promise.all(
            keys.map(async (key) => {
              const template = hook.prefetch![key]
              const url = template.replace(
                /\{\{context\.(.*?)\}\}/gi,
                (_: string, b: string) => context[b] ?? '',
              )
              const result = await api.get(
                `${window.location.protocol}//${store.fhirApi}/${store.sandboxId}/data/${encodeURI(url)}`,
                store.fhirApiHeaders,
              )
              requestData.prefetch[key] = result
            }),
          )
        }

        const response = await api.post<{ cards?: CdsCard[] }>(
          encodeURI(hook.hookUrl),
          undefined,
          requestData,
        )

        if (response?.cards) {
          const tagged = response.cards.map((card) => ({ ...card, requestData }))
          store.appendCards(tagged)
        } else if (response) {
          store.appendCards([
            { noCardsReturned: true, summary: '', indicator: 'info', requestData },
          ])
        }
      }
    }
  }

  return { triggerHooks }
}
