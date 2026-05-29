import { createApp } from 'vue';
import MultiSelectUpdateTool from './components/MultiSelectUpdateTool.vue';

/**
 * In the controller add:
 *  ViewUtils.addPageScript(model, "account-selection.js");
 *
 * In the mustache view:
 * <div id="shared_accounts_management" data-entities="..."></div>
 */

const mock_accounts = {
  selected: [
    {
      id: 2,
      label: 'user2'
    }
  ],
  available: [
    {
      id: 1,
      label: 'user1'
    },
    {
      id: 3,
      label: 'user3'
    },
    {
      id: 4,
      label: 'flimflamthemagicman'
    }
  ]
};

// const dataset = document.querySelector('#shared_accounts_management').dataset;
const translations = [
  ...document.querySelectorAll('#shared_accounts_management .translation-wrapper')
].reduce((accumulator, current) => {
  accumulator[current.dataset.key] = current.dataset.value;
  return accumulator;
}, {});

const app = createApp(MultiSelectUpdateTool, {
  entities: mock_accounts,
  translations: translations,
  saveSelectionCallback: (added, removed) => {
    alert(`Added: ${JSON.stringify(added)}`);
    alert(`Removed: ${JSON.stringify(removed)}`);
  }
});
app.mount('#shared_accounts_management');
