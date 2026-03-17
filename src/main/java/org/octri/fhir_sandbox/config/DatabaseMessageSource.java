package org.octri.fhir_sandbox.config;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

import org.octri.fhir_sandbox.domain.Translation;
import org.octri.fhir_sandbox.service.TranslationService;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * MessageSource that gets translation content from the database.
 */
@Component("messageSource")
public class DatabaseMessageSource extends AbstractMessageSource {

    private TranslationService translationService;
    private Locale defaultLocale;

    public DatabaseMessageSource(TranslationService service, LocalizationConfig config) {
        this.translationService = service;
        this.defaultLocale = config.getDefaultLocale();
        this.setUseCodeAsDefaultMessage(true);
    }

    /**
     * Resolves the code by looking up the key in the database along with the
     * provided Locale. If not found it falls
     * back on the configured defaultLocale. If the key is not found using either
     * locale the result will be null.
     *
     * Caching is implemented in the TranslationService.
     */
    @Override
    @Nullable
    protected MessageFormat resolveCode(String code, Locale locale) {
        Optional<Translation> translation = translationService.findFirstByMessageKeyAndLocale(code,
                locale.toLanguageTag());

        if (translation.isEmpty()) {
            locale = getDefaultLocale();
            translation = translationService.findFirstByMessageKeyAndLocale(code,
                    locale.toLanguageTag());
        }

        if (translation.isEmpty()) {
            return null;
        }
        var content = translation.get().getContent();
        // Prevent escaping single quotes; see
        // https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html
        var contentWithQuotes = content.replaceAll("'", "''");

        return new MessageFormat(contentWithQuotes, locale);
    }

    protected Locale getDefaultLocale() {
        if (this.defaultLocale != null) {
            return this.defaultLocale;
        }
        return Locale.getDefault();
    }

}
