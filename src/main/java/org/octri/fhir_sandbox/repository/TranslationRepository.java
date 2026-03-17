package org.octri.fhir_sandbox.repository;

import java.util.List;
import java.util.Optional;

import org.octri.fhir_sandbox.domain.Translation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "translation")
public interface TranslationRepository extends CrudRepository<Translation, Long> {

    List<Translation> findAll();

    Optional<Translation> findFirstByMessageKeyAndLocale(String messageKey, String locale);
}