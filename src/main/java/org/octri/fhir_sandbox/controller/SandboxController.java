package org.octri.fhir_sandbox.controller;

import java.util.Map;

import org.octri.authentication.server.security.repository.UserRepository;
import org.octri.common.controller.AbstractEntityController;
import org.octri.common.view.OptionList;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.repository.SandboxRepository;
import org.octri.fhir_sandbox.service.SandboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Controller for {@link Sandbox} objects.
 */
@Controller
@RequestMapping("/data/sandbox")
public class SandboxController extends AbstractEntityController<Sandbox, SandboxRepository> {

	private static final Logger log = LoggerFactory.getLogger(SandboxController.class);

	@Autowired
	private SandboxRepository repository;

	@Autowired
	private SandboxService sandboxService;

	@Autowired
	private UserRepository userRepository;

	@Override
	public String newEntity(Map<String, Object> model) {
		String template = super.newEntity(model);

		// Add options for select.
		model.put("ownerOptions",
				OptionList.fromSearch(userRepository.findAll(), null));
		return template;
	}

	@Override
	public String create(Map<String, Object> model, @Valid @ModelAttribute("entity") Sandbox entity,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		var savedSandbox = sandboxService.createSandbox(entity);
		model.put("newEntity", savedSandbox);
		redirectAttributes.addFlashAttribute("successMessage", "Sandbox successfully created.");
		return showRedirect(savedSandbox.getId());
	}

	@Override
	public String show(Map<String, Object> model, @PathVariable Long id) {
		String template = super.show(model, id);
		Sandbox entity = (Sandbox) model.get("entity");
		model.put("fhirServerUrl", sandboxService.getSandboxFhirUrl(entity));
		return template;
	}

	@Override
	public String edit(Map<String, Object> model, @PathVariable Long id) {
		String template = super.edit(model, id);
		Sandbox entity = (Sandbox) model.get("entity");

		// Add options for select.
		model.put("ownerOptions",
				OptionList.fromSearch(userRepository.findAll(), entity.getOwner()));

		return template;
	}

	@Override
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		var sandbox = repository.findById(id).get();
		sandboxService.deleteSandbox(sandbox);
		redirectAttributes.addFlashAttribute("infoMessage", "Sandbox successfully deleted.");
		return listingRedirect();
	}

	@Override
	protected Class<Sandbox> domainClass() {
		return Sandbox.class;
	}

	@Override
	protected SandboxRepository getRepository() {
		return this.repository;
	}
}