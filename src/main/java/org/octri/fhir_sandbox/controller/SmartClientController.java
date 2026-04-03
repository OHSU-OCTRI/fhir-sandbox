package org.octri.fhir_sandbox.controller;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import org.octri.common.controller.AbstractEntityController;
import org.octri.common.view.OptionList;
import org.octri.fhir_sandbox.domain.ClientType;
import org.octri.fhir_sandbox.domain.SmartClient;
import org.octri.fhir_sandbox.repository.SandboxRepository;
import org.octri.fhir_sandbox.repository.SmartClientRepository;
import org.octri.fhir_sandbox.service.SandboxService;
import org.octri.fhir_sandbox.service.SmartClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Admin controller for {@link SmartClient} objects.
 */
@Controller
@RequestMapping("/admin/smart_client")
public class SmartClientController extends AbstractEntityController<SmartClient, SmartClientRepository> {

	@Autowired
	private SmartClientRepository repository;

	@Autowired
	private SandboxRepository sandboxRepository;

	@Autowired
	private SmartClientService service;

	@Autowired
	private SandboxService sandboxService;

	@Override
	public String newEntity(Map<String, Object> model) {
		String template = super.newEntity(model);

		var entity = (SmartClient) model.get("entity");
		entity.setClientId(UUID.randomUUID().toString());
		entity.setClientType(ClientType.PUBLIC);

		// Add options for select.
		model.put("sandboxOptions",
				OptionList.fromSearch(sandboxService.findAll(), null));
		model.put("clientTypeOptions",
				OptionList.fromEnum(EnumSet.allOf(ClientType.class), entity.getClientType()));
		return template;
	}

	@Override
	public String create(Map<String, Object> model, @Valid @ModelAttribute("entity") SmartClient entity,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		var newEntity = service.save(entity);
		model.put("newEntity", newEntity);
		redirectAttributes.addFlashAttribute("successMessage", this.entityName() + " successfully created.");
		return showRedirect(newEntity.getId());
	}

	@Override
	public String edit(Map<String, Object> model, @PathVariable Long id) {
		String template = super.edit(model, id);

		SmartClient entity = (SmartClient) model.get("entity");

		// Add options for select.
		model.put("sandboxOptions",
				OptionList.fromSearch(sandboxService.findAll(), entity.getSandbox()));
		model.put("clientTypeOptions",
				OptionList.fromEnum(EnumSet.allOf(ClientType.class), entity.getClientType()));

		return template;
	}

	@Override
	public String update(Map<String, Object> model, @PathVariable Long id,
			@Valid @ModelAttribute("entity") SmartClient entity, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		service.save(entity);
		redirectAttributes.addFlashAttribute("infoMessage", this.entityName() + " updated.");
		return showRedirect(id);
	}

	@Override
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			service.deleteById(id);
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
	protected Class<SmartClient> domainClass() {
		return SmartClient.class;
	}

	@Override
	protected SmartClientRepository getRepository() {
		return this.repository;
	}
}
