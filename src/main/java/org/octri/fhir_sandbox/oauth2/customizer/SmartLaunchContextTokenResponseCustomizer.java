package org.octri.fhir_sandbox.oauth2.customizer;

import java.util.HashMap;
import java.util.function.Consumer;

import org.octri.fhir_sandbox.repository.SmartLaunchContextRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.util.Assert;

/**
 * Customizer that adds SMART app launch context to the OAuth 2.0 token response.
 */
public class SmartLaunchContextTokenResponseCustomizer implements Consumer<OAuth2AccessTokenAuthenticationContext> {

	private static final String LAUNCH_PARAMETER_NAME = "launch";

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final OAuth2AuthorizationService oauth2AuthorizationService;
	private final SmartLaunchContextRepository smartLaunchContextRepository;

	public SmartLaunchContextTokenResponseCustomizer(OAuth2AuthorizationService oauth2AuthorizationService,
			SmartLaunchContextRepository smartLaunchContextRepository) {
		this.oauth2AuthorizationService = oauth2AuthorizationService;
		this.smartLaunchContextRepository = smartLaunchContextRepository;
	}

	@Override
	public void accept(OAuth2AccessTokenAuthenticationContext context) {
		var authentication = context.getAuthentication();
		Assert.isTrue(authentication instanceof OAuth2AccessTokenAuthenticationToken,
				"Unexpected Authentication type: " + authentication.getClass().getSimpleName());

		var authenticationToken = (OAuth2AccessTokenAuthenticationToken) authentication;
		var tokenResponseAdditionalParams = authenticationToken.getAdditionalParameters();

		var accessTokenStr = ((OAuth2AccessTokenAuthenticationToken) authentication).getAccessToken().getTokenValue();
		var authorization = oauth2AuthorizationService.findByToken(accessTokenStr, OAuth2TokenType.ACCESS_TOKEN);
		Assert.notNull(authorization, "Authorization not found for authentication token");

		var authRequest = authorization.getAttributes()
				.get(OAuth2AuthorizationRequest.class.getCanonicalName());
		Assert.notNull(authRequest,
				"No authorization request found in attributes for authorization " + authorization.getId());
		Assert.isTrue(authRequest instanceof OAuth2AuthorizationRequest,
				"Expected OAuth2Authorization request, but found " + authRequest.getClass().getName());

		var authRequestAdditionalParams = ((OAuth2AuthorizationRequest) authRequest).getAdditionalParameters();
		var launchId = authRequestAdditionalParams.get(LAUNCH_PARAMETER_NAME);
		var clientId = authorization.getRegisteredClientId();
		log.debug("Launch context ID for authorization {}: {}", authorization.getId(), launchId);
		if (launchId == null) {
			log.debug("No launch parameter. Not customizing token response.");
			return;
		}

		var optContext = smartLaunchContextRepository.findByOpaqueIdAndClientId((String) launchId, clientId);
		Assert.isTrue(optContext.isPresent(), "Launch context " + launchId + " not found for client " + clientId);
		var attrs = optContext.get().getAttributes();

		// merge the access token response's additional parameters with the launch context to avoid losing
		// the ID token
		var mergedParams = new HashMap<String, Object>();
		mergedParams.putAll(tokenResponseAdditionalParams);
		mergedParams.putAll(attrs);

		context.getAccessTokenResponse()
				.additionalParameters(mergedParams);
	}

}
