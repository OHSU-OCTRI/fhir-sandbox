package org.octri.fhir_sandbox.domain;

import java.util.UUID;

import org.octri.authentication.server.security.entity.User;
import org.octri.common.domain.AbstractEntity;
import org.octri.common.view.Labelled;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class Sandbox extends AbstractEntity implements Labelled {

	@NotNull
	@ManyToOne
	private User owner;

	@NotNull
	private String description;

	@NotNull
	private String uuid = UUID.randomUUID().toString();

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getLabel() {
		return description;
	}

	@Override
	public String toString() {
		return "Sandbox [id=" + id + ", owner=" + owner + ", description=" + description + ", uuid=" + uuid
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", updatedBy=" + updatedBy + "]";
	}

}
