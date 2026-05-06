package org.octri.fhir_sandbox.oauth2.customizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.domain.SmartClient;
import org.octri.fhir_sandbox.domain.SmartLaunchContext;
import org.octri.fhir_sandbox.oauth2.utils.OAuthUtils;
import org.octri.fhir_sandbox.service.SandboxService;
import org.octri.fhir_sandbox.service.SmartClientService;
import org.octri.fhir_sandbox.service.SmartLaunchContextService;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

@ExtendWith(MockitoExtension.class)
public class SmartOnFhirAwareTokenCustomizerTest {

	private static final String MOCK_CLIENT_ID = "mock-client-id";

	@Mock
	private SandboxService sandboxService;

	@Mock
	private SmartClientService smartClientService;

	@Mock
	private SmartLaunchContextService contextService;

	@Mock
	private JwtEncodingContext context;

	@Mock
	private OAuth2Authorization authorization;

	@Mock
	private OAuth2TokenType tokenType;

	@Mock
	private JwtClaimsSet.Builder claimsBuilder;

	@Mock
	private Sandbox sandbox;

	@Mock
	private SmartClient smartClient;

	@Captor
	ArgumentCaptor<List<String>> listCaptor;

	private SmartOnFhirAwareTokenCustomizer customizer() {
		return new SmartOnFhirAwareTokenCustomizer(sandboxService, smartClientService, contextService);
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

	private void setupAccessTokenContext() {
		when(context.getTokenType()).thenReturn(OAuth2TokenType.ACCESS_TOKEN);
		when(context.getAuthorization()).thenReturn(authorization);
	}

	@Test
	public void testCustomizeAddsFhirUserClaimWhenIdTokenAndFhirUserPresent() {
		var launchId = "abc123";
		var fhirUser = "Practitioner/123";
		setupIdTokenContext(launchId);
		var launchContext = new SmartLaunchContext();
		launchContext.setFhirUserAttribute(fhirUser);
		when(contextService.findByOpaqueIdAndClientId(launchId, MOCK_CLIENT_ID)).thenReturn(Optional.of(launchContext));
		when(context.getClaims()).thenReturn(claimsBuilder);
		when(context.getAuthorizedScopes()).thenReturn(Set.of("openid", "fhirUser"));
		when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

		customizer().customize(context);

		verify(claimsBuilder).claim("fhirUser", fhirUser);
	}

	@Test
	public void testCustomizeDoesNotAddClaimWhenFhirUserIsNull() {
		var launchId = "abc123";
		setupIdTokenContext(launchId);
		var launchContext = new SmartLaunchContext();
		when(contextService.findByOpaqueIdAndClientId(launchId, MOCK_CLIENT_ID)).thenReturn(Optional.of(launchContext));

		customizer().customize(context);

		verify(claimsBuilder, never()).claim(anyString(), any());
	}

	@Test
	public void testCustomizeDoesNotAddClaimWhenFhirUserScopeIsMissing() {
		var launchId = "abc123";
		var fhirUser = "Practitioner/123";
		setupIdTokenContext(launchId);
		var launchContext = new SmartLaunchContext();
		launchContext.setFhirUserAttribute(fhirUser);
		when(contextService.findByOpaqueIdAndClientId(launchId, MOCK_CLIENT_ID)).thenReturn(Optional.of(launchContext));
		when(context.getAuthorizedScopes()).thenReturn(Set.of("openid"));

		customizer().customize(context);

		verify(claimsBuilder, never()).claim(anyString(), any());
	}

	@Test
	public void testCustomizeDoesNotAddClaimWhenLaunchContextNotFound() {
		var launchId = "abc123";
		setupIdTokenContext(launchId);
		when(contextService.findByOpaqueIdAndClientId(launchId, MOCK_CLIENT_ID)).thenReturn(Optional.empty());

		customizer().customize(context);

		verify(claimsBuilder, never()).claim(anyString(), any());
	}

	@Test
	public void testCustomizeDoesNotProcessWhenLaunchParamAbsent() {
		setupIdTokenContext(null);

		customizer().customize(context);

		verify(contextService, never()).findByOpaqueIdAndClientId(anyString(), anyString());
	}

	@Test
	public void testCustomizeDoesNotProcessWhenTokenTypeIsNotIdToken() {
		when(context.getTokenType()).thenReturn(tokenType);
		when(tokenType.getValue()).thenReturn(OAuth2TokenType.REFRESH_TOKEN.getValue());

		customizer().customize(context);

		verify(contextService, never()).findByOpaqueIdAndClientId(anyString(), anyString());
	}

	@Test
	public void testCustomizeAddsAudienceToAccessTokens() {
		var clientId = "12ebc1b1-fc45-413e-b508-6bd68560aa1c";
		var fhirServerUrl = "http://localhost:8001/fhir/a9dea874-163a-4887-a48c-f95c028cbca5/";

		setupAccessTokenContext();
		when(authorization.getRegisteredClientId()).thenReturn(clientId);
		when(smartClientService.findSmartClientByClientId(clientId)).thenReturn(Optional.of(smartClient));
		when(smartClient.getSandbox()).thenReturn(sandbox);
		when(sandboxService.getSandboxFhirUrl(sandbox)).thenReturn(fhirServerUrl);
		when(context.getClaims()).thenReturn(claimsBuilder);

		customizer().customize(context);

		verify(claimsBuilder).audience(listCaptor.capture());
		var audienceList = listCaptor.getValue();
		assertEquals(1, audienceList.size(), "There should be one item in the audience list");
		assertTrue(audienceList.contains(fhirServerUrl), "The FHIR server URL should be included in the audience list");
	}

	@Test
	public void testCustomizeRequiresSmartClientToCustomizeAccessToken() {
		var clientId = "12ebc1b1-fc45-413e-b508-6bd68560aa1c";
		setupAccessTokenContext();
		when(authorization.getRegisteredClientId()).thenReturn(clientId);
		when(smartClientService.findSmartClientByClientId(clientId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> customizer().customize(context),
				"Throws IllegalArgumentException when client for authorization is not found");
	}

	@Test
	public void testCustomizeRequiresSandboxToCustomizeAccessToken() {
		var clientId = "12ebc1b1-fc45-413e-b508-6bd68560aa1c";
		setupAccessTokenContext();
		when(authorization.getRegisteredClientId()).thenReturn(clientId);
		when(smartClientService.findSmartClientByClientId(clientId)).thenReturn(Optional.of(smartClient));
		when(smartClient.getSandbox()).thenReturn(null);

		assertThrows(IllegalArgumentException.class, () -> customizer().customize(context),
				"Throws IllegalArgumentException when sandbox is not found (should be impossible)");
	}

}
