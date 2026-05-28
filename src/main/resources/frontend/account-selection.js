import { createApp } from 'vue';
import EntityMultiSelect from './components/EntityMultiSelect.vue';

/**
 * In the controller add:
 *  model.put("pageScripts", new String[] { "vendor.js", "account-selection.js" });
 *
 * In the mustache view:
 * <div id="account_multi_selector" data-content="<p>Hello world</p>" data-editable="true"></div>
 */

alert('Mounting app');
const mock_accounts = [
  {
    id: 1,
    label: 'user1',
    isSelected: false
  },
  {
    id: 2,
    label: 'user2',
    isSelected: true
  },
  {
    id: 3,
    label: 'user3',
    isSelected: false
  }
];

// const dataset = document.querySelector('#account_multi_selector').dataset;
const app = createApp(EntityMultiSelect, {
  accounts: mock_accounts,
  selectionChangeCallback: selected => {
    alert(JSON.stringify(selected));
  }
});
app.mount('#account_multi_selector');
