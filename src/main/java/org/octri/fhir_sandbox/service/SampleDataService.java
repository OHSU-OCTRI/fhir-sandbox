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
import org.springframework.core.io.Resource;
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
	 * Uses configuration to discover sample data, returns them as an array of
	 * Spring Resource objects.
	 * 
	 * Returns an empty array if an exception occurs.
	 * 
	 * @return
	 */
	private Resource[] getSampleResources() {
		try {
			return resourcePatternResolver.getResources(dataConfig.getSampleDiscoveryPattern());
		} catch (IOException e) {
			log.error("Error locating sample data with pattern " + dataConfig.getSampleDiscoveryPattern(), e);
		}
		return new Resource[0];
	}

	/**
	 * Obtains sample data references, transforms them from Spring Resources to 
	 * FHIR Bundle objects, then filters instances that failed to process before
	 * returning the data
	 * 
	 * @return
	 */
	public List<Bundle> getAllSampleBundles() {
		return Stream.of(getSampleResources())
				.map(resource -> FhirDataUtil.readFhirResource(resource, Bundle.class))
				.filter(Objects::nonNull)
				.toList();
	}
}
