package org.octri.fhir_sandbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the banner that highlights non-production
 * environments.
 */
@Configuration
@ConfigurationProperties(prefix = "environment-banner")
public class EnvironmentBannerConfig {

    /**
     * Whether to display the environment banner. Defaults to false.
     */
    private boolean enabled = false;

    /**
     * The text to display in the environment banner.
     */
    private String text = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}