import { useLaunchStore } from '../stores/launchStore';
import { useApi } from './useApi';
import { usePersonaCookie } from './usePersonaCookie';
import type { SmartApp, LaunchData } from '../types';

export function useApps() {
  const store = useLaunchStore();
  const api = useApi();
  const { setPersonaCookie } = usePersonaCookie();

  async function loadApps(launchData: LaunchData): Promise<void> {
    const apps = await api.get<SmartApp[]>(store.registeredAppsUrl, store.sandboxApiHeaders);
    store.setLoadedApps(apps ?? []);
    if (launchData.appId) {
      const found = (apps ?? []).find(a => a.id === launchData.appId);
      if (found) store.setCurrentApp(found);
    }
  }

  async function launchApp(app: SmartApp, patientId?: string): Promise<void> {
    store.setCurrentApp(app);
    const persona = store.selectedPersona;
    const pid = patientId ?? store.selectedPatient?.resource.id;

    const body = {
      clientId: app.clientName,
      patientId: pid,
      fhirUser: store.selectedPersona?.fhirId
    };

    const data = await api.post<{ id?: string; error?: string }>(
      store.launchCodeUrl,
      store.sandboxApiHeaders,
      body
    );

    if (data?.id) {
      const url = `${app.launchUri}?iss=${store.fhirApi}/&launch=${data.id}`;
      store.setLaunchUrl(url);

      if (persona?.personaUserId != null) {
        try {
          const authResult = await api.post<{ jwt: string }>(
            store.personaAuthUrl,
            undefined,
            { username: persona.personaUserId, password: persona.password }
          );
          if (authResult?.jwt) setPersonaCookie(authResult.jwt);
        } catch {
          console.log('Persona authentication failed');
        }
      }
    }
  }

  return { loadApps, launchApp };
}
