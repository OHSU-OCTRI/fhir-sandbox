package org.octri.fhir_sandbox.controller;

import java.util.Map;

import org.octri.authentication.server.security.SecurityHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Home page controller.
 */
@RestController
public class HomeController {

	@GetMapping("/")
	public ModelAndView welcome(Map<String, Object> model) {

		SecurityHelper securityHelper = new SecurityHelper(SecurityContextHolder.getContext());
		if (securityHelper.isLoggedIn()) {
			model.put("page_title", "OCTRI FHIR Sandbox");
			return new ModelAndView("home", model);
		}

		return new ModelAndView("login", model);

	}

}
