package org.octri.fhir_sandbox.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.octri.fhir_sandbox.config.OAuth2ServerProperties;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Generates signed JWT strings from an arbitrary claims payload using the authorization server's RSA key.
 */
@Service
public class PreAuthorizedTokenService {

	private final OAuth2ServerProperties serverProperties;
	private final JwtEncoder jwtEncoder;

	public PreAuthorizedTokenService(OAuth2ServerProperties serverProperties, JwtEncoder jwtEncoder) {
		this.serverProperties = serverProperties;
		this.jwtEncoder = jwtEncoder;
	}

	/**
	 * Generates a signed JWT from the given claims, valid for one hour.
	 *
	 * @param claims
	 *            token payload
	 * @return signed JWT string
	 */
	public String generateToken(Map<String, Object> claims) {
		return generateToken(claims, Duration.ofHours(1L));
	}

	/**
	 * Generates a signed JWT from the given claims, valid until <code>tokenDuration</code> from now.
	 *
	 * @param claims
	 *            token payload
	 * @return signed JWT string
	 */
	public String generateToken(Map<String, Object> claims, Duration tokenDuration) {
		var now = Instant.now();
		var claimsSet = JwtClaimsSet.builder()
				.issuer(serverProperties.getIssuerUrl())
				.id(UUID.randomUUID().toString())
				.issuedAt(now)
				.expiresAt(now.plus(tokenDuration))
				.claims(c -> c.putAll(claims))
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
	}

}
