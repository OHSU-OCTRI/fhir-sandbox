import { createApp } from 'vue';
import MultiSelectUpdateTool from './components/MultiSelectUpdateTool.vue';

/**
 * In the controller add:
 *  ViewUtils.addPageScript(model, "account-selection.js");
 *
 * In the mustache view:
 * <div id="shared_accounts_management" data-entities="..."></div>
 */

const updateForm = document.querySelector('form[name="updateSharedAccountsForm"]');
const addSharingField = updateForm.querySelector('#add_sharing');
const removeSharingField = updateForm.querySelector('#remove_sharing');

const dataset = document.querySelector('#shared_accounts_management').dataset;
const accounts = JSON.parse(dataset.accounts.replaceAll("'", '"'));
const translations = [
  ...document.querySelectorAll('#shared_accounts_management .translation-wrapper')
].reduce((accumulator, current) => {
  accumulator[current.dataset.key] = current.dataset.value;
  return accumulator;
}, {});

const app = createApp(MultiSelectUpdateTool, {
  entities: accounts,
  translations: translations,
  saveSelectionCallback: (newSelections, removedSelections) => {
    addSharingField.value = newSelections.map(account => account.id).join(',');
    removeSharingField.value = removedSelections.map(account => account.id).join(',');
    updateForm.submit();
  }
});
app.mount('#shared_accounts_management');
