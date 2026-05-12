package org.octri.fhir_sandbox.controller;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.octri.authentication.server.security.SecurityHelper;
import org.octri.authentication.server.security.service.UserService;
import org.octri.common.view.OptionList;
import org.octri.common.view.ViewUtils;
import org.octri.fhir_sandbox.domain.ClientType;
import org.octri.fhir_sandbox.domain.Sandbox;
import org.octri.fhir_sandbox.domain.SmartClient;
import org.octri.fhir_sandbox.domain.SmartLaunchContextProperties;
import org.octri.fhir_sandbox.service.SandboxService;
import org.octri.fhir_sandbox.service.SmartClientService;
import org.octri.fhir_sandbox.service.SmartLaunchContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Home page controller.
 */
@RestController
public class HomeController {

	private static final Logger log = LoggerFactory.getLogger(HomeController.class);

	private static final String BASE_ROUTE = "/sandboxes";

	private final UserService userService;
	private final SandboxService sandboxService;
	private final SmartClientService clientService;
	private final SmartLaunchContextService launchContextService;

	// TODO: RFS-249 remove stub IDs
	@Value("${octri.sandbox.stub-id.patient}")
	private String stubPatientId;

	@Value("${octri.sandbox.stub-id.practitioner}")
	private String stubPractitionerId;

	public HomeController(UserService userService, SandboxService sandboxService, SmartClientService clientService,
			SmartLaunchContextService launchContextService) {
		this.userService = userService;
		this.sandboxService = sandboxService;
		this.clientService = clientService;
		this.launchContextService = launchContextService;
	}

	@GetMapping("/")
	public ModelAndView welcome(Map<String, Object> model) {

		var securityHelper = new SecurityHelper(SecurityContextHolder.getContext());
		if (securityHelper.isLoggedIn()) {
			return new ModelAndView("redirect:/sandboxes/");
		}

		return new ModelAndView("login", model);
	}

	@GetMapping("/sandboxes")
	public ModelAndView sandboxListRedirect() {
		return new ModelAndView("redirect:/sandboxes/");
	}

	@GetMapping("/sandboxes/")
	public ModelAndView sandboxList(Map<String, Object> model) {
		var securityHelper = new SecurityHelper(SecurityContextHolder.getContext());
		var currentUser = userService.findByUsername(securityHelper.username());
		var sandboxes = sandboxService.getSandboxesForUser(currentUser);
		model.put("baseRoute", BASE_ROUTE);
		model.put("pageTitle", "Your Sandboxes");
		model.put("sandboxes", sandboxes);
		model.put("hasSandboxes", !sandboxes.isEmpty());
		return new ModelAndView("home/sandbox_list", model);
	}

	@GetMapping("/sandboxes/new")
	public ModelAndView newSandbox(Map<String, Object> model) {
		var securityHelper = new SecurityHelper(SecurityContextHolder.getContext());
		var currentUser = userService.findByUsername(securityHelper.username());
		var entity = new Sandbox();
		entity.setOwner(currentUser);
		model.put("baseRoute", BASE_ROUTE);
		model.put("entityName", "Sandbox");
		model.put("entity", entity);
		model.put("submitAction", sandboxFormAction(entity));
		return new ModelAndView("home/sandbox_form", model);
	}

	@PostMapping("/sandboxes/create")
	public ModelAndView createSandbox(Map<String, Object> model,
			@RequestParam(value = "importSampleData", required = false) Boolean importSampleData,
			@Valid @ModelAttribute("entity") Sandbox sandbox,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		var securityHelper = new SecurityHelper(SecurityContextHolder.getContext());
		var currentUser = userService.findByUsername(securityHelper.username());
		sandbox.setOwner(currentUser);
		var savedSandbox = sandboxService.createSandbox(sandbox);
		sandboxService.initializeSandbox(savedSandbox, importSampleData);
		model.put("newEntity", savedSandbox);
		redirectAttributes.addFlashAttribute("successMessage", "Sandbox successfully created.");
		return new ModelAndView("redirect:/sandboxes/" + savedSandbox.getId());
	}

	@GetMapping("/sandboxes/{id}")
	public ModelAndView showSandbox(Map<String, Object> model, @PathVariable Long id) {
		var sandbox = sandboxService.findById(id).get();
		var clients = sandboxService.getClientsForSandbox(sandbox);

		ViewUtils.addPageScript(model, "launch-client.js");
		model.put("baseRoute", BASE_ROUTE);
		model.put("entity", sandbox);
		model.put("fhirServerUrl", sandboxService.getSandboxFhirUrl(sandbox));
		model.put("clients", clients);
		model.put("hasClients", !clients.isEmpty());
		// TODO: RFS-249 remove stub IDs
		model.put("stubPatientId", stubPatientId);
		model.put("stubPractitionerId", stubPractitionerId);

		return new ModelAndView("home/sandbox_details", model);
	}

	@GetMapping("/sandboxes/{id}/edit")
	public ModelAndView editSandbox(Map<String, Object> model, @PathVariable Long id) {
		var entity = sandboxService.findById(id).get();
		model.put("baseRoute", BASE_ROUTE);
		model.put("entity", entity);
		model.put("entityName", "Sandbox");
		model.put("submitAction", sandboxFormAction(entity));
		return new ModelAndView("home/sandbox_form", model);
	}

