package org.octri.fhir_sandbox.hapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.fhir_sandbox.JwtTestUtils;
import org.octri.fhir_sandbox.service.PreAuthorizedTokenService;
import org.springframework.http.HttpHeaders;

import ca.uhn.fhir.rest.client.api.IHttpRequest;

@ExtendWith(MockitoExtension.class)
public class RenewableBearerTokenInterceptorTest {

	private static final Duration TOKEN_DURATION = Duration.ofMinutes(5);
	private static final Map<String, Object> CLAIMS = Map.of("sub", "test-user");

	@Mock
	private PreAuthorizedTokenService tokenService;

	@Mock
	private IHttpRequest httpRequest;

	private RenewableBearerTokenInterceptor interceptor;

	@BeforeEach
	public void setup() {
		interceptor = new RenewableBearerTokenInterceptor(tokenService, CLAIMS, TOKEN_DURATION);
	}

	@Test
	public void testHandleClientRequestGeneratesTokenOnFirstCall() {
		String token = JwtTestUtils.buildJwt(Instant.now().plusSeconds(3600).getEpochSecond());
		when(tokenService.generateToken(CLAIMS, TOKEN_DURATION)).thenReturn(token);

		interceptor.handleClientRequest(httpRequest);

		verify(tokenService, times(1)).generateToken(CLAIMS, TOKEN_DURATION);
		verify(httpRequest).addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
	}

	@Test
	public void testHandleClientRequestReusesTokenWhenNotNearExpiration() {
		String token = JwtTestUtils.buildJwt(Instant.now().plusSeconds(3600).getEpochSecond());
		when(tokenService.generateToken(CLAIMS, TOKEN_DURATION)).thenReturn(token);

		interceptor.handleClientRequest(httpRequest);
		interceptor.handleClientRequest(httpRequest);

		// Token is generated only on the first call; reused on the second
		verify(tokenService, times(1)).generateToken(CLAIMS, TOKEN_DURATION);
		verify(httpRequest, times(2)).addHeader(eq(HttpHeaders.AUTHORIZATION), eq("Bearer " + token));
	}

	@Test
	public void testHandleClientRequestRenewsTokenWhenNearExpiration() {
		String expiringToken = JwtTestUtils.buildJwt(Instant.now().plusSeconds(10).getEpochSecond());
		String freshToken = JwtTestUtils.buildJwt(Instant.now().plusSeconds(3600).getEpochSecond());
		when(tokenService.generateToken(any(), any(Duration.class)))
				.thenReturn(expiringToken)
				.thenReturn(freshToken);

		interceptor.handleClientRequest(httpRequest);
		interceptor.handleClientRequest(httpRequest);

		// Token is renewed on both calls: first because it's null, second because it's near expiration
		verify(tokenService, times(2)).generateToken(CLAIMS, TOKEN_DURATION);
		verify(httpRequest).addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + expiringToken);
		verify(httpRequest).addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + freshToken);
	}

}
