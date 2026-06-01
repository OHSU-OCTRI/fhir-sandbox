import { useLaunchStore } from '../stores/launchStore';
import { useApi } from './useApi';
import type { SmartApp, LaunchData } from '../types';

export function useApps() {
  const store = useLaunchStore();
  const api = useApi();

  async function loadApps(launchData: LaunchData): Promise<void> {
    const apps = await api.get<SmartApp[]>(store.registeredAppsUrl, store.sandboxApiHeaders);
    store.setLoadedApps(apps ?? []);
    if (launchData.appId) {
      const found = (apps ?? []).find(a => a.id === launchData.appId);
      if (found) store.setCurrentApp(found);
    }
  }

  return { loadApps };
}
