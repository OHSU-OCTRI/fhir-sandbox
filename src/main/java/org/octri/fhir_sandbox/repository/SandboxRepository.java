package org.octri.fhir_sandbox.repository;

import org.octri.fhir_sandbox.domain.Sandbox;
import org.springframework.data.repository.CrudRepository;

public interface SandboxRepository extends CrudRepository<Sandbox, Long> {
}