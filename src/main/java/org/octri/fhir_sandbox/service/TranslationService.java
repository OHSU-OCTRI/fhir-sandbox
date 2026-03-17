package org.octri.fhir_sandbox.service;

import java.util.Optional;

import org.octri.fhir_sandbox.domain.Translation;
import org.octri.fhir_sandbox.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    @Autowired
    private TranslationRepository repository;

    @Cacheable(value = "translations", key = "#messageKey + '_' + #locale")
    public Optional<Translation> findFirstByMessageKeyAndLocale(String messageKey, String locale) {
        return repository.findFirstByMessageKeyAndLocale(messageKey, locale);
    }

    @CachePut(value = "translations", key = "#result.messageKey + '_' + #result.locale")
    public Translation save(Translation entity) {
        return repository.save(entity);
    }
}
