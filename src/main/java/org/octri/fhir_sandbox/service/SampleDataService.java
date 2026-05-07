package org.octri.fhir_sandbox.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Bundle;
import org.octri.fhir_sandbox.config.SandboxDataConfig;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.util.FhirDataUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;

@Service
public class SampleDataService {

	private static final Log log = LogFactory.getLog(SampleDataService.class);

	private final SandboxDataConfig dataConfig;
	private final ResourcePatternResolver resourcePatternResolver;
	private final FhirContext fhirContext;

	public SampleDataService(SandboxDataConfig dataConfig, ResourcePatternResolver resourcePatternResolver,
			FhirContext fhirContext) {
		this.dataConfig = dataConfig;
		this.resourcePatternResolver = resourcePatternResolver;
		this.fhirContext = fhirContext;
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
	 * @param sandbox
	 */
	@Async
	public CompletableFuture<Void> loadSampleData(Sandbox sandbox, String fhirUrl) {
		var future = new CompletableFuture<Void>();
		List<Bundle> sampleData = List.of();
		try {
			sampleData = getAllSampleBundles();
		} catch (IOException e) {
			log.error("Problem encountered reading sample data resources", e);
			future.completeExceptionally(e);
		}
		var fhirClient = fhirContext.newRestfulGenericClient(fhirUrl);
		for (var bundle : sampleData) {
			try {
				Bundle resp = fhirClient
						.transaction()
						.withBundle(bundle)
						.execute();
				// TODO: check outcome and handle failures
			} catch (BaseServerResponseException e) {
				log.error("Error response to transaction with sandbox server " + fhirUrl, e);
				future.completeExceptionally(e);
				break;
			} catch (Error e) {
				log.error("Error encountered during transaction with sandbox server " + fhirUrl, e);
				future.completeExceptionally(e);
				break;
			}
		}
		if (!future.isCompletedExceptionally()) {
			future.complete(null);
		}
		return future;
	}
}
