package org.octri.fhir_sandbox.service;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.octri.fhir_sandbox.config.FhirServerProperties;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import ca.uhn.fhir.context.FhirContext;

@Service
public class PartitionService {

	private final FhirServerProperties fhirServerProperties;
	private final FhirContext fhirContext;

	public PartitionService(FhirServerProperties fhirServerProperties, FhirContext fhirContext) {
		this.fhirContext = fhirContext;
		this.fhirServerProperties = fhirServerProperties;
	}

	/**
	 * Creates a new partition on the FHIR server for the given sandbox.
	 *
	 * @param sandbox
	 */
	public void createPartitionForSandbox(Sandbox sandbox) {
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
		var partitionId = (IntegerType) response.getParameter("id").getValue();
		sandbox.setServerPartitionId(partitionId.getValue().longValue());
	}

	/**
	 * Deletes the partition on the FHIR server for the given sandbox.
	 *
	 * @param sandbox
	 */
	public void deletePartitionForSandbox(Sandbox sandbox) {
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

	/**
	 * Constructs the FHIR server URL for the default partition, which is based on the base URL from the application
	 * properties and the default partition name from the application properties.
	 *
	 * @return
	 */
	public String getDefaultPartitionUrl() {
		return getPartitionFhirUrl(fhirServerProperties.getDefaultPartition());
	}

	/**
	 * Constructs the FHIR server URL for the given partition name, which is based on the base URL from the application
	 * properties and the given partition name.
	 *
	 * @param partitionName
	 * @return
	 */
	public String getPartitionFhirUrl(String partitionName) {
		var uriBuilder = UriComponentsBuilder.fromUriString(fhirServerProperties.getBaseUrl());
		uriBuilder.pathSegment(partitionName).path("/");
		return uriBuilder.build().toUriString();
	}
}
