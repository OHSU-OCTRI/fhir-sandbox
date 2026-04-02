package org.octri.fhir_sandbox.domain;

import org.octri.common.domain.AbstractEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

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
	private String clientId;

	@NotNull
	private String description;

	@NotNull
	private String launchUri;

	@NotNull
	private String redirectUris;

	@NotNull
	private String scopes;

	public Sandbox getSandbox() {
		return sandbox;
	}

	public void setSandbox(Sandbox sandbox) {
		this.sandbox = sandbox;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public ClientType getClientType() {
		return clientType;
	}

	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
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

	public String getScopes() {
		return scopes;
	}

	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

}
