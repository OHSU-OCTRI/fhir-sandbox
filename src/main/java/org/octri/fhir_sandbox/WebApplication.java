package org.octri.fhir_sandbox;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@ComponentScan({ "org.octri.fhir_sandbox", "org.octri.authentication" })
@EntityScan(basePackages = { "org.octri.fhir_sandbox", "org.octri.authentication" })
@EnableJpaRepositories(basePackages = { "org.octri.fhir_sandbox", "org.octri.authentication" })
@EnableJpaAuditing
@EnableAsync
public class WebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

	/**
	 * Provides a default {@link ThreadPoolTaskExecutor} for background tasks.
	 * TODO: configurable pool parameters
	 *
	 * @return
	 */
	@Bean
	public Executor taskExecutor() {
		var executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("default-async-");
		executor.initialize();
		return executor;
	}

}
