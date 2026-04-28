package org.octri.fhir_sandbox.oauth2.customizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.fhir_sandbox.domain.SmartLaunchContext;
import org.octri.fhir_sandbox.oauth2.utils.OAuthUtils;
import org.octri.fhir_sandbox.service.SmartLaunchContextService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;

@ExtendWith(MockitoExtension.class)
public class SmartLaunchContextTokenResponseCustomizerTest {

	@Mock
	private OAuth2AuthorizationService oauth2AuthorizationService;

	@Mock
	private SmartLaunchContextService smartLaunchContextService;

	@Mock
	private OAuth2AccessTokenAuthenticationContext context;

	@Mock
	private OAuth2AccessTokenAuthenticationToken authenticationToken;

	@Mock
	private OAuth2AccessToken accessToken;

	@Mock
	private OAuth2Authorization authorization;

	@Mock
	private OAuth2AccessTokenResponse.Builder responseBuilder;

	@Mock
	private Authentication wrongAuthentication;

	private SmartLaunchContextTokenResponseCustomizer customizer;

	@BeforeEach
	public void setUp() {
		customizer = new SmartLaunchContextTokenResponseCustomizer(oauth2AuthorizationService,
				smartLaunchContextService);
	}

	private void setupValidContext(String launchId) {
		when(context.getAuthentication()).thenReturn(authenticationToken);
		when(authenticationToken.getAccessToken()).thenReturn(accessToken);
		when(accessToken.getTokenValue()).thenReturn("token-value");
		when(oauth2AuthorizationService.findByToken("token-value", OAuth2TokenType.ACCESS_TOKEN))
				.thenReturn(authorization);
		when(authorization.getRegisteredClientId()).thenReturn("client-id");
		var additionalParams = launchId != null
				? Map.of(OAuthUtils.LAUNCH_PARAMETER_NAME, (Object) launchId)
				: Map.<String, Object> of();
		var authRequest = OAuth2AuthorizationRequest.authorizationCode()
				.authorizationUri("https://example.com/oauth2/authorize")
				.clientId("client-id")
				.additionalParameters(additionalParams)
				.build();
		when(authorization.getAttributes()).thenReturn(
				Map.of(OAuth2AuthorizationRequest.class.getCanonicalName(), authRequest));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAcceptMergesLaunchContextAttributesWhenLaunchContextFound() {
		var launchId = "launch-1";
		setupValidContext(launchId);
		var tokenParams = Map.of("id_token", (Object) "some-id-token");
		when(authenticationToken.getAdditionalParameters()).thenReturn(tokenParams);
		var launchContext = new SmartLaunchContext();
		launchContext.setPatientAttribute("Patient/123");
		when(smartLaunchContextService.findByOpaqueIdAndClientId(launchId, "client-id"))
				.thenReturn(Optional.of(launchContext));
		when(context.getAccessTokenResponse()).thenReturn(responseBuilder);
		when(responseBuilder.additionalParameters(any())).thenReturn(responseBuilder);

		customizer.accept(context);

		var captor = ArgumentCaptor.forClass(Map.class);
		verify(responseBuilder).additionalParameters(captor.capture());
		var merged = captor.getValue();
		assertEquals("some-id-token", merged.get("id_token"),
				"token response additional parameters are preserved in the merged map");
		assertEquals("Patient/123", merged.get(SmartLaunchContext.PATIENT_ATTRIBUTE),
				"launch context attributes are added to the merged map");
	}

	@Test
	public void testAcceptReturnsEarlyWhenNoLaunchParameter() {
		setupValidContext(null);

		customizer.accept(context);

		verify(smartLaunchContextService, never()).findByOpaqueIdAndClientId(anyString(), anyString());
		verify(responseBuilder, never()).additionalParameters(any());
	}

	@Test
	public void testAcceptThrowsExceptionWhenAuthenticationIsWrongType() {
		when(context.getAuthentication()).thenReturn(wrongAuthentication);

		assertThrows(IllegalArgumentException.class, () -> customizer.accept(context),
				"accept throws IllegalArgumentException when authentication is not an OAuth2AccessTokenAuthenticationToken");
	}

	@Test
	public void testAcceptThrowsExceptionWhenAuthorizationNotFound() {
		when(context.getAuthentication()).thenReturn(authenticationToken);
		when(authenticationToken.getAccessToken()).thenReturn(accessToken);
		when(accessToken.getTokenValue()).thenReturn("token-value");
		when(oauth2AuthorizationService.findByToken("token-value", OAuth2TokenType.ACCESS_TOKEN)).thenReturn(null);

		assertThrows(IllegalArgumentException.class, () -> customizer.accept(context),
				"accept throws IllegalArgumentException when no authorization is found for the access token");
	}

	@Test
	public void testAcceptThrowsExceptionWhenLaunchContextNotFound() {
		var launchId = "missing-launch";
		setupValidContext(launchId);
		when(authenticationToken.getAdditionalParameters()).thenReturn(Map.of());
		when(smartLaunchContextService.findByOpaqueIdAndClientId(launchId, "client-id"))
				.thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> customizer.accept(context),
				"accept throws IllegalArgumentException when no launch context is found for the given launch ID and client");
	}

}
