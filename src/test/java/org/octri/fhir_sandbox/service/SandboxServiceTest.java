package org.octri.fhir_sandbox.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.authentication.server.security.entity.User;
import org.octri.fhir_sandbox.config.FhirServerProperties;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.domain.SandboxStatus;
import org.octri.fhir_sandbox.repository.SandboxRepository;
import org.octri.fhir_sandbox.repository.SmartClientRepository;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IOperation;
import ca.uhn.fhir.rest.gclient.IOperationUnnamed;
import ca.uhn.fhir.rest.gclient.IOperationUntyped;
import ca.uhn.fhir.rest.gclient.IOperationUntypedWithInputAndPartialOutput;

@ExtendWith(MockitoExtension.class)
public class SandboxServiceTest {

	@Mock
	private SandboxRepository sandboxRepository;
	@Mock
	private SmartClientRepository sandboxClientRepository;
	@Mock
	private SampleDataService sampleDataService;
	@Mock
	private User mockUser;

	// Mocks for the fhirClient and its fluent method steps
	@Mock
	private FhirServerProperties fhirServerProperties;
	@Mock
	private FhirContext fhirContext;
	@Mock
	private IGenericClient fhirClient;
	@Mock
	private IOperation operationInterface;
	@Mock
	private IOperationUnnamed serverActionInterface;
	@Mock
	private IOperationUntyped namedOperationInterface;
	@Mock
	private IOperationUntypedWithInputAndPartialOutput<Parameters> operationParametersInterface;
	@Mock
	private Parameters mockResponse;

	@InjectMocks
	SandboxService service;

	private Sandbox entity;

	@BeforeEach
	public void setup() {
		entity = new Sandbox();
		entity.setDescription("Test Sandbox");
		entity.setOwner(mockUser);

		// Mock repository interaction
		when(sandboxRepository.save(any(Sandbox.class))).thenAnswer(i -> i.getArgument(0));
	}

	private void mockFhirContext() {
		when(fhirServerProperties.getDefaultPartition()).thenReturn("DEFAULT");
		when(fhirServerProperties.getBaseUrl()).thenReturn("http://localhost:8001/fhir");
		when(fhirContext.newRestfulGenericClient(anyString())).thenReturn(fhirClient);
	}

	private void mockCreatePartition() {
		// Mock the fhirClient fluent methods used to create partition
		when(fhirClient.operation()).thenReturn(operationInterface);
		when(operationInterface.onServer()).thenReturn(serverActionInterface);
		when(serverActionInterface.named(anyString())).thenReturn(namedOperationInterface);
		when(namedOperationInterface.withParameters(any(Parameters.class))).thenReturn(operationParametersInterface);
		when(operationParametersInterface.execute()).thenReturn(mockResponse);

		// Mock the FHIR server response
		var param = new ParametersParameterComponent();
		param.setValue(new IntegerType(42));
		when(mockResponse.getParameter("id")).thenReturn(param);
	}

	@Test
	public void testCreateSandboxStatus() {
		mockFhirContext();
		mockCreatePartition();
		var sandbox = service.createSandbox(entity);
		assertEquals(sandbox.getStatus(), SandboxStatus.INITIALIZING);
	}

}
