package org.octri.fhir_sandbox.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.fhir_sandbox.config.OAuth2ServerProperties;
import org.octri.fhir_sandbox.domain.ClientType;
import org.octri.fhir_sandbox.domain.SmartClient;
import org.octri.fhir_sandbox.repository.SmartClientRepository;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class SmartClientServiceTest {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private OAuth2ServerProperties oAuth2ServerProperties;

	@Mock
	private SmartClientRepository repository;

	@InjectMocks
	private SmartClientService service;

	private Duration mockAccessTokenTtl = Duration.ofMinutes(20);
	private Duration mockRefreshTokenTtl = Duration.ofHours(4);

	@BeforeAll
	public static void setUp() {
		var classLoader = SmartClientService.class.getClassLoader();
		var securityModules = SecurityJackson2Modules.getModules(classLoader);
		objectMapper.registerModules(securityModules);
		objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
	}

	@Test
	public void saveThrowsWhenClientIsNull() {
		SmartClient nullClient = null;
		assertThrows(IllegalArgumentException.class, () -> service.save(nullClient),
				"Saving a null client should throw an exception");
	}

	@Test
	public void saveDoesNotHandleConfidentialClientsYet() {
		var confidentialClient = new SmartClient();
		confidentialClient.setClientType(ClientType.CONFIDENTIAL);
		var ex = assertThrows(UnsupportedOperationException.class, () -> service.save(confidentialClient),
				"Saving a confidential client should throw an exception");
		assertEquals("Confidential clients are not supported yet", ex.getMessage(),
				"The exception message should explain that confidential clients are unsupported");
	}

	@Test
	public void savePersistsPublicClients() {
		var clientCaptor = ArgumentCaptor.forClass(SmartClient.class);
		var publicClient = getTestClient();

		mockServerProperties();
		service.save(publicClient);

		verify(repository).save(clientCaptor.capture());
		assertSame(publicClient, clientCaptor.getValue(), "The public client should be persisted");
	}

	@Test
	public void saveSetsPublicClientAttributesForNewClients() {
		var publicClient = getTestClient();

		mockServerProperties();

		service.save(publicClient);

		assertNotNull(publicClient.getClientAuthenticationMethods(), "Save should set authn methods for new clients");
		assertNotNull(publicClient.getAuthorizationGrantTypes(), "Save should set grant types for new clients");
		assertNotNull(publicClient.getClientSettings(), "Save should set client settings for new clients");
		assertNotNull(publicClient.getTokenSettings(), "Save should set token settings for new clients");
	}

	@Test
	public void saveResetsPublicClientAttributesForUpdatedClients() {
		var originalString = "canary";
		var publicClient = getTestClient();

		publicClient.setAuthorizationGrantTypes(originalString);
		publicClient.setClientAuthenticationMethods(originalString);
		publicClient.setClientSettings(originalString);
		publicClient.setTokenSettings(originalString);

		mockServerProperties();

		service.save(publicClient);

		assertNotEquals(publicClient.getClientAuthenticationMethods(), originalString,
				"Save should overwrite authn methods for updated clients");
		assertNotEquals(publicClient.getAuthorizationGrantTypes(), originalString,
				"Save should overwrite grant types for updated clients");
		assertNotEquals(publicClient.getClientSettings(), originalString,
				"Save should overwrite client settings for updated clients");
		assertNotEquals(publicClient.getTokenSettings(), originalString,
				"Save should overwrite token settings for updated clients");
	}

	@Test
	public void savePublicClientSetsAuthenticationMethodNone() {
		var publicClient = getTestClient();

		mockServerProperties();

		service.save(publicClient);

		assertEquals("none", publicClient.getClientAuthenticationMethods(),
				"Public clients should use ClientAuthenticationMethod.NONE");
	}

	@Test
	public void savePublicClientSetsExpectedGrantTypes() {
		var publicClient = getTestClient();

		mockServerProperties();

		service.save(publicClient);

		assertEquals("authorization_code refresh_token", publicClient.getAuthorizationGrantTypes(),
				"Public clients should allow authorization code and refresh_token grant types");
	}

	@Test
	public void savePublicClientSetsClientSettingsValues() throws JsonMappingException, JsonProcessingException {
		var publicClient = getTestClient();

		mockServerProperties();

		service.save(publicClient);

		assertNotNull(publicClient.getClientSettings(), "Save should populate client settings");

		var clientSettings = parseJsonMap(publicClient.getClientSettings());
		assertEquals(Boolean.TRUE, clientSettings.get("settings.client.require-proof-key"),
				"PKCE should be required for public clients");
		assertEquals(Boolean.TRUE, clientSettings.get("settings.client.require-authorization-consent"),
				"Authorization consent should be required for public clients");
	}

	@Test
	public void savePublicClientSetsTokenTtlValues() throws JsonMappingException, JsonProcessingException {
		var publicClient = getTestClient();

		mockServerProperties();

		service.save(publicClient);

		assertNotNull(publicClient.getTokenSettings(), "Save should populate token settings");

		var tokenSettings = parseJsonMap(publicClient.getTokenSettings());
		assertEquals(mockAccessTokenTtl, tokenSettings.get("settings.token.access-token-time-to-live"),
				"Access token TTL should be set from server settings");
		assertEquals(mockRefreshTokenTtl, tokenSettings.get("settings.token.refresh-token-time-to-live"),
				"Refresh token TTL should be set from server settings");
	}

	private SmartClient getTestClient() {
		var client = new SmartClient();
		client.setClientId(UUID.randomUUID().toString());
		client.setName("Test Client");
		client.setClientType(ClientType.PUBLIC);
		return client;
	}

	private void mockServerProperties() {
		when(oAuth2ServerProperties.getAccessTokenTtl()).thenReturn(mockAccessTokenTtl);
		when(oAuth2ServerProperties.getRefreshTokenTtl()).thenReturn(mockRefreshTokenTtl);
	}

	private Map<String, Object> parseJsonMap(String json) throws JsonMappingException, JsonProcessingException {
		return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
		});
	}
}
