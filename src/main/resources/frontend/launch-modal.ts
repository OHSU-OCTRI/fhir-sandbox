import { createApp } from 'vue';
import { createPinia } from 'pinia';

import getCsrfToken from './utils/getCsrfToken.ts';
import LaunchModal from './components/LaunchModal.vue';

// The application root is only present if the sandbox has clients defined for it
const root = document.getElementById('launcher-app');
if (root) {
  const csrfToken = getCsrfToken();
  const app = createApp(LaunchModal, {
    fhirApi: root?.dataset.fhirApi,
    sandboxApi: window.origin + root?.dataset.sandboxApi,
    sandboxId: root?.dataset.sandboxId,
    bearerToken: root?.dataset.token,
    csrfToken: csrfToken
  });
  app.use(createPinia());
  app.mount('#launcher-app');
} else {
  console.info('Root element is not present.');
}
