package org.octri.fhir_sandbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "octri.sandbox.fhir")
public class FhirServerProperties {

	private String baseUrl;
	private String defaultPartition = "DEFAULT";

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getDefaultPartition() {
		return defaultPartition;
	}

	public void setDefaultPartition(String defaultPartition) {
		this.defaultPartition = defaultPartition;
	}

	@Override
	public String toString() {
		return "FhirServerProperties [baseUrl=" + baseUrl + ", defaultPartition=" + defaultPartition + "]";
	}

}