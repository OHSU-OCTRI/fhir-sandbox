import { createApp } from 'vue';
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
const csrfTokenInput = mount.querySelector('input[name="_csrf"]');

const app = createApp(SandboxSharing, {
  getEndpoint: mount.dataset.getEndpoint,
  postEndpoint: mount.dataset.postEndpoint,
  csrfToken: csrfTokenInput ? csrfTokenInput.value : ''
});
app.mount('#shared_users_selector');
