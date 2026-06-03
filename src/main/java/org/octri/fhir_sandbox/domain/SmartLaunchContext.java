package org.octri.fhir_sandbox.domain;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.octri.common.domain.AbstractEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

/**
 * Represents the SMART application launch context attributes, for example the patient or encounter context to use when
 * opening the application.
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "opaque_id", "client_id" }) })
public class SmartLaunchContext extends AbstractEntity {

	public static final String PATIENT_ATTRIBUTE = "patient";
	public static final String ENCOUNTER_ATTRIBUTE = "encounter";
	public static final String FHIR_USER_ATTRIBUTE = "fhirUser";

	@NotNull
	private String opaqueId;

	@NotNull
	private String clientId;

	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, Object> attributes = new HashMap<>();

	public String getOpaqueId() {
		return opaqueId;
	}

	public void setOpaqueId(String opaqueId) {
		this.opaqueId = opaqueId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public Object getAttribute(String attributeName) {
		return attributes.get(attributeName);
	}

	public void setPatientAttribute(String patientId) {
		attributes.put(PATIENT_ATTRIBUTE, patientId);
	}

	public Object getPatientAttribute() {
		return attributes.get(PATIENT_ATTRIBUTE);
	}

	public void setEncounterAttribute(String encounterId) {
		attributes.put(ENCOUNTER_ATTRIBUTE, encounterId);
	}

	public Object getEncounterAttribute() {
		return attributes.get(ENCOUNTER_ATTRIBUTE);
	}

	public void setFhirUserAttribute(String fhirUser) {
		attributes.put(FHIR_USER_ATTRIBUTE, fhirUser);
	}

	public Object getFhirUserAttribute() {
		return attributes.get(FHIR_USER_ATTRIBUTE);
	}

	public Map<String, Object> toMap() {
		var mergedMap = new HashMap<String, Object>();
		mergedMap.put("id", this.opaqueId);
		mergedMap.put("clientId", this.clientId);
		mergedMap.putAll(this.attributes);
		return mergedMap;
	}

}
