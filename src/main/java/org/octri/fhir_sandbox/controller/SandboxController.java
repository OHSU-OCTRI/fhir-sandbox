package org.octri.fhir_sandbox.controller;

import java.util.Map;

import org.octri.authentication.server.security.repository.UserRepository;
import org.octri.common.controller.AbstractEntityController;
import org.octri.common.view.OptionList;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.repository.SandboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for {@link Sandbox} objects.
 */
@Controller
@RequestMapping("/data/sandbox")
public class SandboxController extends AbstractEntityController<Sandbox, SandboxRepository> {

	@Autowired
	private SandboxRepository repository;

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
	public String edit(Map<String, Object> model, @PathVariable Long id) {
		String template = super.edit(model, id);

		Sandbox entity = (Sandbox) model.get("entity");

		// Add options for select.
		model.put("ownerOptions",
				OptionList.fromSearch(userRepository.findAll(), entity.getOwner()));

		return template;
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