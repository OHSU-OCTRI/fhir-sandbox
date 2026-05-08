package org.octri.fhir_sandbox.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.authentication.server.security.entity.User;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.domain.SandboxStatus;
import org.octri.fhir_sandbox.repository.SandboxRepository;

@ExtendWith(MockitoExtension.class)
public class SandboxServiceTest {

	@Mock
	private SandboxRepository sandboxRepository;
	@Mock
	private PartitionService partitionService;

	@InjectMocks
	SandboxService service;

	private Sandbox entity;

	@BeforeEach
	public void setup() {
		entity = new Sandbox();
		entity.setDescription("Test Sandbox");

		// Mock repository interaction
		when(sandboxRepository.save(any(Sandbox.class))).thenAnswer(i -> i.getArgument(0));
	}

	@Test
	public void testCreateSandboxStatus() {
		var sandbox = service.createSandbox(entity);
		assertEquals(sandbox.getStatus(), SandboxStatus.INITIALIZING);
	}

}
