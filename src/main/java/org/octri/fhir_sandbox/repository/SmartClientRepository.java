package org.octri.fhir_sandbox.repository;

import java.util.List;
import java.util.Optional;

import org.octri.fhir_sandbox.domain.SmartClient;
import org.springframework.data.repository.CrudRepository;

public interface SmartClientRepository extends CrudRepository<SmartClient, Long> {

	List<SmartClient> findBySandboxId(Long sandboxId);

	Optional<SmartClient> findByClientId(String clientId);

}
