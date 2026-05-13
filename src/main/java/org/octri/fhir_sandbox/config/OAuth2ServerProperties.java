package org.octri.fhir_sandbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * Configuration properties for the OAuth 2 authorization server.
 */
@ConfigurationProperties(prefix = "octri.sandbox.oauth2")
public class OAuth2ServerProperties {

	private String issuerUrl;
	private Resource privateKeyLocation;

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

	@Override
	public String toString() {
		return "OAuth2Properties [privateKeyLocation=" + privateKeyLocation + "]";
	}

}
