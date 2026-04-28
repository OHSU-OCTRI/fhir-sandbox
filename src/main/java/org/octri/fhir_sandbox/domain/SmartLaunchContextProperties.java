package org.octri.fhir_sandbox.domain;

/**
 * Represents the payload of a request to create a {@link SmartLaunchContext}.
 */
public record SmartLaunchContextProperties(String clientId, String patientId, String encounterId, String fhirUser) {

}
