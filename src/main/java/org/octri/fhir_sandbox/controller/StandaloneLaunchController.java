package org.octri.fhir_sandbox.controller;

import java.util.Map;

import org.octri.authentication.server.security.SecurityHelper;
import org.octri.authentication.server.security.service.UserService;
import org.octri.common.view.ViewUtils;
import org.octri.fhir_sandbox.domain.SmartLaunchContextProperties;
import org.octri.fhir_sandbox.exception.DisplayedException;
import org.octri.fhir_sandbox.filter.StandaloneLaunchFilter;
import org.octri.fhir_sandbox.service.SmartLaunchContextService;
import org.octri.fhir_sandbox.service.StandaloneLaunchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Handles the standalone SMART on FHIR launch flow. When a SMART app initiates a standalone launch it calls
 * the authorization endpoint without a {@code launch} parameter. {@link StandaloneLaunchFilter} intercepts
 * that request, stores the original OAuth2 parameters in the session, and redirects here so the user can select
 * patient and practitioner context. Once context is selected, {@link #complete} creates a launch context
 * and sends the browser back to the authorization endpoint with the original parameters plus the
 * new {@code launch} value, resuming the normal authorization flow.
 */
@RestController
@RequestMapping("/smart/standalone-launch")
public class StandaloneLaunchController {

	private final UserService userService;
	private final SmartLaunchContextService launchContextService;
	private final StandaloneLaunchService standaloneLaunchService;

	public StandaloneLaunchController(UserService userService, SmartLaunchContextService launchContextService,
			StandaloneLaunchService standaloneLaunchService) {
		this.userService = userService;
		this.launchContextService = launchContextService;
		this.standaloneLaunchService = standaloneLaunchService;
	}

	public record CompleteRequest(String key, String clientId, String patientId, String fhirUser) {
	}

	/**
	 * Renders the patient/practitioner picker for a standalone launch. Retrieves the saved OAuth2 parameters from the
	 * session then delegates to {@link StandaloneLaunchService} to validate access and build the picker data.
	 */
	@GetMapping
	public ModelAndView picker(@RequestParam String key, Map<String, Object> model, HttpSession session) {
		var params = sessionParams(key, session);
		if (params == null) {
			throw new DisplayedException(HttpStatus.BAD_REQUEST, "Invalid or expired launch session");
		}

		var clientIdValues = params.get("client_id");
		if (clientIdValues == null || clientIdValues.length == 0) {
			throw new DisplayedException(HttpStatus.BAD_REQUEST, "Missing client_id in launch session");
		}

		var currentUser = userService.findByUsername(
				new SecurityHelper(SecurityContextHolder.getContext()).username());
		var pickerData = standaloneLaunchService.validateAndGetPickerData(clientIdValues[0], currentUser);

		model.put("sessionKey", key);
		model.put("clientId", pickerData.client().getClientId());
		model.put("fhirApi", pickerData.fhirServerUrl());
		model.put("accessToken", pickerData.accessToken());

		ViewUtils.addPageScript(model, "standalone-launch.ts");

		return new ModelAndView("standalone_launch/picker", model);
	}

	/**
	 * Creates the launch context from the user's selection and redirects back to the OAuth2
	 * authorization endpoint with the original parameters plus the new {@code launch} value.
	 * 
	 * @param payload
	 * @param session
	 * @param request
	 * @return
	 */
	@PostMapping(value = "/complete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> complete(@RequestBody CompleteRequest payload,
			HttpSession session, HttpServletRequest request) {

		var params = sessionParams(payload.key(), session);
		if (params == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired launch session"));
		}

		if (payload.clientId() == null || payload.patientId() == null) {
			return ResponseEntity.badRequest()
					.body(Map.of("error", "clientId and patientId are required"));
		}

		var contextProps = new SmartLaunchContextProperties(
				payload.clientId(), payload.patientId(), null, payload.fhirUser());
		var context = launchContextService.createLaunchContext(contextProps);

		session.removeAttribute(StandaloneLaunchFilter.SESSION_KEY_PREFIX + payload.key());

		var authorizeUrl = standaloneLaunchService.buildAuthorizeUrl(
				request.getContextPath(), params, context.getOpaqueId());

		return ResponseEntity.ok(Map.of("authorizeUrl", authorizeUrl));
	}

	@SuppressWarnings("unchecked")
	private Map<String, String[]> sessionParams(String key, HttpSession session) {
		return (Map<String, String[]>) session.getAttribute(StandaloneLaunchFilter.SESSION_KEY_PREFIX + key);
	}

}
