/**
 * Provides UI components to allow admins to view a translation key for site content and navigate
 * to the admin screens for managing that content.
 *
 * Note that the `octri.i18n.content-editing-enabled` property must be set to true.
 * With this property enabled, the `LocalizationMessageInterceptor` class wraps all translation
 * text in a `span` element with the following format:
 *
 * <span class="translation-key" title="{{key}}" data-url="{{path}}">{{text}}</span>;
 */

(function () {
    'use strict';

    const translationItemClass = 'translation-key';
    const toggleSelectorId = 'toggleTranslationNav';
    const storageKey = 'translationNavEnabled';
    const trueValue = 'true';
    const falseValue = 'false';
    const defaultValue = falseValue;

    /**
     * Initialize the storage.
     */
    function initStorage() {
      const setting = sessionStorage.getItem(storageKey);
      if (!setting) {
        sessionStorage.setItem(storageKey, defaultValue);
      }
    }

    /**
     * @returns - bool indicating whether navigation to manage a translation key is enabled.
     */
    function navigationEnabled() {
      const setting = sessionStorage.getItem(storageKey);
      return setting && setting === trueValue;
    }

    /**
     * Toggles the setting for navigation to a translation management route.
     */
    function toggleNavigationEnabled() {
      if (navigationEnabled()) {
        sessionStorage.setItem(storageKey, falseValue);
      } else {
        sessionStorage.setItem(storageKey, trueValue);
      }
    }

    /**
     * Add a UI component to allow a user to navigate to the url for managing this translation.
     * Navigation happens by invoking the contex menu on an element ('right-clicking').
     */
    function addHandle(element) {
      const link = element.dataset.url;
      element.addEventListener('contextmenu', event => {
        if (navigationEnabled()) {
          event.preventDefault();
          window.location = link;
        }
      });
    }

    /**
     * Add handles for all translation items on the page.
     */
    function addHandles() {
      const items = document.getElementsByClassName(translationItemClass);
      for (let i = 0; i < items.length; i++) {
        addHandle(items[i]);
      }
    }

    /**
     * Add events to the input for toggling the direct navigation behavior.
     */
    function manageNavigationToggleInput() {
      const checkbox = document.getElementById(toggleSelectorId);
      if (checkbox) {
        if (navigationEnabled()) {
          checkbox.checked = 'checked';
        }
        checkbox.addEventListener('change', () => {
          toggleNavigationEnabled();
        });
      }
    }

    window.addEventListener(
      'load',
      function () {
        initStorage();
        manageNavigationToggleInput();
        addHandles();
      },
      false
    );
  })();
