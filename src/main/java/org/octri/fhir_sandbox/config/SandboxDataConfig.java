package org.octri.fhir_sandbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "octri.sandbox.data")
public class SandboxDataConfig {

	private String samplesDirectory;
	
	public String getSampleDirectory() {
		return samplesDirectory;
	}

	public void setSampleDirectory(String samplesDirectory) {
		this.samplesDirectory = samplesDirectory;
	}
}
