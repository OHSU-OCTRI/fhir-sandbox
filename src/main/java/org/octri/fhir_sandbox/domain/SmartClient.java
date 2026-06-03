package org.octri.fhir_sandbox.domain;

import java.time.Instant;

import org.octri.common.domain.AbstractEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entity representing a SMART on FHIR Client associated with a {@link Sandbox}.
 */
@Entity
public class SmartClient extends AbstractEntity {

	@ManyToOne
	@NotNull
	private Sandbox sandbox;

	@NotNull
	@Enumerated(EnumType.STRING)
	private ClientType clientType;

	@NotNull
	@Column(unique = true)
	private String clientId;

	@NotNull
	private String name;

	@Size(max = 1000)
	@Column(length = 1000)
	private String description;

	@NotNull
	@Size(max = 1000)
	@Column(length = 1000)
	private String launchUri;

	@NotNull
	@Column(columnDefinition = "TEXT")
	private String redirectUris;

	@Column(columnDefinition = "TEXT")
	private String postLogoutRedirectUris;

	@NotNull
	@Size(max = 2000)
	@Column(columnDefinition = "TEXT")
	private String scopes;

	private String clientSecret;

	private Instant clientSecretExpiresAt;

	@NotNull
	private String clientAuthenticationMethods;

	@NotNull
	private String authorizationGrantTypes;

	@NotNull
	@Column(columnDefinition = "TEXT")
	private String clientSettings;

	@NotNull
	@Column(columnDefinition = "TEXT")
	private String tokenSettings;

	public Sandbox getSandbox() {
		return sandbox;
	}

	public void setSandbox(Sandbox sandbox) {
		this.sandbox = sandbox;
	}

	public ClientType getClientType() {
		return clientType;
	}

	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLaunchUri() {
		return launchUri;
	}

	public void setLaunchUri(String launchUri) {
		this.launchUri = launchUri;
	}

	public String getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(String redirectUris) {
		this.redirectUris = redirectUris;
	}

	public String getPostLogoutRedirectUris() {
		return postLogoutRedirectUris;
	}

	public void setPostLogoutRedirectUris(String postLogoutRedirectUris) {
		this.postLogoutRedirectUris = postLogoutRedirectUris;
	}

	public String getScopes() {
		return scopes;
	}

	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public Instant getClientSecretExpiresAt() {
		return clientSecretExpiresAt;
	}

	public void setClientSecretExpiresAt(Instant clientSecretExpiresAt) {
		this.clientSecretExpiresAt = clientSecretExpiresAt;
	}

	public String getClientAuthenticationMethods() {
		return clientAuthenticationMethods;
	}

	public void setClientAuthenticationMethods(String clientAuthenticationMethods) {
		this.clientAuthenticationMethods = clientAuthenticationMethods;
	}

	public String getAuthorizationGrantTypes() {
		return authorizationGrantTypes;
	}

	public void setAuthorizationGrantTypes(String authorizationGrantTypes) {
		this.authorizationGrantTypes = authorizationGrantTypes;
	}

	public String getClientSettings() {
		return clientSettings;
	}

	public void setClientSettings(String clientSettings) {
		this.clientSettings = clientSettings;
	}

	public String getTokenSettings() {
		return tokenSettings;
	}

	public void setTokenSettings(String tokenSettings) {
		this.tokenSettings = tokenSettings;
	}

}
