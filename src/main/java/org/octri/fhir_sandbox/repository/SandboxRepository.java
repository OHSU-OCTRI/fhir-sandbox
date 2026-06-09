package org.octri.fhir_sandbox.repository;

import java.util.List;

import org.octri.authentication.server.security.entity.User;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.domain.SandboxStatus;
import org.springframework.data.repository.CrudRepository;

public interface SandboxRepository extends CrudRepository<Sandbox, Long> {

	List<Sandbox> findByOwner(User owner);

	List<Sandbox> findByStatus(SandboxStatus status);

	List<Sandbox> findByAuthorizedUsersId(Long id);
}