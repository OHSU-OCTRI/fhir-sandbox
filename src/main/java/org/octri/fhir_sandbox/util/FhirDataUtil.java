package org.octri.fhir_sandbox.util;

import java.io.IOException;

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
	 * Uses the parser and specified cls to read the target file
	 * 
	 * @param <T>
	 * @param file
	 * @param cls
	 * @param parser
	 * @return
	 */
	public static <T extends IBaseResource> T readFhirResource(Resource resource, Class<T> cls) {
		try {
			return parser.parseResource(cls, resource.getInputStream());
		} catch (IOException e) {
			log.error("Failed to read FHIR resource " + resource.getFilename(), e);
		} catch (DataFormatException e) {
			log.error("Invalid format in FHIR resource " + resource.getFilename(), e);
		}
		return null;
	}
}
