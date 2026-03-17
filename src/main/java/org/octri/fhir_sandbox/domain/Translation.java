package org.octri.fhir_sandbox.domain;

import org.octri.common.domain.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

/**
 * Represents the translation of a word, phrase, or section.
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "message_key", "locale" }) })
public class Translation extends AbstractEntity {

    private String locale;

    @NotNull
    @Column(unique = true)
    private String messageKey;

    @Column(columnDefinition = "TEXT DEFAULT NULL")
    private String content;

    @Column(columnDefinition = "TEXT DEFAULT NULL")
    private String description;

    private Boolean markupAllowed;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getMarkupAllowed() {
        return markupAllowed;
    }

    public void setMarkupAllowed(Boolean markupAllowed) {
        this.markupAllowed = markupAllowed;
    }

    public String getKey() {
        return getMessageKey();
    }

    public void setKey(String key) {
        setMessageKey(key);
    }

    @Override
    public String toString() {
        return "Translation [locale=" + locale + ", key=" + messageKey + ", content=" + content + ", description="
        + description + ", markupAllowed=" + markupAllowed + "]";
    }

}