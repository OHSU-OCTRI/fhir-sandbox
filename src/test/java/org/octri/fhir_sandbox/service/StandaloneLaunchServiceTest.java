package org.octri.fhir_sandbox.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.authentication.server.security.entity.User;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.domain.SmartClient;
import org.octri.fhir_sandbox.exception.DisplayedException;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class StandaloneLaunchServiceTest {

	private static final String CLIENT_ID = "test-client-id";
	private static final String USERNAME = "testuser";
	private static final String FHIR_URL = "http://localhost:8001/fhir/test-partition/";
	private static final String ACCESS_TOKEN = "test-access-token";

	@Mock
	private SmartClientService clientService;
	@Mock
	private SandboxService sandboxService;
	@Mock
	private PreAuthorizedTokenService preAuthorizedTokenService;

	@Mock
	private User currentUser;
	@Mock
	private Sandbox sandbox;
	@Mock
	private SmartClient client;

	@Captor
	private ArgumentCaptor<Map<String, Object>> claimsCaptor;

	@InjectMocks
	StandaloneLaunchService service;

	@Test
	public void testClientNotFound() {
		when(clientService.findSmartClientByClientId(CLIENT_ID)).thenReturn(Optional.empty());

		var ex = assertThrows(DisplayedException.class,
				() -> service.validateAndGetPickerData(CLIENT_ID, currentUser, USERNAME));

		assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
	}

	@Test
	public void testUserLacksSandboxAccess() {
		when(clientService.findSmartClientByClientId(CLIENT_ID)).thenReturn(Optional.of(client));
		when(client.getSandbox()).thenReturn(sandbox);
		when(sandboxService.getSandboxesForUser(currentUser)).thenReturn(List.of());

		var ex = assertThrows(DisplayedException.class,
				() -> service.validateAndGetPickerData(CLIENT_ID, currentUser, USERNAME));

		assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
	}

	@Test
	public void testSuccessReturnsPickerData() {
		when(clientService.findSmartClientByClientId(CLIENT_ID)).thenReturn(Optional.of(client));
		when(client.getSandbox()).thenReturn(sandbox);
		when(sandboxService.getSandboxesForUser(currentUser)).thenReturn(List.of(sandbox));
		when(sandboxService.getSandboxFhirUrl(sandbox)).thenReturn(FHIR_URL);
		when(preAuthorizedTokenService.generateToken(anyMap())).thenReturn(ACCESS_TOKEN);

		var result = service.validateAndGetPickerData(CLIENT_ID, currentUser, USERNAME);

		assertEquals(client, result.client());
		assertEquals(FHIR_URL, result.fhirServerUrl());
		assertEquals(ACCESS_TOKEN, result.accessToken());
	}

	@Test
	public void testSuccessTokenClaimsAreCorrect() {
		when(clientService.findSmartClientByClientId(CLIENT_ID)).thenReturn(Optional.of(client));
		when(client.getSandbox()).thenReturn(sandbox);
		when(sandboxService.getSandboxesForUser(currentUser)).thenReturn(List.of(sandbox));
		when(sandboxService.getSandboxFhirUrl(sandbox)).thenReturn(FHIR_URL);
		when(preAuthorizedTokenService.generateToken(claimsCaptor.capture())).thenReturn(ACCESS_TOKEN);

		service.validateAndGetPickerData(CLIENT_ID, currentUser, USERNAME);

		var claims = claimsCaptor.getValue();
		assertEquals(USERNAME, claims.get("sub"));
		assertEquals(FHIR_URL, claims.get("aud"));
		assertEquals(List.of("user/*.cruds"), claims.get("scope"));
	}

}
