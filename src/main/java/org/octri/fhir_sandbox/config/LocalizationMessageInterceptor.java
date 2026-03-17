package org.octri.fhir_sandbox.config;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.octri.common.view.ViewUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor for Mustache templates to access the Spring MessageSource for
 * localized messages.
 *
 * examples:
 * {{#i18n}}message-key [arg1]...[argN]{{/i18n}}
 * {{#i18n}}home.greeting [{{user.name}}]{{/i18n}}
 *
 * In the database, arguments are referenced by index inside a curly brace:
 * home.greeting="Hello, {0}".
 *
 * Adapted from:
 * https://github.com/sps/mustache-spring-view/blob/master/src/main/java/org/springframework/web/servlet/i18n/MustacheLocalizationMessageInterceptor.java
 * https://github.com/sps/mustache-spring-view/blob/master/src/main/java/org/springframework/web/servlet/view/mustache/java/LocalizationMessageInterceptor.java
 */
public class LocalizationMessageInterceptor
        implements HandlerInterceptor, MessageSourceAware {

    private static final Pattern KEY_PATTERN = Pattern.compile("(.*?)[\\s\\[]");
    private static final Pattern ARGS_PATTERN = Pattern.compile("\\[(.*?)\\]");

    private String messageKey = "i18n";
    private MessageSource messageSource;
    private LocaleResolver localeResolver;

    // optionally wrap the translated string in html markup
    private TranslationWrapper wrapper;
    private String scriptName = null;

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
            final ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            modelAndView.addObject(this.messageKey, createContext(request));
            if (this.scriptName != null) {
                // Consider putting this concern in TemplateAdvice.
                ViewUtils.addAdminScript(modelAndView.getModel(), this.scriptName);
            }
        }
    }

    /**
     * Creates a Mustache Lambda that executes a fragment, then looks up the
     * resulting string (key) in the i18n messages
     * property file associated with the current locale.
     *
     * see: https://github.com/samskivert/jmustache/?tab=readme-ov-file#lambdas
     *
     * @param request
     * @return
     */
    public Mustache.Lambda createContext(final HttpServletRequest request) {
        Assert.notNull(localeResolver, "localeResolver not initialized");
        Assert.notNull(messageSource, "messageSource not initialized");

        Locale locale = localeResolver.resolveLocale(request);
        var i18n = new Mustache.Lambda() {

            public void execute(Template.Fragment frag, Writer out) throws IOException {
                // execute the code within the {{#i18n}}...{{/i18n}} to get a key (and args).
                String keyAndOptionalArgs = frag.execute();
                var key = extractKey(keyAndOptionalArgs);
                var args = extractParameters(keyAndOptionalArgs);
                String text = messageSource.getMessage(key, args, locale);

                if (wrapper != null) {
                    text = wrapper.wrapped(text, request, key, locale);
                }
                // TODO: consider sanitizing text with
                // https://github.com/OWASP/java-html-sanitizer
                out.write(text);
            }
        };
        return i18n;
    }

    public String getMessageKey() {
        return messageKey;
    }

    /**
     * Define custom key to access i18n messages in Mustache templates.
     *
     * @param messageKey
     *                   - the key used in the template.
     *                   ex. with messageKey "t", the template should be:
     *                   {{#t}}message.key{{/t}}
     *
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    /**
     * Set the class for optionally wrapping the content in html.
     *
     * @param wrapper
     */
    public void setWrapper(TranslationWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public String getScriptName() {
        return this.scriptName;
    }

    /**
     * Optionally include a javascript file with the given name in the models'
     * pageScripts attribute.
     *
     * @param name
     */
    public void setScriptName(String name) {
        this.scriptName = name;
    }

    /**
     * Split key from (optional) arguments.
     *
     * @param keyAndOptionalArgs
     * @return localization key
     */
    private String extractKey(String keyAndOptionalArgs) {
        Matcher matcher = KEY_PATTERN.matcher(keyAndOptionalArgs);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return keyAndOptionalArgs;
    }

    /**
     * Split args from input string.
     *
     * @param keyAndOptionalArgs
     * @return Array of extracted parameters
     */
    private Object[] extractParameters(String keyAndOptionalArgs) {
        Matcher matcher = ARGS_PATTERN.matcher(keyAndOptionalArgs);
        List<String> args = new ArrayList<String>();
        while (matcher.find()) {
            args.add(matcher.group(1));
        }
        return args.toArray();
    }

}