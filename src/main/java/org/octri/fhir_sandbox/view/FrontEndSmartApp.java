package org.octri.fhir_sandbox.view;

/**
 * Represents a SmartApp object, as expected by Vue component code.
 */
public record FrontEndSmartApp(String id, String clientName, String launchUri, FrontEndSmartAppSandbox sandbox) {

}
