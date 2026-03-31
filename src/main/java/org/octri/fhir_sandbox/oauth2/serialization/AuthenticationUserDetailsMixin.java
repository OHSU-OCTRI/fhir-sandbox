package org.octri.fhir_sandbox.oauth2.serialization;

import java.util.Collection;

import org.octri.authentication.server.security.AuthenticationUserDetails;
import org.octri.fhir_sandbox.oauth2.service.JpaOAuth2AuthorizationService;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mixin to support JSON serialization and deserialization of {@link AuthenticationUserDetails} entities. Used by the
 * {@link JpaOAuth2AuthorizationService} when serializing and deserializing the {@link AuthenticationUserDetails}
 * associated with an {@link Authorization}.
 */
public abstract class AuthenticationUserDetailsMixin {

	@JsonCreator
	public AuthenticationUserDetailsMixin(@JsonProperty("userId") long userId,
			@JsonProperty("username") String username, @JsonProperty("password") String password,
			@JsonProperty("enabled") boolean enabled, @JsonProperty("accountNonExpired") boolean accountNonExpired,
			@JsonProperty("credentialsNonExpired") boolean credentialsNonExpired,
			@JsonProperty("accountNonLocked") boolean accountNonLocked,
			@JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities) {
	}

}
