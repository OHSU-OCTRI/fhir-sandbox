package org.octri.fhir_sandbox.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.domain.SandboxStatus;
import org.octri.fhir_sandbox.repository.SandboxRepository;

@ExtendWith(MockitoExtension.class)
public class SandboxServiceTest {

	@Mock
	private SandboxRepository sandboxRepository;
	@Mock
	private PartitionService partitionService;
	@Mock
	private SampleDataService sampleDataService;
	@Spy
	private Sandbox spySandbox;

	@InjectMocks
	SandboxService service;

	@BeforeEach
	public void setup() {
		spySandbox.setId(42l);	// Prevent SandboxService.save from calling createSandbox
		spySandbox.setDescription("Test Sandbox");
		when(sandboxRepository.save(any(Sandbox.class))).thenAnswer(i -> i.getArgument(0));
	}

	@Test
	public void testCreateSandboxStatus() {
		var sandbox = service.createSandbox(spySandbox);

		assertEquals(SandboxStatus.INITIALIZING, sandbox.getStatus(),
				"Create sandbox must set status to INITIALIZING");
	}

	@Test
	public void testInitializeSandboxNoSample() {
		service.initializeSandbox(spySandbox, false);

		verify(sampleDataService, never()).loadSampleData(any());
		assertEquals(SandboxStatus.READY, spySandbox.getStatus(),
				"Sandbox without sample data should immediately become READY");
	}

	@Test
	public void testInitializeSandboxSampleSuccessful() {
		service.initializeSandbox(spySandbox, true);

		verify(sampleDataService).loadSampleData(any());
		assertEquals(SandboxStatus.READY, spySandbox.getStatus(),
				"Sandbox status should be ready once data is loaded");
	}

	@Test
	public void testInitializeSandboxSampleFailure() {
		doThrow(new UncheckedIOException(new IOException())).when(sampleDataService).loadSampleData(any());
		service.initializeSandbox(spySandbox, true);

		verify(sampleDataService).loadSampleData(any());
		assertEquals(SandboxStatus.ERROR, spySandbox.getStatus(),
				"Expect ERROR status when the data import fails");
	}

	@Test
	public void testInitializeSandboxStatusTiming() {
		InOrder order = inOrder(sampleDataService, spySandbox);
		service.initializeSandbox(spySandbox, true);

		order.verify(sampleDataService).loadSampleData(any());
		order.verify(spySandbox).setStatus(any());
		assertEquals(SandboxStatus.READY, spySandbox.getStatus(),
				"Sandbox should load data before updating status");
	}
}
