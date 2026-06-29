package org.octri.fhir_sandbox.service;

import java.util.List;
import java.util.Map;

import org.octri.authentication.server.security.entity.User;
import org.octri.fhir_sandbox.domain.SmartClient;
import org.octri.fhir_sandbox.exception.DisplayedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Business logic for the standalone SMART on FHIR launch flow.
 */
@Service
public class StandaloneLaunchService {

    private final SmartClientService clientService;
    private final SandboxService sandboxService;
    private final PreAuthorizedTokenService preAuthorizedTokenService;

    public StandaloneLaunchService(SmartClientService clientService, SandboxService sandboxService,
            PreAuthorizedTokenService preAuthorizedTokenService) {
        this.clientService = clientService;
        this.sandboxService = sandboxService;
        this.preAuthorizedTokenService = preAuthorizedTokenService;
    }

    /**
     * Data needed to render the patient/practitioner picker page.
     */
    public record LaunchPickerData(SmartClient client, String fhirServerUrl, String accessToken) {
    }

    /**
     * Validates that the given client exists and that {@code currentUser} has access to its sandbox,
     * then returns the data needed to render the picker page.
     *
     * @param clientId    OAuth2 client ID of the SMART client initiating the launch
     * @param currentUser authenticated user
     * @param username    username used as the {@code sub} claim in the pre-authorized bearer token
     * @throws DisplayedException with 404 if the client is not found, or 403 if the user lacks access
     */
    public LaunchPickerData validateAndGetPickerData(String clientId, User currentUser, String username) {
        var client = clientService.findSmartClientByClientId(clientId)
                .orElseThrow(() -> new DisplayedException(HttpStatus.NOT_FOUND, "SMART client not found"));

        var sandbox = client.getSandbox();
        if (!sandboxService.getSandboxesForUser(currentUser).contains(sandbox)) {
            throw new DisplayedException(HttpStatus.FORBIDDEN, "You do not have access to this sandbox");
        }

        var fhirServerUrl = sandboxService.getSandboxFhirUrl(sandbox);
        var accessToken = preAuthorizedTokenService.generateToken(Map.of(
                "sub", username,
                "aud", fhirServerUrl,
                "scope", List.of("user/*.cruds")));

        return new LaunchPickerData(client, fhirServerUrl, accessToken);
    }

}
