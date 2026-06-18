package org.octri.fhir_sandbox.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.fhir_sandbox.config.OAuth2ServerProperties;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.SignedJWT;

@ExtendWith(MockitoExtension.class)
public class PreAuthorizedTokenServiceTest {

	private static final String TEST_ISSUER = "https://example.com/issuer";

	@Mock
	private OAuth2ServerProperties serverProperties;

	private PreAuthorizedTokenService service;

	@BeforeEach
	public void setUp() throws Exception {
		var rsaKey = new RSAKeyGenerator(2048).generate();
		var jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
		var jwtEncoder = new NimbusJwtEncoder(jwkSource);
		when(serverProperties.getIssuerUrl()).thenReturn(TEST_ISSUER);
		service = new PreAuthorizedTokenService(serverProperties, jwtEncoder);
	}

	@Test
	public void generateTokenReturnsSignedJwtString() {
		var token = service.generateToken(Map.of("sub", "user1"));
		assertNotNull(token, "Token should not be null");
		assertEquals(3, token.split("\\.").length, "Token should have three dot-separated segments");
	}

	@Test
	public void generateTokenIncludesProvidedClaims() throws ParseException {
		var token = service.generateToken(Map.of("sub", "user1", "fhirUser", "Patient/123"));
		var claims = SignedJWT.parse(token).getJWTClaimsSet();
		assertEquals("user1", claims.getSubject(), "Token should include the provided sub claim");
		assertEquals("Patient/123", claims.getStringClaim("fhirUser"),
				"Token should include the provided fhirUser claim");
	}

	@Test
	public void generateTokenSetsIssuerFromServerProperties() throws ParseException {
		var token = service.generateToken(Map.of());
		var claims = SignedJWT.parse(token).getJWTClaimsSet();
		assertEquals(TEST_ISSUER, claims.getIssuer(), "Issuer should come from OAuth2ServerProperties");
	}

	@Test
	public void generateTokenSetsJtiClaim() throws ParseException {
		var token = service.generateToken(Map.of());
		var claims = SignedJWT.parse(token).getJWTClaimsSet();
		var jti = claims.getJWTID();
		assertNotNull(jti, "Token should have a jti claim");
		assertDoesNotThrow(() -> UUID.fromString(jti), "jti should be a valid UUID");
	}

	@Test
	public void generateTokenJtiIsUniquePerCall() throws ParseException {
		var token1 = service.generateToken(Map.of());
		var token2 = service.generateToken(Map.of());
		var jti1 = SignedJWT.parse(token1).getJWTClaimsSet().getJWTID();
		var jti2 = SignedJWT.parse(token2).getJWTClaimsSet().getJWTID();
		assertNotEquals(jti1, jti2, "Each token should have a unique jti");
	}

	@Test
	public void generateTokenDefaultsToOneHourExpiry() throws ParseException {
		var before = Instant.now();
		var token = service.generateToken(Map.of());
		var after = Instant.now();

		var claims = SignedJWT.parse(token).getJWTClaimsSet();
		var exp = claims.getExpirationTime().toInstant();
		var iat = claims.getIssueTime().toInstant();

		assertTrue(exp.isAfter(before.plus(Duration.ofHours(1)).minusSeconds(5)),
				"Expiry should be approximately one hour after issuance");
		assertTrue(exp.isBefore(after.plus(Duration.ofHours(1)).plusSeconds(5)),
				"Expiry should be approximately one hour after issuance");
		assertTrue(exp.isAfter(iat), "Expiry should be after issuance time");
	}

	@Test
	public void generateTokenWithDurationUsesSuppliedDuration() throws ParseException {
		var duration = Duration.ofMinutes(30);
		var before = Instant.now();
		var token = service.generateToken(Map.of(), duration);
		var after = Instant.now();

		var claims = SignedJWT.parse(token).getJWTClaimsSet();
		var exp = claims.getExpirationTime().toInstant();

		assertTrue(exp.isAfter(before.plus(duration).minusSeconds(5)),
				"Expiry should be approximately 30 minutes after issuance");
		assertTrue(exp.isBefore(after.plus(duration).plusSeconds(5)),
				"Expiry should be approximately 30 minutes after issuance");
	}

	@Test
	public void generateTokenSetsIssuedAtClaim() throws ParseException {
		var before = Instant.now().minusSeconds(1);
		var token = service.generateToken(Map.of());
		var after = Instant.now().plusSeconds(1);

		var iat = SignedJWT.parse(token).getJWTClaimsSet().getIssueTime().toInstant();
		assertNotNull(iat, "iat claim should be present");
		assertTrue(iat.isAfter(before) && iat.isBefore(after), "iat should be close to the time of token generation");
	}

}
