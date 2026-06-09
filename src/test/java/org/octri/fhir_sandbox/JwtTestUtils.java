package org.octri.fhir_sandbox;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility methods for constructing unsigned JWT strings in tests.
 */
public class JwtTestUtils {

	/**
	 * Builds a minimal JWT with an {@code exp} claim set to the given epoch second. The signature segment is a
	 * placeholder and is not cryptographically valid.
	 *
	 * @param expEpochSeconds
	 *            token expiration as seconds since the Unix epoch
	 * @return a dot-separated JWT string
	 */
	public static String buildJwt(long expEpochSeconds) {
		String header = Base64.getUrlEncoder().withoutPadding()
				.encodeToString("{\"alg\":\"RS256\"}".getBytes(StandardCharsets.UTF_8));
		String payloadJson = "{\"sub\":\"user\",\"exp\":" + expEpochSeconds + "}";
		String payload = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
		return header + "." + payload + ".fakesignature";
	}

	/**
	 * Builds a minimal JWT with no {@code exp} claim. The signature segment is a placeholder and is not
	 * cryptographically valid.
	 *
	 * @return a dot-separated JWT string
	 */
	public static String buildJwtWithoutExp() {
		String header = Base64.getUrlEncoder().withoutPadding()
				.encodeToString("{\"alg\":\"RS256\"}".getBytes(StandardCharsets.UTF_8));
		String payload = Base64.getUrlEncoder().withoutPadding()
				.encodeToString("{\"sub\":\"user\"}".getBytes(StandardCharsets.UTF_8));
		return header + "." + payload + ".fakesignature";
	}

}
