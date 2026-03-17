package org.octri.fhir_sandbox.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

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

	/**
	 * Provide a FHIR context bean for R4. This can be used to construct API clients that need to interact with the FHIR
	 * server. This object is costly to construct, so we create it here for reuse across the application.
	 *
	 * @return
	 * @see <a href="https://hapifhir.io/hapi-fhir/docs/client/generic_client.html">HAPI FHIR generic client
	 *      documentation</a>
	 */
	@Bean
	public FhirContext fhirContext() {
		return FhirContext.forR4();
	}

	private void validateProperties() {
		log.info("Validating FHIR server configuration...");
		log.debug(properties.toString());

		// TODO: Test connection to FHIR server?
		if (StringUtils.isBlank(properties.getBaseUrl())) {
			log.error("FHIR base URL is not configured");
			throw new IllegalStateException("FHIR base URL is required");
		}
	}

}
