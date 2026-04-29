package org.octri.fhir_sandbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "octri.sandbox.data")
public class SandboxDataConfig {

	// Configure root directory to pull all FHIR JSON samples from
	private String samplesLocation;

	public String getSamplesLocation() {
		return samplesLocation;
	}

	public void setSamplesLocation(String samplesLocation) {
		this.samplesLocation = samplesLocation;
	}

	public String getSampleResourcePattern() {
		return samplesLocation + "**.json";
	}

}
