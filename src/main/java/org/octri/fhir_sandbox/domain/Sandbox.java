package org.octri.fhir_sandbox.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.octri.authentication.server.security.entity.User;
import org.octri.common.domain.AbstractEntity;
import org.octri.common.view.Labelled;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Sandbox extends AbstractEntity implements Labelled {

	@NotNull
	@ManyToOne
	private User owner;

	@NotNull
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "sandbox_sharing", joinColumns = {
			@JoinColumn(name = "sandbox") }, inverseJoinColumns = { @JoinColumn(name = "user") })
	private Set<User> authorizedUsers = new HashSet<>();

	@NotNull
	@Size(max = 200)
	private String description;

	@NotNull
	@Enumerated(value = EnumType.STRING)
	private SandboxStatus status;

	@NotNull
	@Column(unique = true)
	private Long serverPartitionId;

	@NotNull
	@Column(unique = true)
	private String serverPartitionName = UUID.randomUUID().toString();

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Set<User> getAuthorizedUsers() {
		return authorizedUsers;
	}

	public void setAuthorizedUsers(Set<User> authorizedUsers) {
		this.authorizedUsers = authorizedUsers;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SandboxStatus getStatus() {
		return status;
	}

	public void setStatus(SandboxStatus status) {
		this.status = status;
	}

	public Long getServerPartitionId() {
		return serverPartitionId;
	}

	public void setServerPartitionId(Long partitionId) {
		this.serverPartitionId = partitionId;
	}

	public String getServerPartitionName() {
		return serverPartitionName;
	}

	public void setServerPartitionName(String uuid) {
		this.serverPartitionName = uuid;
	}

	@Override
	public String getLabel() {
		return description;
	}

	@Override
	public String toString() {
		return "Sandbox [id=" + id + ", owner=" + owner + ", description=" + description + ", serverPartitionId="
				+ serverPartitionId + ", serverPartitionName=" + serverPartitionName + "]";
	}

}
