package org.octri.fhir_sandbox.hapi;

import java.time.Duration;
import java.util.Map;

import org.octri.fhir_sandbox.oauth2.utils.OAuthUtils;
import org.octri.fhir_sandbox.service.PreAuthorizedTokenService;
import org.springframework.http.HttpHeaders;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.client.api.IHttpRequest;

/**
 * HAPI FHIR client interceptor that provides a bearer token header and ensures that the token does not expire as
 * multiple requests are made.
 */
@Interceptor
public class RenewableBearerTokenInterceptor {

	private final PreAuthorizedTokenService tokenService;
	private final Map<String, Object> claims;
	private final Duration tokenDuration;

	private String bearerToken;

	/**
	 * Constructor.
	 *
	 * @param tokenService
	 * @param claims
	 * @param tokenDuration
	 */
	public RenewableBearerTokenInterceptor(PreAuthorizedTokenService tokenService, Map<String, Object> claims,
			Duration tokenDuration) {
		this.tokenService = tokenService;
		this.claims = claims;
		this.tokenDuration = tokenDuration;
	}

	/**
	 * Adds an {@code Authorization} header with an unexpired bearer token to the request.
	 *
	 * @param request
	 */
	@Hook(Pointcut.CLIENT_REQUEST)
	public void handleClientRequest(IHttpRequest request) {
		if (renewToken(bearerToken)) {
			bearerToken = tokenService.generateToken(claims, tokenDuration);
		}

		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
	}

	/**
	 * Reports whether the JWT bearer token needs to be renewed. Returns true if the token is null, malformed, or within
	 * 30 seconds of expiration. Otherwise returns false.
	 *
	 * @param bearerToken
	 * @return true if the token is malformed or near expiration, false otherwise
	 */
	private boolean renewToken(String bearerToken) {
		return OAuthUtils.isTokenNearExpiration(bearerToken);
	}

}
