package org.octri.fhir_sandbox.repository;

import java.util.Optional;

import org.octri.fhir_sandbox.domain.SmartLaunchContext;
import org.springframework.data.repository.CrudRepository;

public interface SmartLaunchContextRepository extends CrudRepository<SmartLaunchContext, Long> {

	Optional<SmartLaunchContext> findByOpaqueIdAndClientId(String opaqueId, String clientId);

}
