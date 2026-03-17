package org.octri.fhir_sandbox.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * FHIR server configuration.
 */
@Configuration
@EnableConfigurationProperties(FhirServerProperties.class)
public class FhirServerConfig {

	private static final Logger log = LoggerFactory.getLogger(FhirServerConfig.class);

	private final FhirServerProperties properties;

	public FhirServerConfig(FhirServerProperties properties) {
		this.properties = properties;
		validateProperties();
	}

	private void validateProperties() {
		log.info("Validating FHIR server configuration...");
		log.debug(this.properties.toString());

		// TODO: Test connection to FHIR server?
		if (properties.getBaseUrl() == null) {
			throw new IllegalStateException("FHIR base URL is required");
		}
	}

}
