package org.octri.fhir_sandbox.oauth2.customizer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.fhir_sandbox.domain.SmartLaunchContext;
import org.octri.fhir_sandbox.oauth2.utils.OAuthUtils;
import org.octri.fhir_sandbox.service.SmartLaunchContextService;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

@ExtendWith(MockitoExtension.class)
public class SmartLaunchContextIdTokenCustomizerTest {

	private static final String MOCK_CLIENT_ID = "mock-client-id";

	@Mock
	private SmartLaunchContextService service;

	@Mock
	private JwtEncodingContext context;

	@Mock
	private OAuth2Authorization authorization;

	@Mock
	private OAuth2TokenType tokenType;

	@Mock
	private JwtClaimsSet.Builder claimsBuilder;

	private SmartLaunchContextIdTokenCustomizer customizer() {
		return new SmartLaunchContextIdTokenCustomizer(service);
	}

	private OAuth2AuthorizationRequest buildAuthRequest(Map<String, Object> additionalParams) {
		return OAuth2AuthorizationRequest.authorizationCode()
				.authorizationUri("https://example.com/oauth2/authorize")
				.clientId(MOCK_CLIENT_ID)
				.additionalParameters(additionalParams)
				.build();
	}

	private void setupIdTokenContext(String launchId) {
		when(context.getTokenType()).thenReturn(tokenType);
		when(tokenType.getValue()).thenReturn(OidcParameterNames.ID_TOKEN);
		when(context.getAuthorization()).thenReturn(authorization);
		// getRegisteredClientId is only called when a launch ID is resolved
		if (launchId != null) {
			when(authorization.getRegisteredClientId()).thenReturn(MOCK_CLIENT_ID);
		}
		var additionalParams = launchId != null
				? Map.of(OAuthUtils.LAUNCH_PARAMETER_NAME, (Object) launchId)
				: Map.<String, Object> of();
		var authRequest = buildAuthRequest(additionalParams);
		when(authorization.getAttributes()).thenReturn(
				Map.of(OAuth2AuthorizationRequest.class.getCanonicalName(), authRequest));
	}

	@Test
	public void testCustomizeAddsFhirUserClaimWhenIdTokenAndFhirUserPresent() {
		var launchId = "abc123";
		var fhirUser = "Practitioner/123";
		setupIdTokenContext(launchId);
		var launchContext = new SmartLaunchContext();
		launchContext.setFhirUserAttribute(fhirUser);
		when(service.findByOpaqueIdAndClientId(launchId, MOCK_CLIENT_ID)).thenReturn(Optional.of(launchContext));
		when(context.getClaims()).thenReturn(claimsBuilder);
		when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

		customizer().customize(context);

		verify(claimsBuilder).claim("fhirUser", fhirUser);
	}

	@Test
	public void testCustomizeDoesNotAddClaimWhenFhirUserIsNull() {
		var launchId = "abc123";
		setupIdTokenContext(launchId);
		var launchContext = new SmartLaunchContext();
		when(service.findByOpaqueIdAndClientId(launchId, MOCK_CLIENT_ID)).thenReturn(Optional.of(launchContext));

		customizer().customize(context);

		verify(claimsBuilder, never()).claim(anyString(), any());
	}

	@Test
	public void testCustomizeDoesNotAddClaimWhenLaunchContextNotFound() {
		var launchId = "abc123";
		setupIdTokenContext(launchId);
		when(service.findByOpaqueIdAndClientId(launchId, MOCK_CLIENT_ID)).thenReturn(Optional.empty());

		customizer().customize(context);

		verify(claimsBuilder, never()).claim(anyString(), any());
	}

	@Test
	public void testCustomizeDoesNotProcessWhenLaunchParamAbsent() {
		setupIdTokenContext(null);

		customizer().customize(context);

		verify(service, never()).findByOpaqueIdAndClientId(anyString(), anyString());
	}

	@Test
	public void testCustomizeDoesNotProcessWhenTokenTypeIsNotIdToken() {
		when(context.getTokenType()).thenReturn(tokenType);
		when(tokenType.getValue()).thenReturn(OAuth2TokenType.ACCESS_TOKEN.getValue());

		customizer().customize(context);

		verify(service, never()).findByOpaqueIdAndClientId(anyString(), anyString());
	}

}
