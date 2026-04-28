package org.octri.fhir_sandbox.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.Test;

public class SmartLaunchContextTest {

	@Test
	public void testGetAttribute() {
		var context = new SmartLaunchContext();
		var attrs = context.getAttributes();
		attrs.put("example", "something");
		assertEquals(attrs.get("example"), context.getAttribute("example"),
				"getAttribute returns the value of the named entry in the attribute map");
		assertNull(context.getAttribute("nope"),
				"getAttribute returns null if the named entry is not present in the attribute map");
	}

	@Test
	public void testSetPatientAttribute() {
		var context = new SmartLaunchContext();
		var attrs = context.getAttributes();
		var expectedPatientId = "somePatient";
		assertNull(attrs.get(SmartLaunchContext.PATIENT_ATTRIBUTE), "the patient attribute should initially be null");
		context.setPatientAttribute(expectedPatientId);
		assertEquals(expectedPatientId, attrs.get(SmartLaunchContext.PATIENT_ATTRIBUTE),
				"setPatientAttribute sets the expected entry in the attribute map");
	}

	@Test
	public void testSetEncounterAttribute() {
		var context = new SmartLaunchContext();
		var attrs = context.getAttributes();
		var expectedEncounterId = "someEncounter";
		assertNull(attrs.get(SmartLaunchContext.ENCOUNTER_ATTRIBUTE),
				"the encounter attribute should initially be null");
		context.setEncounterAttribute(expectedEncounterId);
		assertEquals(expectedEncounterId, attrs.get(SmartLaunchContext.ENCOUNTER_ATTRIBUTE),
				"setEncounterAttribute sets the expected entry in the attribute map");
	}

	@Test
	public void testSetFhirUserAttribute() {
		var context = new SmartLaunchContext();
		var attrs = context.getAttributes();
		var expectedFhirUser = "Practitioner/123";
		assertNull(attrs.get(SmartLaunchContext.FHIR_USER_ATTRIBUTE),
				"the fhirUser attribute should initially be null");
		context.setFhirUserAttribute(expectedFhirUser);
		assertEquals(expectedFhirUser, attrs.get(SmartLaunchContext.FHIR_USER_ATTRIBUTE),
				"setFhirUserAttribute sets the expected entry in the attribute map");
	}

}
