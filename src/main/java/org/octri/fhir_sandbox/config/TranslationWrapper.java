package org.octri.fhir_sandbox.config;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Responsible for wrapping a translation string in HTML markup so it can be
 * manipulated on the front end via javascript.
 */
public class TranslationWrapper {

    private static final String DEFAULT_WRAP_CLASS = "translation-key";

    private String wrapElement = "span";
    private String wrapClass = DEFAULT_WRAP_CLASS;
    private String manageTranslationRoute = "/admin/translation/manage?key={key}&locale={locale}";

    /**
     * Returns a copy of the content wrapped in some html markup.
     *
     * @param text
     *                - the content string to wrap
     * @param request
     *                - request with context for additional element attributes
     * @param key
     *                - key/code for looking up the content in the message source.
     * @param locale
     *                - locale of the provided content
     * @return
     */
    public String wrapped(String text, HttpServletRequest request, String key, Locale locale) {
        var url = makeUrl(request, key, locale);
        var element = this.wrapElement;
        var format = "<%s class=\"%s\" title=\"%s\" data-url=\"%s\">%s</%s>";
        return format.formatted(element, wrapClass, key, url, text, element);
    }

    /**
     * Construct the URL for managing the Translation content for the given
     * messageKey with the current Locale.
     *
     * @param request
     * @param messageKey
     */
    public String makeUrl(HttpServletRequest request, String messageKey, Locale locale) {
        var route = manageTranslationRoute.replace("{key}", messageKey).replace("{locale}", locale.toLanguageTag());
        return request.getContextPath() + route;
    }

    public String getWrapElement() {
        return wrapElement;
    }

    /**
     * HTML element used for wrapping the text. Ex. 'span', 'div'
     *
     * @param wrapElement
     */
    public void setWrapElement(String wrapElement) {
        this.wrapElement = wrapElement;
    }

    public String getWrapClass() {
        return wrapClass;
    }

    /**
     * This class is given to the surrounding html element. Class may be used as a
     * selector for styling or other
     * front end javascript interactions.
     *
     * @param wrapClass
     */
    public void setWrapClass(String wrapClass) {
        this.wrapClass = wrapClass;
    }

    public String getManageTranslationRoute() {
        return manageTranslationRoute;
    }

    /**
     * This route is written to a data-url attribute in the span element, along with
     * the translation
     * key. This can be used by front end javascript interactions for navigating to
     * a url for
     * editing the content. The route should include placeholders for the key and
     * locale in the
     * format "{key}" and "{locale}"
     *
     * @param route
     */
    public void setManageTranslationRoute(String route) {
        this.manageTranslationRoute = route;
    }
}
