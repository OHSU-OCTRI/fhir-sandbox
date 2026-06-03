import { createApp } from 'vue';
import ManagedContentEditor from './components/ManagedContentEditor.vue';

/**
 * In the controller add:
 *  model.put("pageScripts", new String[] { "vendor.js", "managed-content.js" });
 *
 * In the mustache view:
 * <div id="managed_content" data-content="<p>Hello world</p>" data-editable="true"></div>
 */

const dataset = document.querySelector('#managed_content').dataset;
const app = createApp(ManagedContentEditor, {
  content: dataset.content,
  editable: dataset.editable
});
app.mount('#managed_content');
