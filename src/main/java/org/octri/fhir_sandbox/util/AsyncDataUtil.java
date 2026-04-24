package org.octri.fhir_sandbox.util;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

public class AsyncDataUtil {

	private static final Long GET_TIMEOUT_IN_SECOND = 300l;
	private static final Log log = LogFactory.getLog(AsyncDataUtil.class);

	/**
	 * Attempts to resolve a CompletableFuture to its completed value,
	 * defaulting to null if an exception occurs
	 * 
	 * - Specifies a timeout parameter
	 * - Does not check for completion - ensure future.isDone before calling
	 * 
	 * @param <T>
	 * @param future
	 * @return
	 */
	public static <T> T tryGetFutureOrNull(CompletableFuture<T> future) {
		T value = null;
		try {
			value = future.get(GET_TIMEOUT_IN_SECOND, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			log.error("Timeout occurred after waiting " + GET_TIMEOUT_IN_SECOND + " seconds for data", e);
		} catch (InterruptedException e) {
			log.error("Attempt to load resource was interrupted", e);
		} catch (ExecutionException e) {
		}	// Error should be logged in readFhirResource
		return value;
	}

	/**
	 * Uses the parser and specified cls to read the target file
	 * 
	 * @param <T>
	 * @param file
	 * @param cls
	 * @param parser
	 * @return
	 */
	@Async
	public static <T extends IBaseResource> CompletableFuture<T> readFhirResource(Resource resource, Class<T> cls,
			IParser parser) {
		CompletableFuture<T> data = new CompletableFuture<>();
		try {
			var bundle = parser.parseResource(cls, resource.getInputStream());
			data.complete(bundle);
		} catch (IOException e) {
			log.error("Failed to read FHIR resource " + resource.getFilename(), e);
			data.completeExceptionally(e);
		} catch (DataFormatException e) {
			log.error("Invalid format in FHIR resource " + resource.getFilename(), e);
			data.completeExceptionally(e);
		}
		return data;
	}
}
