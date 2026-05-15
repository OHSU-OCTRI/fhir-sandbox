package org.octri.fhir_sandbox.config;

import java.nio.file.Path;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "octri.sandbox.data")
public class SandboxDataConfig {

	private List<String> sampleDirectories;

	public List<String> getSampleDirectories() {
		return sampleDirectories;
	}

	public void setSampleDirectories(List<String> sampleDirectories) {
		this.sampleDirectories = sampleDirectories;
	}

	public List<String> getSampleResourcePatterns() {
		return sampleDirectories.stream()
				.map(dir -> Path.of(dir, "**.json").toString())
				.toList();
	}

}