	@PostMapping("/sandboxes/{id}/update")
	public ModelAndView updateSandbox(Map<String, Object> model, @PathVariable Long id,
			@Valid @ModelAttribute("entity") Sandbox sandbox, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		var savedSandbox = sandboxService.save(sandbox);
		model.put("newEntity", savedSandbox);
		redirectAttributes.addFlashAttribute("successMessage", "Sandbox successfully updated.");
		return new ModelAndView("redirect:/sandboxes/" + savedSandbox.getId());
	}

	@GetMapping("/sandboxes/{sandboxId}/delete")
	public ModelAndView delete(@PathVariable("sandboxId") Long sandboxId, RedirectAttributes redirectAttributes) {
		try {
			sandboxService.deleteById(sandboxId);
		} catch (DataIntegrityViolationException e) {
			String msg = "Sandbox is in use and cannot be deleted.";
			redirectAttributes.addFlashAttribute("errorMessage", msg);
			return new ModelAndView("redirect:/sandboxes/" + sandboxId);
		}

		String msg = "Sandbox with id " + sandboxId + " successfully deleted.";
		redirectAttributes.addFlashAttribute("infoMessage", msg);
		return new ModelAndView("redirect:/sandboxes/");
	}

	@GetMapping("/sandboxes/{sandboxId}/client/new")
	public ModelAndView newClient(Map<String, Object> model, @PathVariable Long sandboxId) {
		Sandbox sandbox = sandboxService.findById(sandboxId).get();
		setupClientForm(model, sandbox, null);
		return new ModelAndView("home/client_form", model);
	}

	@PostMapping("/sandboxes/{sandboxId}/client/create")
	public ModelAndView createClient(Map<String, Object> model, @PathVariable Long sandboxId,
			@Valid @ModelAttribute("entity") SmartClient client,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		Sandbox sandbox = sandboxService.findById(sandboxId).get();
		client.setSandbox(sandbox);
		clientService.save(client);
		redirectAttributes.addFlashAttribute("successMessage", "Client successfully created.");
		return new ModelAndView("redirect:/sandboxes/" + sandboxId);
	}

	@GetMapping("/sandboxes/{sandboxId}/client/{id}/edit")
	public ModelAndView editClient(Map<String, Object> model, @PathVariable Long sandboxId,
			@PathVariable Long id) {
		Sandbox sandbox = sandboxService.findById(sandboxId).get();
		SmartClient client = clientService.findById(id).get();
		setupClientForm(model, sandbox, client);
		return new ModelAndView("home/client_form", model);
	}

	@PostMapping("/sandboxes/{sandboxId}/client/{id}/update")
	public ModelAndView updateClient(Map<String, Object> model, @PathVariable Long sandboxId, @PathVariable Long id,
			@Valid @ModelAttribute("entity") SmartClient client, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		Sandbox sandbox = sandboxService.findById(sandboxId).get();
		client.setSandbox(sandbox);
		clientService.save(client);
		redirectAttributes.addFlashAttribute("successMessage", "Client successfully updated.");
		return new ModelAndView("redirect:/sandboxes/" + sandboxId);
	}

	@GetMapping("/sandboxes/{sandboxId}/client/{id}/delete")
	public ModelAndView deleteClient(Map<String, Object> model, @PathVariable Long sandboxId,
			@PathVariable Long id, RedirectAttributes redirectAttributes) {
		Sandbox sandbox = sandboxService.findById(sandboxId).get();
		SmartClient client = clientService.findById(id).get();
		Assert.isTrue(client.getSandbox().equals(sandbox), "Client does not belong to sandbox");
		clientService.delete(client);
		redirectAttributes.addFlashAttribute("successMessage", "Client successfully deleted.");
		return new ModelAndView("redirect:/sandboxes/" + sandboxId);
	}

	@PostMapping(value = "/create_context", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> createLaunchContext(@RequestBody SmartLaunchContextProperties payload) {
		var response = new HashMap<String, Object>();

		if (payload.clientId() == null) {
			response.put("error", "Client ID");
			return response;
		}

		if (payload.patientId() == null && payload.encounterId() == null) {
			response.put("error", "Either patient ID or encounter ID is required");
			return response;
		}

		var context = launchContextService.createLaunchContext(payload);
		response.put("id", context.getOpaqueId());

		return response;
	}

	private void setupClientForm(Map<String, Object> model, Sandbox sandbox, SmartClient client) {
		if (client == null) {
			client = new SmartClient();
			client.setSandbox(sandbox);
			client.setClientId(UUID.randomUUID().toString());
			client.setClientType(ClientType.PUBLIC);
		}
		model.put("entity", client);
		model.put("baseRoute", BASE_ROUTE);
		model.put("entityName", "Client");
		model.put("submitAction", clientFormAction(sandbox, client));
		model.put("cancelAction", BASE_ROUTE + "/" + sandbox.getId());
		model.put("clientTypes", OptionList.fromEnum(EnumSet.allOf(ClientType.class), client.getClientType()));
		model.put("sandbox", sandbox);
	}

	private String sandboxFormAction(Sandbox sandbox) {
		if (sandbox.getId() == null) {
			return BASE_ROUTE + "/create";
		} else {
			return BASE_ROUTE + "/" + sandbox.getId() + "/update";
		}
	}

	private String clientFormAction(Sandbox sandbox, SmartClient client) {
		if (client.getId() == null) {
			return BASE_ROUTE + "/" + sandbox.getId() + "/client/create";
		} else {
			return BASE_ROUTE + "/" + sandbox.getId() + "/client/" + client.getId() + "/update";
		}
	}

}