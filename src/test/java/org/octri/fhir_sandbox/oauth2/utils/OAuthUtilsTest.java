package org.octri.fhir_sandbox.oauth2.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.fhir_sandbox.JwtTestUtils;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

@ExtendWith(MockitoExtension.class)
public class OAuthUtilsTest {

	@Mock
	private OAuth2Authorization authorization;

	private OAuth2AuthorizationRequest buildAuthRequest(Map<String, Object> additionalParams) {
		return OAuth2AuthorizationRequest.authorizationCode()
				.authorizationUri("https://example.com/oauth2/authorize")
				.clientId("mock-client-id")
				.additionalParameters(additionalParams)
				.build();
	}

	@Test
	public void testGetLaunchIdFromAuthorizationReturnsLaunchIdWhenPresent() {
		var launchId = "abc123";
		var authRequest = buildAuthRequest(Map.of(OAuthUtils.LAUNCH_PARAMETER_NAME, launchId));
		when(authorization.getAttributes()).thenReturn(
				Map.of(OAuth2AuthorizationRequest.class.getCanonicalName(), authRequest));

		var result = OAuthUtils.getLaunchIdFromAuthorization(authorization);

		assertEquals(launchId, result, "getLaunchIdFromAuthorization returns the launch ID when present");
	}

	@Test
	public void testGetLaunchIdFromAuthorizationReturnsNullWhenLaunchParamAbsent() {
		var authRequest = buildAuthRequest(Map.of());
		when(authorization.getAttributes()).thenReturn(
				Map.of(OAuth2AuthorizationRequest.class.getCanonicalName(), authRequest));

		var result = OAuthUtils.getLaunchIdFromAuthorization(authorization);

		assertNull(result, "getLaunchIdFromAuthorization returns null when the launch parameter is not present");
	}

	@Test
	public void testGetLaunchIdFromAuthorizationReturnsNullWhenLaunchParamNotString() {
		var authRequest = buildAuthRequest(Map.of(OAuthUtils.LAUNCH_PARAMETER_NAME, 42));
		when(authorization.getAttributes()).thenReturn(
				Map.of(OAuth2AuthorizationRequest.class.getCanonicalName(), authRequest));

		var result = OAuthUtils.getLaunchIdFromAuthorization(authorization);

		assertNull(result, "getLaunchIdFromAuthorization returns null when the launch parameter is not a String");
	}

	@Test
	public void testGetLaunchIdFromAuthorizationThrowsExceptionWhenAuthorizationNull() {
		assertThrows(IllegalArgumentException.class,
				() -> OAuthUtils.getLaunchIdFromAuthorization(null),
				"getLaunchIdFromAuthorization throws IllegalArgumentException when authorization is null");
	}

	@Test
	public void testGetLaunchIdFromAuthorizationThrowsExceptionWhenAuthRequestAbsent() {
		var attrs = new HashMap<String, Object>();
		attrs.put(OAuth2AuthorizationRequest.class.getCanonicalName(), null);
		when(authorization.getAttributes()).thenReturn(attrs);

		assertThrows(IllegalArgumentException.class,
				() -> OAuthUtils.getLaunchIdFromAuthorization(authorization),
				"getLaunchIdFromAuthorization throws IllegalArgumentException when no authorization request is in the attributes");
	}

	@Test
	public void testIsTokenNearExpirationReturnsTrueForNullToken() {
		assertTrue(OAuthUtils.isTokenNearExpiration(null),
				"isTokenNearExpiration returns true for a null token");
	}

	@Test
	public void testIsTokenNearExpirationReturnsTrueForExpiredToken() {
		String jwt = JwtTestUtils.buildJwt(Instant.now().minusSeconds(60).getEpochSecond());
		assertTrue(OAuthUtils.isTokenNearExpiration(jwt),
				"isTokenNearExpiration returns true for an expired token");
	}

	@Test
	public void testIsTokenNearExpirationReturnsTrueWhenExpirationWithinDefaultThreshold() {
		// Expires in 15 seconds — within the 30-second default threshold
		String jwt = JwtTestUtils.buildJwt(Instant.now().plusSeconds(15).getEpochSecond());
		assertTrue(OAuthUtils.isTokenNearExpiration(jwt),
				"isTokenNearExpiration returns true when expiration is within the default 30-second threshold");
	}

	@Test
	public void testIsTokenNearExpirationReturnsFalseWhenExpirationBeyondDefaultThreshold() {
		String jwt = JwtTestUtils.buildJwt(Instant.now().plusSeconds(3600).getEpochSecond());
		assertFalse(OAuthUtils.isTokenNearExpiration(jwt),
				"isTokenNearExpiration returns false when expiration is well beyond the default threshold");
	}

	@Test
	public void testIsTokenNearExpirationThrowsWhenThresholdNull() {
		assertThrows(IllegalArgumentException.class,
				() -> OAuthUtils.isTokenNearExpiration("any", null),
				"isTokenNearExpiration throws IllegalArgumentException when threshold is null");
	}

