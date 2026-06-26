import { createApp } from 'vue';

import getCsrfToken from './utils/getCsrfToken.ts';
import StandaloneLauncher from './components/StandaloneLauncher.vue';

const root = document.getElementById('standalone-launcher-app');
if (root) {
  const csrfToken = getCsrfToken() ?? '';

  const ctxMeta = document.querySelector<HTMLMetaElement>('meta[name="ctx"]');
  const ctx = ctxMeta?.content?.replace(/\/$/, '') ?? '';
  const completeUrl = `${window.location.origin}${ctx}/smart/standalone-launch/complete`;

  const app = createApp(StandaloneLauncher, {
    fhirApi: root.dataset.fhirApi ?? '',
    accessToken: root.dataset.accessToken ?? '',
    clientId: root.dataset.clientId ?? '',
    sessionKey: root.dataset.sessionKey ?? '',
    csrfToken,
    completeUrl,
  });
  app.mount('#standalone-launcher-app');
} else {
  console.info('Standalone launcher root element not found.');
}
