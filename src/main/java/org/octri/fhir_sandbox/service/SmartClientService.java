package org.octri.fhir_sandbox.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.octri.fhir_sandbox.domain.ClientType;
import org.octri.fhir_sandbox.domain.SmartClient;
import org.octri.fhir_sandbox.repository.SmartClientRepository;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.lang.Assert;

/**
 * Service for managing {@link SmartClient} objects, including synchronization with the OAuth2 registered clients.
 */
@Service
public class SmartClientService implements RegisteredClientRepository {

	private final SmartClientRepository repository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public SmartClientService(SmartClientRepository repository) {
		this.repository = repository;

		ClassLoader classLoader = SmartClientService.class.getClassLoader();
		List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
		this.objectMapper.registerModules(securityModules);
		this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
	}

	/**
	 * Finds a client by ID.
	 *
	 * @param id
	 * @return
	 */
	public Optional<SmartClient> findById(Long id) {
		return repository.findById(id);
	}

	/**
	 * Saves a SmartClient.
	 *
	 * @param client
	 * @return
	 */
	public SmartClient save(SmartClient client) {
		Assert.notNull(client);
		if (ClientType.PUBLIC.equals(client.getClientType())) {
			configurePublicClient(client);
		} else if (ClientType.CONFIDENTIAL.equals(client.getClientType())) {
			configureConfidentialClient(client);
		} else {
			throw new IllegalArgumentException("Unsupported client type: " + client.getClientType());
		}

		return repository.save(client);
	}

	/**
	 * Deletes a client and the corresponding OAuth2 registered client.
	 *
	 * @param client
	 */
	public void delete(SmartClient client) {
		Assert.notNull(client);
		repository.delete(client);
	}

	/**
	 * Deletes a client by ID. Finds the client, then delegates to {@link #delete(SmartClient)}.
	 *
	 * @param id
	 */
	public void deleteById(Long id) {
		Assert.notNull(id);
		repository.findById(id).ifPresent(this::delete);
	}

