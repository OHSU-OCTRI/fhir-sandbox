package org.octri.fhir_sandbox.oauth2.customizer;

import java.time.Instant;
import java.util.Base64;

import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * Custom {@link OAuth2TokenGenerator} that allows {@link OAuth2RefreshToken}s to be generated for public clients that
 * use {@link ClientAuthenticationMethod.NONE}.
 *
 * This code is based on examples from the default implementation in Spring Security and a medium.com article.
 *
 * @see <a
 *      href=
 *      "https://medium.com/@afeefrazickamir/spring-authorization-server-public-client-pkce-authorization-code-flow-with-refresh-tokens-ac2763080898">Spring
 *      Authorization Server — Public Client PKCE Authorization code flow (with refresh tokens) at medium.com</a>
 * @see <a href=
 *      "https://github.com/spring-projects/spring-security/blob/main/oauth2/oauth2-authorization-server/src/main/java/org/springframework/security/oauth2/server/authorization/token/OAuth2RefreshTokenGenerator.java">Spring
 *      Security's default OAuth2RefreshTokenGenerator.java</a>
 */
public class PublicClientAllowingRefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {

	private final StringKeyGenerator refreshTokenGenerator = new Base64StringKeyGenerator(
			Base64.getUrlEncoder().withoutPadding(), 96);

	@Override
	public OAuth2RefreshToken generate(OAuth2TokenContext context) {
		if (!OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
			return null;
		}

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(context.getRegisteredClient().getTokenSettings().getRefreshTokenTimeToLive());
		return new OAuth2RefreshToken(this.refreshTokenGenerator.generateKey(), issuedAt, expiresAt);
	}

}
