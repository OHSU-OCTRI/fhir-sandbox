package org.octri.fhir_sandbox.oauth2.utils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utilities for working with OAuth authorization requests.
 */
public class OAuthUtils {

	public static final String LAUNCH_PARAMETER_NAME = "launch";
	public static final Map<String, Object> SYSTEM_CLAIMS = Map.of("sub", "system", "scope", List.of("system/*.cruds"));

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

	/**
	 * Returns true if the given JSON web token string is malformed or within 30 seconds of expiring.
	 *
	 * @param jwtStr
	 *            JSON web token string
	 * @return true if the token is malformed or will expire less than 30 seconds from now, otherwise false
	 */
	public static boolean isTokenNearExpiration(String jwtStr) {
		return isTokenNearExpiration(jwtStr, Duration.ofSeconds(30L));
	}

	/**
	 * Returns true if the given JSON web token string is malformed or if the token expiration is within the
	 * {@code expiryThreshold}.
	 *
	 * @param jwtStr
	 *            JSON web token string
	 * @param expiryThreshold
	 *            amount of time prior to expiration that the token will be considered near expiration
	 * @return true if the token is malformed or if the token will expire less than {@code expiryThreshold} from now,
	 *         otherwise false
	 */
	public static boolean isTokenNearExpiration(String jwtStr, Duration expiryThreshold) {
		Assert.notNull(expiryThreshold, "Token expiry threshold is required");

		if (jwtStr == null) {
			return true;
		}

		try {
			String[] parts = jwtStr.split("\\.");
			if (parts.length < 2) {
				// Does not look like a JWT
				return true;
			}

			// Decode JWT payload
			String paddedPayload = parts[1] + "=".repeat((4 - parts[1].length() % 4) % 4);
			String json = new String(Base64.getUrlDecoder().decode(paddedPayload), StandardCharsets.UTF_8);

			@SuppressWarnings("unchecked")
			Map<String, Object> claims = OBJECT_MAPPER.readValue(json, Map.class);
			Number exp = (Number) claims.get("exp");
			if (exp == null) {
				// Token expiration not found
				return true;
			}

			// Is the token expiration less than expiryThreshold from now
			return Instant.ofEpochSecond(exp.longValue()).isBefore(Instant.now().plus(expiryThreshold));
		} catch (Exception e) {
			// Error decoding or converting to map
			return true;
		}
	}

	/**
	 * Returns a map of token claims for the given sandbox URL with {@code system/*.*} scope.
	 *
	 * @param sandboxUrl
	 * @return map of claims for use in creating a JSON web token with system scope
	 */
	public static Map<String, Object> getSystemClaimsForSandbox(String sandboxUrl) {
		var claims = new HashMap<String, Object>();
		claims.putAll(SYSTEM_CLAIMS);
		claims.put("aud", sandboxUrl);
		return claims;
	}

}
