package org.octri.fhir_sandbox.oauth2.customizer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;

@ExtendWith(MockitoExtension.class)
public class PublicClientAllowingRefreshTokenGeneratorTest {

	private static final Duration TOKEN_TTL = Duration.ofHours(1);

	@Mock
	private OAuth2TokenContext context;

	@Mock
	private RegisteredClient registeredClient;

	@Mock
	private TokenSettings tokenSettings;

	private PublicClientAllowingRefreshTokenGenerator generator() {
		return new PublicClientAllowingRefreshTokenGenerator();
	}

	private void setupRefreshTokenContext() {
		when(context.getTokenType()).thenReturn(OAuth2TokenType.REFRESH_TOKEN);
		when(context.getRegisteredClient()).thenReturn(registeredClient);
		when(registeredClient.getTokenSettings()).thenReturn(tokenSettings);
		when(tokenSettings.getRefreshTokenTimeToLive()).thenReturn(TOKEN_TTL);
	}

	@Test
	public void testGenerateReturnsNullForAccessToken() {
		when(context.getTokenType()).thenReturn(OAuth2TokenType.ACCESS_TOKEN);

		assertNull(generator().generate(context));
	}

	@Test
	public void testGenerateReturnsNullForNonRefreshTokenType() {
		when(context.getTokenType()).thenReturn(new OAuth2TokenType("id_token"));

		assertNull(generator().generate(context));
	}

	@Test
	public void testGenerateReturnsTokenForRefreshTokenType() {
		setupRefreshTokenContext();

		assertNotNull(generator().generate(context));
	}

	@Test
	public void testGeneratedTokenValueIsNotBlank() {
		setupRefreshTokenContext();

		OAuth2RefreshToken token = generator().generate(context);

		assertNotNull(token.getTokenValue());
		assertFalse(token.getTokenValue().isBlank());
	}

	@Test
	public void testGeneratedTokenIssuedAtIsApproximatelyNow() {
		setupRefreshTokenContext();
		Instant before = Instant.now();

		OAuth2RefreshToken token = generator().generate(context);

		Instant after = Instant.now();
		assertNotNull(token.getIssuedAt());
		assertFalse(token.getIssuedAt().isBefore(before), "issuedAt should not be before the test started");
		assertFalse(token.getIssuedAt().isAfter(after), "issuedAt should not be after the test ended");
	}

	@Test
	public void testGeneratedTokenExpiresAtMatchesTimeToLive() {
		setupRefreshTokenContext();

		OAuth2RefreshToken token = generator().generate(context);

		assertNotNull(token.getExpiresAt());
		Duration actualTtl = Duration.between(token.getIssuedAt(), token.getExpiresAt());
		assertTrue(TOKEN_TTL.equals(actualTtl), "expiresAt should be issuedAt plus the configured TTL");
	}

	@Test
	public void testEachGeneratedTokenHasUniqueValue() {
		setupRefreshTokenContext();

		OAuth2RefreshToken first = generator().generate(context);
		OAuth2RefreshToken second = generator().generate(context);

		assertFalse(first.getTokenValue().equals(second.getTokenValue()),
				"Each generated token should have a unique value");
	}

}
