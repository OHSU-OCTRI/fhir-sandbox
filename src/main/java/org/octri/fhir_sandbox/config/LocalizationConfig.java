package org.octri.fhir_sandbox.config;

import java.util.List;
import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the use of i18n tags in Mustache templates.
 *
 * ex. {{#i18n}}message-key [arg1]...[argN]{{/i18n}}
 */
@Configuration
@ConfigurationProperties(prefix = "octri.i18n")
public class LocalizationConfig {

    private List<Locale> supportedLocales = List.of(Locale.US, Locale.FRENCH);
    private Locale defaultLocale = Locale.US;
    private String templateKey = "i18n";
    private Boolean contentEditingEnabled = false;
    private String contentManagementScript = "translation-management.js";

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public Boolean getContentEditingEnabled() {
        return contentEditingEnabled;
    }

    public void setContentEditingEnabled(Boolean debug) {
        this.contentEditingEnabled = debug;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public List<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(List<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public String getContentManagementScript() {
        return contentManagementScript;
    }

    public void setContentManagementScript(String contentManagementScript) {
        this.contentManagementScript = contentManagementScript;
    }

}