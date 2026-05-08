package org.octri.fhir_sandbox.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Bundle;
import org.octri.fhir_sandbox.config.FhirServerProperties;
import org.octri.fhir_sandbox.config.SandboxDataConfig;
import org.octri.fhir_sandbox.util.FhirDataUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;

@Service
public class SampleDataService {

	private static final Log log = LogFactory.getLog(SampleDataService.class);

	private final SandboxDataConfig dataConfig;
	private final ResourcePatternResolver resourcePatternResolver;
	private final FhirServerProperties fhirServerProperties;
	private final FhirContext fhirContext;

	public SampleDataService(SandboxDataConfig dataConfig, ResourcePatternResolver resourcePatternResolver,
			FhirServerProperties fhirServerProperties, FhirContext fhirContext) {
		this.dataConfig = dataConfig;
		this.resourcePatternResolver = resourcePatternResolver;
		this.fhirServerProperties = fhirServerProperties;
		this.fhirContext = fhirContext;

		fhirContext.getRestfulClientFactory().setSocketTimeout(this.fhirServerProperties.getSocketTimeout());
	}

	/**
	 * Obtains sample data references, transforms them from Spring Resources to
	 * FHIR Bundle objects, then filters instances that failed to process before
	 * returning the data
	 * 
	 * @return
	 */
	public List<Bundle> getAllSampleBundles() throws IOException {
		return Stream.of(getSampleResources())
				.map(resource -> FhirDataUtil.readFhirJson(resource, Bundle.class))
				.filter(Objects::nonNull)
				.toList();
	}

	/**
	 * Uses configuration to discover sample data, returns them as an array of
	 * Spring Resource objects.
	 * 
	 * Propagates IOException if thrown by ResourcePatternResolver
	 * 
	 * @return
	 */
	private Resource[] getSampleResources() throws IOException {
		try {
			return resourcePatternResolver.getResources(dataConfig.getSampleResourcePattern());
		} catch (IOException e) {
			log.error("Error locating sample data with pattern " + dataConfig.getSampleResourcePattern(), e);
			throw e;
		}
	}

	/**
	 * Loads sample FHIR resources then posts them to the FHIR server
	 * 
	 * @param fhirUrl
	 */
	public void loadSampleData(String fhirUrl) throws IOException {
		List<Bundle> sampleData = List.of();
		sampleData = getAllSampleBundles();
		var fhirClient = fhirContext.newRestfulGenericClient(fhirUrl);
		for (var bundle : sampleData) {
			Bundle resp = fhirClient
					.transaction()
					.withBundle(bundle)
					.execute();
			// TODO: check outcome and handle failures
		}
	}
}
