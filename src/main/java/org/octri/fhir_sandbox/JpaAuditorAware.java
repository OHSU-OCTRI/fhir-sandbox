package org.octri.fhir_sandbox;

import java.util.Optional;

import org.octri.authentication.server.security.SecurityHelper;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * {@link AuditorAware} Records the username when entity modifications occur.
 */
@Component("jpaAuditorAware")
public class JpaAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        SecurityHelper helper = new SecurityHelper(SecurityContextHolder.getContext());
        if (helper.isAnonymous()) {
            return Optional.empty();
        }
        return Optional.of(helper.username());
    }
}
