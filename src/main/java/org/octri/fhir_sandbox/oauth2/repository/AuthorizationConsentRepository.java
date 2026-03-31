package org.octri.fhir_sandbox.oauth2.repository;

import java.util.Optional;

import org.octri.fhir_sandbox.oauth2.domain.AuthorizationConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for OAuth2 {@link AuthorizationConsent} entities.
 *
 * see: <a href="https://docs.spring.io/spring-authorization-server/reference/guides/how-to-jpa.html">Spring
 * Authorization Server - How to Use JPA</a>
 */
@Repository
public interface AuthorizationConsentRepository
		extends JpaRepository<AuthorizationConsent, AuthorizationConsent.AuthorizationConsentId> {

	Optional<AuthorizationConsent> findByRegisteredClientIdAndPrincipalName(String registeredClientId,
			String principalName);

	void deleteByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);

}