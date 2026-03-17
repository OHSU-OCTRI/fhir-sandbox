package org.octri.fhir_sandbox.service;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.octri.fhir_sandbox.config.FhirServerProperties;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.repository.SandboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import ca.uhn.fhir.context.FhirContext;

@Service
public class SandboxService {

	private static final Logger log = LoggerFactory.getLogger(SandboxService.class);

	private final SandboxRepository repository;
	private final FhirServerProperties fhirServerProperties;
	private final FhirContext fhirContext;

	public SandboxService(SandboxRepository repository, FhirServerProperties fhirServerProperties,
			FhirContext fhirContext) {
		this.repository = repository;
		this.fhirServerProperties = fhirServerProperties;
		this.fhirContext = fhirContext;
	}

	public Sandbox createSandbox(Sandbox sandbox) {
		log.info(sandbox.toString());
		createPartitionForSandbox(sandbox);
		Sandbox savedSandbox = repository.save(sandbox);
		return savedSandbox;
	}

	// TODO: Use method-level security to restrict deletion to admins or sandbox owner.
	public void deleteSandbox(Sandbox sandbox) {
		deletePartitionForSandbox(sandbox);
		repository.delete(sandbox);
	}

	private void createPartitionForSandbox(Sandbox sandbox) {
		var fhirClient = fhirContext.newRestfulGenericClient(getDefaultPartitionUrl());
		var parameters = new Parameters();

		// TODO: use constants for parameter names
		parameters.addParameter("name", new CodeType(sandbox.getServerPartitionName()));
		parameters.addParameter("description", new StringType(sandbox.getDescription()));

		// TODO: use constants for operation name
		var response = fhirClient
				.operation()
				.onServer()
				.named("$partition-management-create-partition")
				.withParameters(parameters).execute();

		// TODO: check for and handle errors
		IntegerType partitionId = (IntegerType) response.getParameter("id").getValue();
		sandbox.setServerPartitionId(partitionId.getValue().longValue());
	}

	private void deletePartitionForSandbox(Sandbox sandbox) {
		var fhirClient = fhirContext.newRestfulGenericClient(getDefaultPartitionUrl());
		var parameters = new Parameters();
		parameters.addParameter().setName("id").setValue(new IntegerType(sandbox.getServerPartitionId()));
		var response = fhirClient
				.operation()
				.onServer()
				.named("$partition-management-delete-partition")
				.withParameters(parameters).execute();
		// TODO: check and handle errors
	}

	public String getSandboxFhirUrl(Sandbox sandbox) {
		return getSandboxFhirUrl(sandbox.getServerPartitionName());
	}

	public String getDefaultPartitionUrl() {
		return getSandboxFhirUrl(fhirServerProperties.getDefaultPartition());
	}

	private String getSandboxFhirUrl(String partitionName) {
		var uriBuilder = UriComponentsBuilder.fromUriString(fhirServerProperties.getBaseUrl());
		uriBuilder.pathSegment(partitionName).path("/");
		return uriBuilder.build().toUriString();
	}

}
