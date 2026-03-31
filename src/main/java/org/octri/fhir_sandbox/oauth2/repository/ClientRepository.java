package org.octri.fhir_sandbox.oauth2.repository;

import java.util.Optional;

import org.octri.fhir_sandbox.oauth2.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for OAuth2 {@link Client} entities.
 *
 * @see <a href="https://docs.spring.io/spring-authorization-server/reference/guides/how-to-jpa.html">Spring
 *      Authorization Server - How to Use JPA</a>
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

	Optional<Client> findByClientId(String clientId);

}