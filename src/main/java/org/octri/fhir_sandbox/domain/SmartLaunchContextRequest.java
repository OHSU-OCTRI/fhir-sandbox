package org.octri.fhir_sandbox.domain;

/**
 * Represents the payload of a request to create a {@link SmartLaunchContext}.
 */
public record SmartLaunchContextRequest(String clientId, String patientId, String encounterId) {

}
