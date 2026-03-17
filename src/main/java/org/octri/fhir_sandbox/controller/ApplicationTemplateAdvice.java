package org.octri.fhir_sandbox.controller;

import org.octri.authentication.server.controller.TemplateAdvice;
import org.octri.authentication.server.security.SecurityHelper;
import org.octri.authentication.server.security.SecurityHelper.Role;
import org.octri.fhir_sandbox.config.EnvironmentBannerConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Additional advice beyond what is offered in the authentication library
 */
@Component("application_template_advice")
@ControllerAdvice
public class ApplicationTemplateAdvice {

	@Autowired
	TemplateAdvice templateAdvice;

	@Autowired
	EnvironmentBannerConfig bannerConfig;

	@ModelAttribute
	public void addDefaultAttributes(HttpServletRequest request, Model model) {
		templateAdvice.addDefaultAttributes(request, model);
		SecurityHelper securityHelper = new SecurityHelper(SecurityContextHolder.getContext());
		model.addAttribute("isAdmin", securityHelper.hasRole(Role.ROLE_ADMIN));
		model.addAttribute("isSuper", securityHelper.hasRole(Role.ROLE_SUPER));
		model.addAttribute("enableEnvironmentBanner", bannerConfig.isEnabled());
		model.addAttribute("environmentBannerText", bannerConfig.getText());
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
}
