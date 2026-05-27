package org.octri.fhir_sandbox.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

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

	@Test
	public void testToMapIncludesIdAndClientId() {
		var context = new SmartLaunchContext();
		context.setOpaqueId("opaque-123");
		context.setClientId("client-abc");
		Map<String, Object> map = context.toMap();
		assertEquals("opaque-123", map.get("id"), "toMap includes opaqueId under the 'id' key");
		assertEquals("client-abc", map.get("clientId"), "toMap includes clientId under the 'clientId' key");
	}

	@Test
	public void testToMapIncludesAttributes() {
		var context = new SmartLaunchContext();
		context.setOpaqueId("opaque-123");
		context.setClientId("client-abc");
		context.setPatientAttribute("patient-456");
		context.setEncounterAttribute("encounter-789");
		Map<String, Object> map = context.toMap();
		assertEquals("patient-456", map.get(SmartLaunchContext.PATIENT_ATTRIBUTE),
				"toMap includes patient attribute");
		assertEquals("encounter-789", map.get(SmartLaunchContext.ENCOUNTER_ATTRIBUTE),
				"toMap includes encounter attribute");
	}

	@Test
	public void testToMapWithNoAttributes() {
		var context = new SmartLaunchContext();
		context.setOpaqueId("opaque-123");
		context.setClientId("client-abc");
		Map<String, Object> map = context.toMap();
		assertTrue(map.containsKey("id"), "toMap always contains 'id'");
		assertTrue(map.containsKey("clientId"), "toMap always contains 'clientId'");
		assertEquals(2, map.size(), "toMap contains only 'id' and 'clientId' when attributes is empty");
	}

}
