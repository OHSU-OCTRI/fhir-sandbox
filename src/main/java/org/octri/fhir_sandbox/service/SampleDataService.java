package org.octri.fhir_sandbox.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Bundle;
import org.octri.fhir_sandbox.config.SandboxDataConfig;
import org.octri.fhir_sandbox.util.FhirDataUtil;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

@Service
public class SampleDataService {

	private static final Log log = LogFactory.getLog(SampleDataService.class);

	private final SandboxDataConfig dataConfig;
	private final ResourcePatternResolver resourcePatternResolver;

	public SampleDataService(SandboxDataConfig dataConfig, ResourcePatternResolver resourcePatternResolver) {
		this.dataConfig = dataConfig;
		this.resourcePatternResolver = resourcePatternResolver;
	}

	/**
	 * Locates FHIR bundle files in the configured samples directory, parses the
	 * data, then filters data that failed to process before returning the data
	 * 
	 * Returns an empty list if there was an error reading the samples directory
	 * 
	 * @return
	 */
	public List<Bundle> getAllSampleBundles() {
		try {
			// Get the sample file resources and parse them into FHIR Bundles
			var resources = resourcePatternResolver.getResources(dataConfig.getSampleDirectory());
			return Stream.of(resources)
					.map(resource -> FhirDataUtil.readFhirResource(resource, Bundle.class))
					.filter(Objects::nonNull)
					.toList();
		} catch (IOException e) {
			log.error("Error reading data from samples directory (" + dataConfig.getSampleDirectory() + "):", e);
		}
		return List.of();
	}
}
