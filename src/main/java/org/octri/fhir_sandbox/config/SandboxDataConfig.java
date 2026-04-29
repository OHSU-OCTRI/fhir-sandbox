package org.octri.fhir_sandbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "octri.sandbox.data")
public class SandboxDataConfig {

	private String samplesLocation;
	private String sampleDiscoveryPattern;

	public String getSamplesLocation() {
		return samplesLocation;
	}

	public void setSamplesLocation(String samplesLocation) {
		this.samplesLocation = samplesLocation;
	}

	public String getSampleDiscoveryPattern() {
		return getSamplesLocation() + sampleDiscoveryPattern;
	}

	public void setSampleDiscoveryPattern(String sampleDiscoveryPattern) {
		this.sampleDiscoveryPattern = sampleDiscoveryPattern;
	}
}
