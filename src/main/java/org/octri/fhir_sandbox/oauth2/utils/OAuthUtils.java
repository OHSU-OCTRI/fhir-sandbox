package org.octri.fhir_sandbox.oauth2.utils;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.util.Assert;

/**
 * Utilities for working with OAuth authorization requests.
 */
public class OAuthUtils {

	public static final String LAUNCH_PARAMETER_NAME = "launch";

	/**
	 * Returns the SMART launch context ID for the given {@link OAuth2Authorization}.
	 *
	 * @param authorization
	 * @return
	 */
	public static String getLaunchIdFromAuthorization(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "Authorization is required");

		var authRequest = authorization.getAttributes().get(OAuth2AuthorizationRequest.class.getCanonicalName());
		Assert.notNull(authRequest,
				"No authorization request found in attributes for authorization " + authorization.getId());
		Assert.isTrue(authRequest instanceof OAuth2AuthorizationRequest,
				"Expected OAuth2Authorization request, but found " + authRequest.getClass().getName());

		var authRequestAdditionalParams = ((OAuth2AuthorizationRequest) authRequest).getAdditionalParameters();
		var launchId = authRequestAdditionalParams.get(LAUNCH_PARAMETER_NAME);
		if (launchId == null || !(launchId instanceof String)) {
			return null;
		}

		return (String) launchId;
	}

}
