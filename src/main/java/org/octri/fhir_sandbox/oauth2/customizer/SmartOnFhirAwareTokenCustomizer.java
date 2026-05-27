package org.octri.fhir_sandbox.oauth2.customizer;

import java.util.ArrayList;
import java.util.Optional;

import org.octri.fhir_sandbox.domain.SmartLaunchContext;
import org.octri.fhir_sandbox.oauth2.utils.OAuthUtils;
import org.octri.fhir_sandbox.service.SandboxService;
import org.octri.fhir_sandbox.service.SmartClientService;
import org.octri.fhir_sandbox.service.SmartLaunchContextService;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.util.Assert;

/**
 * Token customizer that adds SMART app information to OAuth tokens. Customizes access tokens to add an audience ("aud")
 * claim set to the FHIR server URL and add a custom <code>launchContext</code> claim if a launch context is associated
 * with the authorization. Adds the SMART app launch context's <code>fhirUser</code> attribute to the OIDC ID token if
 * the corresponding scope was requested.
 */
public class SmartOnFhirAwareTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

	private final SandboxService sandboxService;
	private final SmartClientService clientService;
	private final SmartLaunchContextService contextService;

	public SmartOnFhirAwareTokenCustomizer(SandboxService sandboxService, SmartClientService clientService,
			SmartLaunchContextService contextService) {
		this.sandboxService = sandboxService;
		this.clientService = clientService;
		this.contextService = contextService;
	}

	@Override
	public void customize(JwtEncodingContext context) {
		var authorization = context.getAuthorization();

		if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
			var launchId = OAuthUtils.getLaunchIdFromAuthorization(authorization);
			if (launchId == null) {
				return;
			}

			var optLaunchContext = getLaunchContext(launchId, authorization.getRegisteredClientId());
			if (optLaunchContext.isPresent()) {
				var launchContext = optLaunchContext.get();
				var hasFhirUserScope = context.getAuthorizedScopes()
						.contains(SmartLaunchContext.FHIR_USER_ATTRIBUTE);
				if (hasFhirUserScope && launchContext.getFhirUserAttribute() != null) {
					context.getClaims().claim("fhirUser", launchContext.getFhirUserAttribute());
				}
			}
		}

		if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
			var clientId = authorization.getRegisteredClientId();
			var optClient = clientService.findSmartClientByClientId(clientId);
			Assert.isTrue(optClient.isPresent(),
					"Could not find client with client ID " + clientId + " when customizing access token");
			var sandbox = optClient.get().getSandbox();
			Assert.notNull(sandbox,
					"Could not get sandbox for client with client ID " + clientId + " when customizing access token");
			var fhirUrl = sandboxService.getSandboxFhirUrl(sandbox);
			var audience = new ArrayList<String>();
			audience.add(fhirUrl);
			context.getClaims().audience(audience);

			var launchId = OAuthUtils.getLaunchIdFromAuthorization(authorization);
			if (launchId != null) {
				var optLaunchContext = getLaunchContext(launchId, authorization.getRegisteredClientId());
				if (optLaunchContext.isEmpty()) {
					return;
				}

				var launchContext = optLaunchContext.get();
				context.getClaims().claim("launchContext", launchContext.toMap());
			}
		}
	}

	private Optional<SmartLaunchContext> getLaunchContext(String opaqueId, String registeredClientId) {
		return contextService.findByOpaqueIdAndClientId(opaqueId, registeredClientId);
	}

}
