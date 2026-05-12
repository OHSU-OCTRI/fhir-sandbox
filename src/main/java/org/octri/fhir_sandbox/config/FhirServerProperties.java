package org.octri.fhir_sandbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;

@ConfigurationProperties(prefix = "octri.sandbox.fhir")
public class FhirServerProperties {

	private String baseUrl;
	private String defaultPartition = "DEFAULT";
	private Integer socketTimeout = IRestfulClientFactory.DEFAULT_SOCKET_TIMEOUT;

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

	public Integer getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	@Override
	public String toString() {
		return "FhirServerProperties [baseUrl=" + baseUrl + ", defaultPartition=" + defaultPartition + "]";
	}

}