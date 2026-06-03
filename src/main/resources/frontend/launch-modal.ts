import { createApp } from 'vue';
import { createPinia } from 'pinia';

import getCsrfToken from './utils/getCsrfToken.ts';
import LaunchModal from './components/LaunchModal.vue';

const root = document.getElementById('launcher-app');
if (!root) {
  console.error('Application root element not found');
}

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