package org.octri.fhir_sandbox.controller;

import java.util.Map;

import org.octri.authentication.server.security.repository.UserRepository;
import org.octri.common.controller.AbstractEntityController;
import org.octri.common.view.OptionList;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.domain.SandboxStatus;
import org.octri.fhir_sandbox.repository.SandboxRepository;
import org.octri.fhir_sandbox.service.SandboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Admin controller for {@link Sandbox} objects.
 */
@Controller
@RequestMapping("/admin/sandbox")
public class SandboxController extends AbstractEntityController<Sandbox, SandboxRepository> {

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

	@PostMapping(value = "/new", params = "importSampleData")
	public String create(Map<String, Object> model,
			@RequestParam(value = "importSampleData", required = false) Boolean importSampleData,
			@Valid @ModelAttribute("entity") Sandbox entity,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		var newEntity = sandboxService.createSandbox(entity);
		sandboxService.initializeSandbox(newEntity, importSampleData);
		redirectAttributes.addFlashAttribute("successMessage", this.entityName() + " successfully created.");
		return showRedirect(newEntity.getId());
	}

	@Override
	public String show(Map<String, Object> model, @PathVariable Long id) {
		String template = super.show(model, id);

		Sandbox entity = (Sandbox) model.get("entity");

		model.put("preventDelete", entity.getStatus().equals(SandboxStatus.INITIALIZING));
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
	public String update(Map<String, Object> model, @PathVariable Long id, @ModelAttribute("entity") Sandbox entity,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		sandboxService.save(entity);
		redirectAttributes.addFlashAttribute("infoMessage", this.entityName() + " updated.");
		return showRedirect(id);
	}

	@Override
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			sandboxService.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			String msg = this.entityName() + " is in use and cannot be deleted.";
			redirectAttributes.addFlashAttribute("errorMessage", msg);
			return showRedirect(id);
		}

		String msg = this.entityName() + " with id " + id + " successfully deleted.";
		redirectAttributes.addFlashAttribute("infoMessage", msg);
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