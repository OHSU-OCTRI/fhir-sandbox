package org.octri.fhir_sandbox.service;

import java.util.List;
import java.util.Map;

import org.octri.authentication.server.security.entity.User;
import org.octri.fhir_sandbox.domain.SmartClient;
import org.octri.fhir_sandbox.exception.DisplayedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Business logic for the standalone SMART on FHIR launch flow.
 */
@Service
public class StandaloneLaunchService {

	private final SmartClientService clientService;
	private final SandboxService sandboxService;
	private final PreAuthorizedTokenService preAuthorizedTokenService;
	private final AuthorizationServerSettings authorizationServerSettings;

	public StandaloneLaunchService(SmartClientService clientService, SandboxService sandboxService,
			PreAuthorizedTokenService preAuthorizedTokenService,
			AuthorizationServerSettings authorizationServerSettings) {
		this.clientService = clientService;
		this.sandboxService = sandboxService;
		this.preAuthorizedTokenService = preAuthorizedTokenService;
		this.authorizationServerSettings = authorizationServerSettings;
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
	 * @param clientId
	 *            OAuth2 client ID of the SMART client initiating the launch
	 * @param currentUser
	 *            authenticated user
	 * @throws DisplayedException
	 */
	public LaunchPickerData validateAndGetPickerData(String clientId, User currentUser) {
		var client = clientService.findSmartClientByClientId(clientId)
				.orElseThrow(() -> new DisplayedException(HttpStatus.NOT_FOUND, "SMART client not found"));

		var sandbox = client.getSandbox();
		if (!sandboxService.getSandboxesForUser(currentUser).contains(sandbox)) {
			throw new DisplayedException(HttpStatus.FORBIDDEN, "You do not have access to this sandbox");
		}

		var fhirServerUrl = sandboxService.getSandboxFhirUrl(sandbox);
		var accessToken = preAuthorizedTokenService.generateToken(Map.of(
				"sub", currentUser.getUsername(),
				"aud", fhirServerUrl,
				"scope", List.of("user/*.cruds")));

		return new LaunchPickerData(client, fhirServerUrl, accessToken);
	}

	/**
	 * Reconstructs the OAuth2 authorize URL from the original session parameters, appending the
	 * given {@code launchId} as the {@code launch} query parameter.
	 *
	 * @param contextPath
	 *            the servlet context path (e.g. {@code /fhir-sandbox})
	 * @param sessionParams
	 *            the original OAuth2 parameters stored in the session by
	 *            {@link org.octri.fhir_sandbox.filter.StandaloneLaunchFilter}
	 * @param launchId
	 *            the opaque launch context ID to append
	 * @return the fully-qualified authorize URL with all original parameters plus {@code launch}
	 */
	public String buildAuthorizeUrl(String contextPath, Map<String, String[]> sessionParams, String launchId) {
		var uriBuilder = UriComponentsBuilder.fromPath(contextPath + authorizationServerSettings.getAuthorizationEndpoint());
		sessionParams.forEach((k, v) -> uriBuilder.queryParam(k, (Object[]) v));
		uriBuilder.queryParam("launch", launchId);
		return uriBuilder.build().toUriString();
	}

}
