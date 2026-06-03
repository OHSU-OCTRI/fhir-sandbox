import { createApp } from 'vue';
import MultiSelectTool from './components/MultiSelectTool.vue';

/**
 * In the controller add:
 *  ViewUtils.addPageScript(model, "shared-account-selector.js");
 *  model.put("selectedAccountsJson", ...);
 *  model.put("availableAccountsJson", ...);
 *  model.put("updateSharingAction", {{ POST_ROUTE }});
 *
 * In the mustache view:
 * <div id="shared_accounts_selector"
 *       data-selected="{{selectedAccountsJson}}"
 *       data-available="{{availableAccountsJson}}">
 *   <div class="translation-wrapper" data-key="..." data-value="..."></div>
 *   ...
 * </div>
 *
 * Note: a list of divs with the .translation-wrapper class can be defined beneath the mounting div. These use data
 * attributes to pass one key-value pair per element to the Vue application.
 */

// Process data attributes used for props
const dataset = document.querySelector('#shared_accounts_selector').dataset;
const selected = JSON.parse(dataset.selected.replaceAll("'", '"'));
const available = JSON.parse(dataset.available.replaceAll("'", '"'));
const translations = [
  ...document.querySelectorAll('#shared_accounts_selector .translation-wrapper')
].reduce((accumulator, current) => {
  accumulator[current.dataset.key] = current.dataset.value;
  return accumulator;
}, {});

const app = createApp(MultiSelectTool, {
  selected: selected,
  available: available,
  translations: translations,
  submitSelectionCallback: (newSelections, removedSelections) => {
    // Update the hidden form fields and submit
    const updateForm = document.querySelector('form[name="updateSharedAccountsForm"]');
    const addSharingField = updateForm.querySelector('#add_sharing');
    const removeSharingField = updateForm.querySelector('#remove_sharing');

    addSharingField.value = newSelections.map(account => account.id).join(',');
    removeSharingField.value = removedSelections.map(account => account.id).join(',');
    updateForm.submit();
  }
});
app.mount('#shared_accounts_selector');
