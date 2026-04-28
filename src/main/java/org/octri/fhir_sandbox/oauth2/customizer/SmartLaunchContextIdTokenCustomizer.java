package org.octri.fhir_sandbox.oauth2.customizer;

import org.octri.fhir_sandbox.oauth2.utils.OAuthUtils;
import org.octri.fhir_sandbox.service.SmartLaunchContextService;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * Customizer that adds the SMART app launch context's <code>fhirUser</code> attribute to the OIDC ID token if present.
 */
public class SmartLaunchContextIdTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

	private final SmartLaunchContextService service;

	public SmartLaunchContextIdTokenCustomizer(SmartLaunchContextService service) {
		this.service = service;
	}

	@Override
	public void customize(JwtEncodingContext context) {
		if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
			var authorization = context.getAuthorization();
			var launchId = OAuthUtils.getLaunchIdFromAuthorization(authorization);
			if (launchId == null) {
				return;
			}

			var optLaunchContext = service.findByOpaqueIdAndClientId(launchId,
					authorization.getRegisteredClientId());
			if (optLaunchContext.isPresent()) {
				var launchContext = optLaunchContext.get();
				if (launchContext.getFhirUserAttribute() != null) {
					context.getClaims().claim("fhirUser", launchContext.getFhirUserAttribute());
				}
			}
		}
	}

}