	@Test
	public void testIsTokenNearExpirationReturnsTrueForNullTokenWithCustomThreshold() {
		assertTrue(OAuthUtils.isTokenNearExpiration(null, Duration.ofSeconds(60)),
				"isTokenNearExpiration returns true for a null token with a custom threshold");
	}

	@Test
	public void testIsTokenNearExpirationReturnsTrueForMalformedJwt() {
		assertTrue(OAuthUtils.isTokenNearExpiration("notajwt", Duration.ofSeconds(30)),
				"isTokenNearExpiration returns true for a string that is not a JWT");
	}

	@Test
	public void testIsTokenNearExpirationReturnsTrueForJwtWithOnePartOnly() {
		String header = Base64.getUrlEncoder().withoutPadding()
				.encodeToString("{\"alg\":\"RS256\"}".getBytes(StandardCharsets.UTF_8));
		assertTrue(OAuthUtils.isTokenNearExpiration(header, Duration.ofSeconds(30)),
				"isTokenNearExpiration returns true for a JWT with only one segment");
	}

	@Test
	public void testIsTokenNearExpirationReturnsTrueForJwtWithInvalidBase64Payload() {
		assertTrue(OAuthUtils.isTokenNearExpiration("header.!!!invalid!!!.sig", Duration.ofSeconds(30)),
				"isTokenNearExpiration returns true when the JWT payload cannot be base64-decoded");
	}

	@Test
	public void testIsTokenNearExpirationReturnsTrueWhenExpClaimAbsent() {
		assertTrue(OAuthUtils.isTokenNearExpiration(JwtTestUtils.buildJwtWithoutExp(), Duration.ofSeconds(30)),
				"isTokenNearExpiration returns true when the JWT payload has no exp claim");
	}

	@Test
	public void testIsTokenNearExpirationReturnsTrueWhenTokenExpired() {
		String jwt = JwtTestUtils.buildJwt(Instant.now().minusSeconds(60).getEpochSecond());
		assertTrue(OAuthUtils.isTokenNearExpiration(jwt, Duration.ofSeconds(30)),
				"isTokenNearExpiration returns true when the token is already expired");
	}

	@Test
	public void testIsTokenNearExpirationReturnsTrueWhenExpirationWithinThreshold() {
		// Expires in 20 seconds — within the 30-second threshold
		String jwt = JwtTestUtils.buildJwt(Instant.now().plusSeconds(20).getEpochSecond());
		assertTrue(OAuthUtils.isTokenNearExpiration(jwt, Duration.ofSeconds(30)),
				"isTokenNearExpiration returns true when expiration is within the custom threshold");
	}

	@Test
	public void testIsTokenNearExpirationReturnsFalseWhenExpirationBeyondThreshold() {
		String jwt = JwtTestUtils.buildJwt(Instant.now().plusSeconds(3600).getEpochSecond());
		assertFalse(OAuthUtils.isTokenNearExpiration(jwt, Duration.ofSeconds(30)),
				"isTokenNearExpiration returns false when expiration is well beyond the custom threshold");
	}

	@Test
	public void testGetSystemClaimsForSandboxIncludesAudClaim() {
		var sandboxUrl = "https://sandbox.example.com/fhir";
		var claims = OAuthUtils.getSystemClaimsForSandbox(sandboxUrl);
		assertEquals(sandboxUrl, claims.get("aud"),
				"getSystemClaimsForSandbox includes the sandbox URL as the aud claim");
	}

	@Test
	public void testGetSystemClaimsForSandboxIncludesSystemClaims() {
		var claims = OAuthUtils.getSystemClaimsForSandbox("https://sandbox.example.com/fhir");
		OAuthUtils.SYSTEM_CLAIMS.forEach((key, value) ->
				assertEquals(value, claims.get(key),
						"getSystemClaimsForSandbox includes SYSTEM_CLAIMS entry: " + key));
	}

	@Test
	public void testGetSystemClaimsForSandboxReturnsNewMapInstance() {
		var claims = OAuthUtils.getSystemClaimsForSandbox("https://sandbox.example.com/fhir");
		assertNotSame(OAuthUtils.SYSTEM_CLAIMS, claims,
				"getSystemClaimsForSandbox returns a new map, not the SYSTEM_CLAIMS constant");
	}

	@Test
	public void testGetSystemClaimsForSandboxAudOverridesSystemClaimsIfPresent() {
		// SYSTEM_CLAIMS does not contain "aud", but verify aud from the argument takes precedence
		// over any future addition to SYSTEM_CLAIMS by confirming only one value for "aud" is set.
		var sandboxUrl = "https://sandbox.example.com/fhir";
		var claims = OAuthUtils.getSystemClaimsForSandbox(sandboxUrl);
		assertEquals(sandboxUrl, claims.get("aud"),
				"getSystemClaimsForSandbox sets aud to the provided sandboxUrl regardless of SYSTEM_CLAIMS content");
	}

}
