/**
 * Functionality to designate an anchor tag or button as a control which can copy embedded
 * text to the clipboard.
 *
 * Ex.
 * <a href="#" class="copy-text-control" data-text="Hello, world">Copy to clipboard</a>
 */
(function () {
  'use strict';

  const selector = '.copy-text-control';

  async function writeClipboardText(text, alertUser) {
    try {
      await navigator.clipboard.writeText(text);
      if (alertUser) {
        alert('Copied to clipboard!');
      }
    } catch (error) {
      console.error(error.message);
    }
  }

  function initClipBoardFn() {
    document.querySelectorAll(selector).forEach(el => {
      const url = el.dataset.text;
      el.addEventListener('click', event => {
        event.preventDefault();
        writeClipboardText(url, true);
      });
    });
  }

  window.addEventListener('load', function () {
    initClipBoardFn();
  });
})();