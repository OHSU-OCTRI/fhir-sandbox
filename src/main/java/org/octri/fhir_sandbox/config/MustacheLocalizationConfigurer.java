package org.octri.fhir_sandbox.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * Configures the use of embedded i18n translations in Mustache templates.
 *
 * ex. {{#i18n}}message-key [arg1]...[argN]{{/i18n}}
 */
@Configuration
public class MustacheLocalizationConfigurer implements WebMvcConfigurer {

    LocalizationConfig config;
    MessageSource messageSource;

    public MustacheLocalizationConfigurer(LocalizationConfig config, MessageSource messageSource) {
        this.config = config;
        this.messageSource = messageSource;
    }

    /**
     * Register Interceptors to apply to incoming requests.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Look for the 'lang' parameter and use its value to change the Locale.
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);
        registry.addInterceptor(i18nMessageInterceptor());
    }

    @Bean
    public LocaleResolver localeResolver() {
        // Could also consider using the CookieLocaleResolver here.
        var sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(config.getDefaultLocale());
        return sessionLocaleResolver;
    }

    @Bean
    public LocalizationMessageInterceptor i18nMessageInterceptor() {
        LocalizationMessageInterceptor interceptor = new LocalizationMessageInterceptor();
        interceptor.setLocaleResolver(localeResolver());
        interceptor.setMessageSource(this.getMessageSource());
        interceptor.setMessageKey(config.getTemplateKey());
        if (config.getContentEditingEnabled()) {
            interceptor.setWrapper(new TranslationWrapper());
            interceptor.setScriptName(config.getContentManagementScript());
        }
        return interceptor;
    }

    public LocalizationConfig getConfig() {
        return config;
    }

    public void setConfig(LocalizationConfig config) {
        this.config = config;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

}