package org.octri.fhir_sandbox.controller;

import java.time.Instant;
import java.util.Map;

import org.octri.fhir_sandbox.oauth2.repository.ClientRepository;
import org.octri.fhir_sandbox.oauth2.service.JpaRegisteredClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/data/client")
public class ClientController {

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private JpaRegisteredClientRepository registeredClientRepository;

	@GetMapping("/")
	public String list(Map<String, Object> model) {
		model.put("clients", clientRepository.findAll());
		return "client/list";
	}

	@GetMapping("/new")
	public String form(Map<String, Object> model) {
		return "client/form";
	}

	@PostMapping("/create")
	public String create(Map<String, Object> model, RedirectAttributes redirectAttributes) {
		RegisteredClient registeredClient = RegisteredClient.withId("b0ee91a6-a49b-40fb-a31c-13bb24b3c059")
				.clientId("96c897b3-59ed-4700-b39c-b44c564f9516")
				.clientIdIssuedAt(Instant.now())
				.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("http://localhost:5173/app.html")
				.scope("launch")
				.scope("launch/patient")
				.scope("online_access")
				.scope("patient/Patient.read")
				.scope("patient/Observation.read")
				.scope("patient/Observation.write")
				.clientSettings(
						ClientSettings.builder()
								.requireAuthorizationConsent(true)
								.requireProofKey(true)
								.build())
				.build();

		registeredClientRepository.save(registeredClient);
		redirectAttributes.addFlashAttribute("successMessage", "Client created successfully.");
		return "redirect:/data/client/";
	}

	@GetMapping("/redirect")
	public String redirect(Map<String, Object> model, RedirectAttributes redirectAttributes) {
		return "redirect:http://localhost:5173/launch.html?iss=http://localhost:8000/fhir/67b7da02-e178-4e91-b660-bd3b9990735d";
	}
}
