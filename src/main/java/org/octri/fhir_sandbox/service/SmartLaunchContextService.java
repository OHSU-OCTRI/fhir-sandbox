package org.octri.fhir_sandbox.service;

import java.security.SecureRandom;

import org.octri.fhir_sandbox.domain.SmartLaunchContext;
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
	 * Creates a new SMART app launch context for the given client ID.
	 *
	 * @param clientId
	 * @param patientId
	 * @param encounterId
	 * @return
	 */
	public SmartLaunchContext createLaunchContext(String clientId, String patientId, String encounterId) {
		var context = new SmartLaunchContext();
		context.setOpaqueId(generateOpaqueId());
		context.setClientId(clientId);

		var contextAttrs = context.getAttributes();
		if (patientId != null) {
			contextAttrs.put("patient", patientId);
		}

		if (encounterId != null) {
			contextAttrs.put("encounter", encounterId);
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
