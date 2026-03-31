package org.octri.fhir_sandbox.config;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.util.Assert;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * Provides beans for configuring the OAuth 2 authorization server.
 */
@Configuration
@EnableConfigurationProperties(OAuth2ServerProperties.class)
public class OAuth2ServerConfig {

	private final OAuth2ServerProperties oAuth2Properties;

	public OAuth2ServerConfig(OAuth2ServerProperties oAuth2Properties) {
		this.oAuth2Properties = oAuth2Properties;
	}

	/**
	 * Provides a JWK source for the RSA private key used to sign JWTs issued by the authorization server.
	 *
	 * @return
	 */
	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		JWK privateKey = readPrivateKey(oAuth2Properties.getPrivateKeyLocation());
		JWKSet jwkSet = new JWKSet(privateKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	/**
	 * Provides a JWT decoder that can be used to decode and validate JWTs issued by the authorization server.
	 *
	 * @param jwkSource
	 * @return
	 */
	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	/**
	 * Provides OAuth 2 authorization server configuration.
	 *
	 * @return
	 */
	@Bean
	public OAuth2AuthorizationServerConfiguration authorizationServerConfiguration() {
		return new OAuth2AuthorizationServerConfiguration();
	}

	/**
	 * Attempts to read a private key from the given resource. The location referenced should contain PEM-encoded RSA
	 * private key data in PKCS #8 format.
	 *
	 * @param location
	 *            location of the RSA private key file
	 * @return private key data
	 */
	private JWK readPrivateKey(Resource location) {
		Assert.notNull(location, "Key location cannot be null");
		try {
			var keyBytes = location.getInputStream().readAllBytes();
			return JWK.parseFromPEMEncodedObjects(new String(keyBytes, StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
