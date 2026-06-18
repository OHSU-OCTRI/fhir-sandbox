package org.octri.fhir_sandbox.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * Configuration properties for the OAuth 2 authorization server.
 */
@ConfigurationProperties(prefix = "octri.sandbox.oauth2")
public class OAuth2ServerProperties {

	private String issuerUrl;
	private Resource privateKeyLocation;
	private Duration accessTokenTtl = Duration.ofMinutes(20);
	private Duration refreshTokenTtl = Duration.ofHours(4);

	/**
	 * Gets the URL to use in the JWT issuer <code>iss</code> claim.
	 *
	 * @return
	 */
	public String getIssuerUrl() {
		return issuerUrl;
	}

	/**
	 * Sets the URL to use in the JWT issuer <code>iss</code> claim.
	 *
	 * @param issuerUrl
	 */
	public void setIssuerUrl(String issuerUrl) {
		this.issuerUrl = issuerUrl;
	}

	/**
	 * Gets the location of the RSA private key used to sign JWTs.
	 *
	 * @return
	 */
	public Resource getPrivateKeyLocation() {
		return privateKeyLocation;
	}

	/**
	 * Sets the location of the RSA private key used to sign JWTs.
	 *
	 * @param privateKeyLocation
	 */
	public void setPrivateKeyLocation(Resource privateKeyLocation) {
		this.privateKeyLocation = privateKeyLocation;
	}

	/**
	 * Gets the time that access tokens will be valid. Defaults to 20 minutes.
	 *
	 * @return
	 */
	public Duration getAccessTokenTtl() {
		return accessTokenTtl;
	}

	/**
	 * Sets the time that access tokens will be valid. Allows formats supported by Spring Boot duration properties, e.g.
	 * "20m", "1d", etc.
	 *
	 * @see <a
	 *      href=
	 *      "https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.durations">Spring
	 *      Boot duration property conversion</a>
	 * @param accessTokenTtl
	 */
	public void setAccessTokenTtl(Duration accessTokenTtl) {
		this.accessTokenTtl = accessTokenTtl;
	}

	/**
	 * Gets the time that refresh tokens will be valid. Defaults to 4 hours.
	 *
	 * @return
	 */
	public Duration getRefreshTokenTtl() {
		return refreshTokenTtl;
	}

	/**
	 * Sets the time that refresh tokens will be valid. Allows formats supported by Spring Boot duration properties,
	 * e.g. "20m", "1d", etc.
	 *
	 * @see <a
	 *      href=
	 *      "https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.durations">Spring
	 *      Boot duration property conversion</a>
	 * @param accessTokenTtl
	 */
	public void setRefreshTokenTtl(Duration refreshTokenTtl) {
		this.refreshTokenTtl = refreshTokenTtl;
	}

	@Override
	public String toString() {
		return "OAuth2ServerProperties [issuerUrl=" + issuerUrl + ", privateKeyLocation=" + privateKeyLocation
				+ ", accessTokenTtl=" + accessTokenTtl + ", refreshTokenTtl=" + refreshTokenTtl + "]";
	}

}
