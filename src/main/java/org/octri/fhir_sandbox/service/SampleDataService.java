package org.octri.fhir_sandbox.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.octri.fhir_sandbox.config.FhirServerProperties;
import org.octri.fhir_sandbox.config.SandboxDataConfig;
import org.octri.fhir_sandbox.util.FhirDataUtil;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Service for processing sample data and saving them to a remote FHIR server
 */
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
	 * Uses the provided pattern to resolve {@link Resource} objects, then parses them using the {@link Bundle} model.
	 * 
	 * @param resourcePattern
	 * @return
	 * @throws UncheckedIOException
	 * @throws DataFormatException
	 */
	private Bundle[] getSampleBundles(String resourcePattern) {
		try {
			return Arrays.stream(resourcePatternResolver.getResources(resourcePattern))
					.map(resource -> FhirDataUtil.readFhirJson(resource, Bundle.class))
					.toArray(Bundle[]::new);
		} catch (IOException e) {
			log.error("Error locating sample data using pattern " + resourcePattern, e);
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Executes the client transaction to save the {@link Bundle} to the remote server.
	 * 
	 * @param bundle
	 * @param client
	 */
	private void handleTransaction(Bundle bundle, IGenericClient client) {
		Bundle resp = client
				.transaction()
				.withBundle(bundle)
				.execute();
		// TODO: check outcome; throw exception if data is not saved
	}

	/**
	 * Reads configured sample data then posts them to the FHIR server.
	 * 
	 * Intentially does not handle errors from helper methods.
	 * 
	 * @param fhirUrl
	 * @throws UncheckedIOException
	 * @throws DataFormatException
	 */
	public void loadSampleData(String fhirUrl) {
		var fhirClient = fhirContext.newRestfulGenericClient(fhirUrl);
		for (var pattern : dataConfig.getSampleResourcePatterns()) {
			for (var bundle : getSampleBundles(pattern)) {
				handleTransaction(bundle, fhirClient);
			}
		}
	}
}
