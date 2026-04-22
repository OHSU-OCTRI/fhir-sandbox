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

}