	/**
	 * Configure a public client with appropriate authentication methods and grant types.
	 *
	 * @param client
	 */
	private void configurePublicClient(SmartClient client) {
		client.setClientAuthenticationMethods(ClientAuthenticationMethod.NONE.getValue());
		client.setAuthorizationGrantTypes(AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
		var clientSettings = ClientSettings.builder()
				.requireAuthorizationConsent(true)
				.requireProofKey(true)
				.build();
		var tokenSettings = TokenSettings.builder().build();
		client.setClientSettings(writeMap(clientSettings.getSettings()));
		client.setTokenSettings(writeMap(tokenSettings.getSettings()));
	}

	/**
	 * Configure a confidential client with appropriate authentication methods and grant types.
	 * TODO: RFS-257 Not implemented yet - need to generate and manage client secrets.
	 *
	 * @param client
	 */
	private void configureConfidentialClient(SmartClient client) {
		throw new UnsupportedOperationException("Confidential clients are not supported yet");
	}

	/**
	 * Saves a {@link RegisteredClient} after converting it to a {@link SmartClient}. The client must already exist.
	 *
	 * This method should only be called when client secrets are re-encoded or through OIDC dynamic client registration,
	 * which is not currently supported.
	 */
	@Override
	public void save(RegisteredClient registeredClient) {
		var optClient = repository.findByClientId(registeredClient.getClientId());
		Assert.isTrue(optClient.isPresent(),
				"RegisteredClient with clientId " + registeredClient.getClientId() + " must exist");
		var existingClient = optClient.get();

		// Update the client secret
		var newSecret = registeredClient.getClientSecret();
		Assert.isTrue(StringUtils.isNotBlank(newSecret) && !existingClient.getClientSecret().equals(newSecret),
				"The RegisteredClient secret must be different from the existing secret");
		existingClient.setClientSecret(registeredClient.getClientSecret());
		repository.save(existingClient);
	}

	/**
	 * Finds a {@link RegisteredClient} by ID. Delegates to {@link #findByClientId(String)} since client ID is used as
	 * the registered client ID. Returns null if not found.
	 */
	@Override
	public RegisteredClient findById(String id) {
		return findByClientId(id);
	}

	/**
	 * Finds a {@link RegisteredClient} by client ID. Converts the corresponding {@link SmartClient} to a
	 * {@link RegisteredClient} and returns it. Returns null if not found.
	 */
	@Override
	public RegisteredClient findByClientId(String clientId) {
		var optClient = repository.findByClientId(clientId);
		if (optClient.isPresent()) {
			return toRegisteredClient(optClient.get());
		}

		return null;
	}

	private RegisteredClient toRegisteredClient(SmartClient client) {
		var registeredClientBuilder = RegisteredClient.withId(client.getClientId())
				.clientId(client.getClientId())
				.clientIdIssuedAt(client.getCreatedAt().toInstant())
				.clientSecret(client.getClientSecret())
				.clientSecretExpiresAt(client.getClientSecretExpiresAt())
				.clientAuthenticationMethods(authenticationMethods -> {
					var methods = client.getClientAuthenticationMethods().strip().split("\\s+");
					Stream.of(methods)
							.forEach(method -> authenticationMethods.add(new ClientAuthenticationMethod(method)));
				})
				.authorizationGrantTypes(grantTypes -> {
					var types = client.getAuthorizationGrantTypes().strip().split("\\s+");
					Stream.of(types).forEach(type -> grantTypes.add(new AuthorizationGrantType(type)));
				})
				.redirectUris(uris -> {
					var redirectUris = client.getRedirectUris().strip().split("\\s+");
					Stream.of(redirectUris).forEach(uris::add);
				})
				.scopes(scopes -> {
					var scopeList = client.getScopes().strip().split("\\s+");
					Stream.of(scopeList).forEach(scopes::add);
				})
				.clientSettings(ClientSettings.withSettings(parseMap(client.getClientSettings())).build())
				.tokenSettings(TokenSettings.withSettings(parseMap(client.getTokenSettings())).build());

		if (client.getPostLogoutRedirectUris() != null) {
			registeredClientBuilder.postLogoutRedirectUris(uris -> {
				var postLogoutRedirectUris = client.getPostLogoutRedirectUris().strip().split("\\s+");
				Stream.of(postLogoutRedirectUris).forEach(uris::add);
			});
		}

		return registeredClientBuilder.build();
	}

	private SmartClient toSmartClient(RegisteredClient registeredClient) {
		var client = new SmartClient();
		client.setClientId(registeredClient.getClientId());
		client.setCreatedAt(Date.from(registeredClient.getClientIdIssuedAt()));
		client.setClientSecret(registeredClient.getClientSecret());
		client.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());

		var authenticationMethods = registeredClient.getClientAuthenticationMethods().stream()
				.map(ClientAuthenticationMethod::getValue)
				.reduce((a, b) -> a + " " + b)
				.orElse("");
		client.setClientAuthenticationMethods(authenticationMethods);

		var grantTypes = registeredClient.getAuthorizationGrantTypes().stream()
				.map(AuthorizationGrantType::getValue)
				.reduce((a, b) -> a + " " + b)
				.orElse("");
		client.setAuthorizationGrantTypes(grantTypes);

		var redirectUris = registeredClient.getRedirectUris().stream()
				.reduce((a, b) -> a + " " + b)
				.orElse("");
		client.setRedirectUris(redirectUris);

		var postLogoutRedirectUris = registeredClient.getPostLogoutRedirectUris().stream()
				.reduce((a, b) -> a + " " + b)
				.orElse("");
		client.setPostLogoutRedirectUris(postLogoutRedirectUris);

		var scopes = registeredClient.getScopes().stream()
				.reduce((a, b) -> a + " " + b)
				.orElse("");
		client.setScopes(scopes);

		client.setClientSettings(writeMap(registeredClient.getClientSettings().getSettings()));
		client.setTokenSettings(writeMap(registeredClient.getTokenSettings().getSettings()));

		return client;
	}

	private Map<String, Object> parseMap(String data) {
		try {
			return this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private String writeMap(Map<String, Object> data) {
		try {
			return this.objectMapper.writeValueAsString(data);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

}
