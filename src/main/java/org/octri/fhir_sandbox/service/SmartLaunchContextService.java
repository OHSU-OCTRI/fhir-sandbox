package org.octri.fhir_sandbox.service;

import java.security.SecureRandom;
import java.util.Optional;

import org.octri.fhir_sandbox.domain.SmartLaunchContext;
import org.octri.fhir_sandbox.domain.SmartLaunchContextProperties;
import org.octri.fhir_sandbox.repository.SmartLaunchContextRepository;
import org.springframework.stereotype.Service;

/**
 * Service for working with {@link SmartLaunchContext} entities.
 */
@Service
public class SmartLaunchContextService {

	private static final String ID_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int ID_LENGTH = 6;

	private final SmartLaunchContextRepository repository;

	public SmartLaunchContextService(SmartLaunchContextRepository repository) {
		this.repository = repository;
	}

	/**
	 * Finds a SMART app launch context with the given opaque ID and client ID.
	 *
	 * @param opaqueId
	 * @param clientId
	 * @return
	 */
	public Optional<SmartLaunchContext> findByOpaqueIdAndClientId(String opaqueId, String clientId) {
		return repository.findByOpaqueIdAndClientId(opaqueId, clientId);
	}

	/**
	 * Creates a new SMART app launch context for the given client ID.
	 *
	 * @param properties
	 * @return
	 */
	public SmartLaunchContext createLaunchContext(SmartLaunchContextProperties properties) {
		var context = new SmartLaunchContext();
		context.setOpaqueId(generateOpaqueId());
		context.setClientId(properties.clientId());

		if (properties.patientId() != null) {
			context.setPatientAttribute(properties.patientId());
		}

		if (properties.encounterId() != null) {
			context.setEncounterAttribute(properties.encounterId());
		}

		if (properties.fhirUser() != null) {
			context.setFhirUserAttribute(properties.fhirUser());
		}

		return repository.save(context);
	}

	/**
	 * Generates a random ID consisting of uppercase, lowercase, and number characters for use as a opaque launch ID.
	 *
	 * @return
	 */
	private String generateOpaqueId() {
		var random = new SecureRandom();
		var result = new StringBuilder();

		for (int i = 0; i < ID_LENGTH; i++) {
			result.append(ID_CHARACTERS.charAt(random.nextInt(ID_CHARACTERS.length())));
		}

		return result.toString();
	}

}
