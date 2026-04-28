package org.octri.fhir_sandbox.oauth2.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

@ExtendWith(MockitoExtension.class)
public class OAuthUtilsTest {

	@Mock
	private OAuth2Authorization authorization;

	private OAuth2AuthorizationRequest buildAuthRequest(Map<String, Object> additionalParams) {
		return OAuth2AuthorizationRequest.authorizationCode()
				.authorizationUri("https://example.com/oauth2/authorize")
				.clientId("client-id")
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

}
