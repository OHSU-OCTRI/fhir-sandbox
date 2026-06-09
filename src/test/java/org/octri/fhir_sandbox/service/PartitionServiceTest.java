package org.octri.fhir_sandbox.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.fhir_sandbox.config.FhirServerProperties;
import org.octri.fhir_sandbox.domain.Sandbox;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IOperation;
import ca.uhn.fhir.rest.gclient.IOperationUnnamed;
import ca.uhn.fhir.rest.gclient.IOperationUntyped;
import ca.uhn.fhir.rest.gclient.IOperationUntypedWithInputAndPartialOutput;

@ExtendWith(MockitoExtension.class)
public class PartitionServiceTest {

	private static final String BASE_URL = "http://fhir-server:8080/fhir";
	private static final String DEFAULT_PARTITION = "DEFAULT";

	@Mock
	private FhirServerProperties fhirServerProperties;
	@Mock
	private FhirContext fhirContext;
	@Mock
	private PreAuthorizedTokenService tokenService;
	@Mock
	private IGenericClient fhirClient;
	@Mock
	private IOperation operation;
	@Mock
	private IOperationUnnamed operationUnnamed;
	@Mock
	private IOperationUntyped operationUntyped;
	@SuppressWarnings("rawtypes")
	@Mock
	private IOperationUntypedWithInputAndPartialOutput operationWithInput;

	@InjectMocks
	private PartitionService service;

	@BeforeEach
	@SuppressWarnings("unchecked")
	public void setup() {
		when(fhirServerProperties.getBaseUrl()).thenReturn(BASE_URL);
	}

	private void setupFhirClient() {
		when(tokenService.generateToken(anyMap(), any(Duration.class))).thenReturn("mock_token");
		when(fhirContext.newRestfulGenericClient(anyString())).thenReturn(fhirClient);
		when(fhirClient.operation()).thenReturn(operation);
		when(operation.onServer()).thenReturn(operationUnnamed);
		when(operationUnnamed.named(anyString())).thenReturn(operationUntyped);
		when(operationUntyped.withParameters(any())).thenReturn(operationWithInput);
	}

	@Test
	public void testGetPartitionFhirUrl() {
		String url = service.getPartitionFhirUrl("myPartition");
		assertEquals(BASE_URL + "/myPartition/", url);
	}

	@Test
	public void testGetDefaultPartitionUrl() {
		when(fhirServerProperties.getDefaultPartition()).thenReturn(DEFAULT_PARTITION);
		String url = service.getDefaultPartitionUrl();
		assertEquals(BASE_URL + "/" + DEFAULT_PARTITION + "/", url);
	}

	@Test
	public void testCreatePartitionForSandboxSetsPartitionId() {
		setupFhirClient();
		when(fhirServerProperties.getDefaultPartition()).thenReturn(DEFAULT_PARTITION);
		var response = new Parameters();
		response.addParameter("id", new IntegerType(42));
		when(operationWithInput.execute()).thenReturn(response);

		var sandbox = new Sandbox();
		sandbox.setDescription("Test Sandbox");
		service.createPartitionForSandbox(sandbox);

		assertEquals(42L, sandbox.getServerPartitionId());
	}

	@Test
	public void testCreatePartitionForSandboxCallsCorrectOperation() {
		setupFhirClient();
		when(fhirServerProperties.getDefaultPartition()).thenReturn(DEFAULT_PARTITION);
		var response = new Parameters();
		response.addParameter("id", new IntegerType(1));
		when(operationWithInput.execute()).thenReturn(response);

		var sandbox = new Sandbox();
		sandbox.setDescription("Test Sandbox");
		service.createPartitionForSandbox(sandbox);

		verify(operationUnnamed).named("$partition-management-create-partition");
	}

	@Test
	public void testDeletePartitionForSandboxCallsCorrectOperation() {
		setupFhirClient();
		when(fhirServerProperties.getDefaultPartition()).thenReturn(DEFAULT_PARTITION);
		when(operationWithInput.execute()).thenReturn(new Parameters());

		var sandbox = new Sandbox();
		sandbox.setServerPartitionId(7L);
		service.deletePartitionForSandbox(sandbox);

		verify(operationUnnamed).named("$partition-management-delete-partition");
	}

}
