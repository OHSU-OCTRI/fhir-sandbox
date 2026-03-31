package org.octri.fhir_sandbox.oauth2.repository;

import java.util.Optional;

import org.octri.fhir_sandbox.oauth2.domain.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for OAuth2 {@link Authorization} entities.
 *
 * @see <a href="https://docs.spring.io/spring-authorization-server/reference/guides/how-to-jpa.html">Spring
 *      Authorization Server - How to Use JPA</a>
 */
@Repository
public interface AuthorizationRepository extends JpaRepository<Authorization, String> {

	Optional<Authorization> findByState(String state);

	Optional<Authorization> findByAuthorizationCodeValue(String authorizationCode);

	Optional<Authorization> findByAccessTokenValue(String accessToken);

	Optional<Authorization> findByRefreshTokenValue(String refreshToken);

	Optional<Authorization> findByOidcIdTokenValue(String idToken);

	Optional<Authorization> findByUserCodeValue(String userCode);

	Optional<Authorization> findByDeviceCodeValue(String deviceCode);

	@Query("select a from Authorization a where a.state = :token" +
			" or a.authorizationCodeValue = :token" +
			" or a.accessTokenValue = :token" +
			" or a.refreshTokenValue = :token" +
			" or a.oidcIdTokenValue = :token" +
			" or a.userCodeValue = :token" +
			" or a.deviceCodeValue = :token")
	Optional<Authorization> findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValueOrOidcIdTokenValueOrUserCodeValueOrDeviceCodeValue(
			@Param("token") String token);

}