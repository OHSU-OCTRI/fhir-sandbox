package org.octri.fhir_sandbox.service;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import org.octri.fhir_sandbox.domain.ClientType;
import org.octri.fhir_sandbox.domain.SmartClient;
import org.octri.fhir_sandbox.oauth2.repository.ClientRepository;
import org.octri.fhir_sandbox.oauth2.service.JpaRegisteredClientRepository;
import org.octri.fhir_sandbox.repository.SmartClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.lang.Assert;

/**
 * Service for managing {@link SmartClient} objects, including synchronization with the OAuth2 registered clients.
 */
@Service
public class SmartClientService {

	private final SmartClientRepository repository;
	private final JpaRegisteredClientRepository registeredClientRepository;
	private final ClientRepository oauth2ClientRepository;

	public SmartClientService(SmartClientRepository repository,
			JpaRegisteredClientRepository registeredClientRepository, ClientRepository oauth2ClientRepository) {
		this.repository = repository;
		this.registeredClientRepository = registeredClientRepository;
		this.oauth2ClientRepository = oauth2ClientRepository;
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
	 * Saves a client and the corresponding OAuth2 registered client.
	 *
	 * @param client
	 * @return
	 */
	public SmartClient save(SmartClient client) {
		Assert.notNull(client);
		if (ClientType.PUBLIC.equals(client.getClientType())) {
			var registeredClient = toPublicRegisteredClient(client);
			registeredClientRepository.save(registeredClient);
		} else if (ClientType.CONFIDENTIAL.equals(client.getClientType())) {
			var registeredClient = toConfidentialRegisteredClient(client);
			registeredClientRepository.save(registeredClient);
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
		oauth2ClientRepository.deleteById(client.getClientId());
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
	 * Convert a {@link SmartClient} to a {@link RegisteredClient} for public clients.
	 *
	 * @param client
	 * @return
	 */
	private RegisteredClient toPublicRegisteredClient(SmartClient client) {
		var registeredClientBuilder = RegisteredClient.withId(client.getClientId())
				.clientId(client.getClientId())
				.clientIdIssuedAt(Instant.now())
				.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.clientSettings(
						ClientSettings.builder()
								.requireAuthorizationConsent(true)
								.requireProofKey(true)
								.build());

		var redirectUris = client.getRedirectUris().strip().split("\\s+");
		Stream.of(redirectUris).forEach(registeredClientBuilder::redirectUri);

		var scopes = client.getScopes().strip().split("\\s+");
		Stream.of(scopes).forEach(registeredClientBuilder::scope);

		return registeredClientBuilder.build();
	}

	/**
	 * Convert a {@link SmartClient} to a {@link RegisteredClient} for confidential clients.
	 * TODO: RFS-257 Not implemented yet - need to generate and manage client secrets.
	 *
	 * @param client
	 * @return
	 */
	private RegisteredClient toConfidentialRegisteredClient(SmartClient client) {
		// TODO: Generate client secret
		throw new UnsupportedOperationException("Confidential clients are not supported yet");
	}

}
