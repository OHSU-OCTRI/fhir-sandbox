(function () {
  'use strict';
  window.addEventListener('load', function () {
    const buttons = document.querySelectorAll('a.client-launch');
    buttons.forEach(function (btn) {
      btn.addEventListener('click', function (evt) {
        evt.preventDefault();
        const { clientId, patientId, launchUrl, fhirUser } = this.dataset;
        const contextPath = document
          .querySelector('meta[name="ctx"]')
          ?.getAttribute('content');
        const csrfToken = document
          .querySelector('meta[name="csrf"]')
          ?.getAttribute('content');

        if (!clientId || !patientId || !launchUrl) {
          console.error('Required request parameters are missing');
          return;
        }

        if (!contextPath) {
          console.error('Context path not found');
          return;
        }

        if (!csrfToken) {
          console.error('CSRF token not found');
          return;
        }

        const requestBody = { clientId, patientId };
        if (fhirUser) {
          requestBody.fhirUser = fhirUser;
        }

        fetch(`${contextPath}create_context`, {
          method: 'POST',
          credentials: 'same-origin',
          headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
          },
          body: JSON.stringify(requestBody)
        })
          .then(response => response.json())
          .then(json => {
            if (json.id) {
              const parsedUrl = new URL(launchUrl);
              parsedUrl.searchParams.set('launch', json.id);
              window.open(parsedUrl.href, '_blank');
            } else {
              alert('Could not launch client: ' + json.error);
            }
          });
      });
    });
  });
})();
