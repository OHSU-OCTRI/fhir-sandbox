import { createApp } from 'vue';
import getCsrfToken from './utils/getCsrfToken.ts';
import SandboxSharing from './components/SandboxSharing.vue';

/**
 * In the controller add:
 *  ViewUtils.addPageScript(model, "shared-account-selector.js");
 *
 * In the mustache view:
 * <div id="shared_users_selector"
 *       data-get-endpoint="..."
 *       data-post-endpoint="...">
 *   ...
 * </div>
 */

const mount = document.querySelector('#shared_users_selector');

const app = createApp(SandboxSharing, {
  endpoint: mount.dataset.endpoint,
  csrfToken: getCsrfToken()
});
app.mount('#shared_users_selector');
