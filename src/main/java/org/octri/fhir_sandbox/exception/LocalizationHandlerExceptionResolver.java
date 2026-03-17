package org.octri.fhir_sandbox.exception;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.octri.fhir_sandbox.config.LocalizationMessageInterceptor;
import org.octri.fhir_sandbox.controller.ApplicationTemplateAdvice;
import org.octri.common.view.ViewUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * An exception handler for the application that includes logic from the LocalizationMessageInterceptor. If these
 * exceptions get handled through ControllerAdvice, the postHandle method of the interceptor gets bypassed because
 * these requests are not considered to have completed "successfully".
 */
@Component
public class LocalizationHandlerExceptionResolver implements HandlerExceptionResolver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String ERROR_ROUTE = "error";

    @Autowired
    ApplicationTemplateAdvice templateAdvice;

    @Autowired
    LocalizationMessageInterceptor messageInterceptor;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {

        ExtendedModelMap model = new ExtendedModelMap();
        templateAdvice.addDefaultAttributes(request, model);
        ModelAndView modelAndView = new ModelAndView(ERROR_ROUTE, model);
        modelAndView.addObject(messageInterceptor.getMessageKey(), messageInterceptor.createContext(request));
        if (messageInterceptor.getScriptName() != null) {
            ViewUtils.addAdminScript(modelAndView.getModel(), messageInterceptor.getScriptName());
        }
        if (ex instanceof DisplayedException) {
            DisplayedException displayedException = (DisplayedException) ex;
            return processError(modelAndView, displayedException.getHttpStatus().value(),
                    displayedException.getHttpStatus().getReasonPhrase(),
                    displayedException.getMessage());
        } else if (ex instanceof AccessDeniedException) {
            return processError(modelAndView, HttpStatus.FORBIDDEN.value(), "Forbidden", "Access Denied");
        } else {
            var integrityViolation = findSQLIntegrityConstraintViolation(ex);
            if (integrityViolation.isPresent()) {
                return processError(modelAndView, HttpStatus.UNPROCESSABLE_ENTITY.value(), "Unprocessable Entity",
                        integrityViolation.get());
            }
        }

        // Continues to throw a 500 and log an alerting error for unhandled exceptions. For some reason, the postHandle
        // method gets called in this case.
        throw new RuntimeException(ex);
    }

    private ModelAndView processError(ModelAndView modelAndView, int status, String error, String message) {
        Map<String, Object> model = modelAndView.getModel();
        model.put("status", status);
        model.put("error", error);
        model.put("message", message);
        model.put("timestamp", new Date());
        return modelAndView;
    }

    public Optional<String> findSQLIntegrityConstraintViolation(Throwable ex) {
        while (ex != null) {
            if (ex instanceof SQLIntegrityConstraintViolationException) {
                String message = ex.getMessage();
                logger.warn("Data constraint violation: " + ex.getMessage());
                if (message != null && message.contains("Duplicate entry")) {
                    return Optional.of("A record with the same value already exists. Please try again.");
                } else {
                    return Optional.of("A data constraint was violated. Please verify your input.");
                }
            }
            ex = ex.getCause();
        }
        return Optional.empty();
    }

}

