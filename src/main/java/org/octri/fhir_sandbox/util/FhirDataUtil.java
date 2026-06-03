package org.octri.fhir_sandbox.util;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.core.io.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

public class FhirDataUtil {

	private static final Log log = LogFactory.getLog(FhirDataUtil.class);
	private static final IParser parser = FhirContext.forR4().newJsonParser();

	/**
	 * Parses the provided {@link Resource} as the specified {@link IBaseResource} class.
	 * 
	 * @param <T>
	 * @param resource
	 * @param cls
	 * @return
	 * @throws UncheckedIOException
	 * @throws DataFormatException
	 */
	public static <T extends IBaseResource> T readFhirJson(Resource resource, Class<T> cls) {
		try {
			return parser.parseResource(cls, resource.getInputStream());
		} catch (IOException e) {
			log.error("Failed to read FHIR resource " + resource.getFilename(), e);
			throw new UncheckedIOException(e);
		} catch (DataFormatException e) {
			log.error("Invalid format in FHIR resource " + resource.getFilename(), e);
			throw e;
		}
	}
}
