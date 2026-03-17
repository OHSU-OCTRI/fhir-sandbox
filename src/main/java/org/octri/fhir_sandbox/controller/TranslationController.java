package org.octri.fhir_sandbox.controller;

import java.util.Map;

import org.octri.fhir_sandbox.config.LocalizationConfig;
import org.octri.fhir_sandbox.domain.Translation;
import org.octri.fhir_sandbox.repository.TranslationRepository;
import org.octri.fhir_sandbox.service.TranslationService;
import org.octri.common.controller.AbstractEntityController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Controller for {@link Translation} objects.
 */
@Controller
@RequestMapping("/admin/translation")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER')")
public class TranslationController extends AbstractEntityController<Translation, TranslationRepository> {

    @Autowired
    private TranslationRepository repository;

    @Autowired
    private TranslationService service;

    @Autowired
    private LocalizationConfig config;

    record LocaleOption(String label, String value) {

    }

    @Override
    public String list(Map<String, Object> model) {
        var template = super.list(model);
        model.put("contentEditingEnabled", config.getContentEditingEnabled());
        return template;
    }

    @Override
    public String show(Map<String, Object> model, @PathVariable Long id) {
        var template = super.show(model, id);
        model.put("contentEditingEnabled", config.getContentEditingEnabled());
        return template;
    }

    /**
     * Redirect to the edit screen for a translation string given its key and
     * locale.
     *
     * @param request
     * @param key
     * @param locale
     * @return
     */
    @GetMapping("/manage")
    public String editCurrentTranslationWithKey(HttpServletRequest request,
            @RequestParam String key, @RequestParam String locale) {
        var translation = service.findFirstByMessageKeyAndLocale(key, locale);
        return "redirect:" + this.getBaseRoute() + "/" + translation.get().getId() + "/edit";
    }

    @GetMapping("/new")
    @Override
    public String newEntity(Map<String, Object> model) {
        var localeOpts = config.getSupportedLocales().stream()
                .map(locale -> new LocaleOption(locale.getDisplayName(), locale.toLanguageTag())).toList();

        model.put("supportedLocales", localeOpts);
        return super.newEntity(model);
    }

    public String edit(Map<String, Object> model, @PathVariable Long id) {
        if (!config.getContentEditingEnabled()) {
            return super.listingRedirect();
        }
        model.put("pageScripts", new String[] { "vendor.js", "managed-content.js" });
        model.put("pageStyles", new String[] { "managed-content.css" });
        model.put("hideEntityId", true);
        super.edit(model, id);
        return template("edit");
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER')")
    public String update(Map<String, Object> model, @PathVariable Long id,
            @Valid @ModelAttribute("entity") Translation entity, BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (config.getContentEditingEnabled()) {
            this.service.save(entity);
            redirectAttributes.addFlashAttribute("infoMessage", this.entityName() + " updated.");
        }
        return super.listingRedirect();
    }

    @PreAuthorize("hasRole('ROLE_SUPER')")
    @Override
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return super.delete(id, redirectAttributes);
    }

    @Override
    protected Class<Translation> domainClass() {
        return Translation.class;
    }

    @Override
    protected TranslationRepository getRepository() {
        return this.repository;
    }
}