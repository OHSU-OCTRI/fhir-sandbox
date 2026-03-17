package org.octri.fhir_sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({ "org.octri.fhir_sandbox"  , "org.octri.authentication" })
@EntityScan(basePackages = { "org.octri.fhir_sandbox" , "org.octri.authentication"  })
@EnableJpaRepositories(basePackages = { "org.octri.fhir_sandbox" , "org.octri.authentication" })
@EnableJpaAuditing
public class WebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}
}
