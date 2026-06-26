package org.octri.fhir_sandbox.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Intercepts standalone SMART on FHIR launches arriving at the OAuth2 authorization endpoint.
 * <p>
 * A standalone launch is one where the SMART app calls {@code /oauth2/authorize} without a
 * {@code launch} parameter — the app discovered the authorization endpoint from the FHIR
 * server's {@code .well-known/smart-configuration} and initiated OAuth2 itself. In this case
 * we need to collect patient/practitioner context before the authorization can proceed.
 * <p>
 * When a standalone launch is detected (authenticated user, {@code client_id} present, no
 * {@code launch} param), this filter saves the original authorization request parameters to the
 * HTTP session and redirects the user to the standalone launch picker page. After the user
 * selects context, the picker redirects back to {@code /oauth2/authorize} with the original
 * params plus a {@code launch} parameter, at which point this filter passes through normally.
 */
public class StandaloneLaunchFilter extends OncePerRequestFilter {

    public static final String SESSION_KEY_PREFIX = "standalone_launch_params_";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!isStandaloneLaunchRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!isAuthenticated(SecurityContextHolder.getContext().getAuthentication())) {
            // Not yet authenticated — let Spring Security redirect to login; we will intercept
            // again after the user logs in and is sent back to /oauth2/authorize.
            filterChain.doFilter(request, response);
            return;
        }

        String key = UUID.randomUUID().toString();
        request.getSession().setAttribute(SESSION_KEY_PREFIX + key, new HashMap<>(request.getParameterMap()));
        response.sendRedirect(request.getContextPath() + "/smart/standalone-launch?key=" + key);
    }

    private boolean isStandaloneLaunchRequest(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod())
                && "/oauth2/authorize".equals(request.getServletPath())
                && request.getParameter("client_id") != null
                && request.getParameter("launch") == null;
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

}
