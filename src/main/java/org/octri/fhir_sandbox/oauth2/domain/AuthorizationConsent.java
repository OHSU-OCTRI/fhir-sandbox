package org.octri.fhir_sandbox.oauth2.domain;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Entity representing an OAuth2 Authorization Consent, as defined by Spring Authorization Server.
 *
 * @see <a href="https://docs.spring.io/spring-authorization-server/reference/guides/how-to-jpa.html">Spring
 *      Authorization Server - How to Use JPA</a>
 */
@Entity
@IdClass(AuthorizationConsent.AuthorizationConsentId.class)
@Table(name = "oauth2_authorization_consent")
public class AuthorizationConsent {

	@Id
	private String registeredClientId;
	@Id
	private String principalName;
	@Column(length = 1000)
	private String authorities;

	public String getRegisteredClientId() {
		return registeredClientId;
	}

	public void setRegisteredClientId(String registeredClientId) {
		this.registeredClientId = registeredClientId;
	}

	public String getPrincipalName() {
		return principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}

	public String getAuthorities() {
		return authorities;
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public static class AuthorizationConsentId implements Serializable {

		private String registeredClientId;
		private String principalName;

		public String getRegisteredClientId() {
			return registeredClientId;
		}

		public void setRegisteredClientId(String registeredClientId) {
			this.registeredClientId = registeredClientId;
		}

		public String getPrincipalName() {
			return principalName;
		}

		public void setPrincipalName(String principalName) {
			this.principalName = principalName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AuthorizationConsentId that = (AuthorizationConsentId) o;
			return registeredClientId.equals(that.registeredClientId) && principalName.equals(that.principalName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(registeredClientId, principalName);
		}
	}

}