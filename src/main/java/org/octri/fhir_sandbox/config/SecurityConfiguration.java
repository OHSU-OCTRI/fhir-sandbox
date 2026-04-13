package org.octri.fhir_sandbox.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.octri.authentication.DefaultSecurityConfigurer;
import org.octri.authentication.config.AuthenticationRouteProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Custom security configuration.
 */
@Configuration
@EnableConfigurationProperties(OAuth2ServerProperties.class)
public class SecurityConfiguration {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

	@Autowired
	private AuthenticationRouteProperties routes;

	@Autowired
	private DefaultSecurityConfigurer securityConfigurer;

	/**
	 * Configure the security filter chain for the OAuth2 authorization server endpoints. Must be ordered before the
	 * main security filter chain.
	 *
	 * @param http
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer
				.authorizationServer();

		http
				.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
				.with(authorizationServerConfigurer, (authorizationServer) -> authorizationServer.oidc(withDefaults()))
				.authorizeHttpRequests((authorize) -> authorize
						.anyRequest().authenticated())
				.exceptionHandling((exceptions) -> exceptions
						.defaultAuthenticationEntryPointFor(
								new LoginUrlAuthenticationEntryPoint("/login"),
								new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
				.cors(withDefaults());

		return http.build();

	}

	/**
	 * Set up basic authentication and restrict requests based on HTTP methods,
	 * URLS, and roles.
	 */
	@Bean
	@Order(2)
	public SecurityFilterChain configure(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
		securityConfigurer.configureAuthenticationManager(authBuilder);
		AuthenticationManager authManager = authBuilder.build();

		http.authenticationManager(authManager)
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(routes.getLoginUrl())))
				.csrf(withDefaults())
				.cors(withDefaults());

		securityConfigurer.configureContentSecurityPolicy(http);
		securityConfigurer.configureFormLoginWithDefaults(http);
		securityConfigurer.configureLogoutWithDefaults(http);
		securityConfigurer.configureSamlWithDefaults(http, authManager);

		http.authorizeHttpRequests(authRequests -> authRequests.requestMatchers(routes.getPublicRoutesWithDefaults())
				.permitAll()
				// Admin pages are not available to basic users
				.requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER")
				.requestMatchers(HttpMethod.POST).authenticated()
				.requestMatchers(HttpMethod.PUT).authenticated()
				.requestMatchers(HttpMethod.PATCH).authenticated()
				.requestMatchers(HttpMethod.DELETE).denyAll()
				.anyRequest()
				.authenticated());

		return http.build();
	}

	/**
	 * Configure CORS to allow requests from the client applications.
	 *
	 * TODO: Get allowed origins from registered clients.
	 *
	 * @return
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.addAllowedOriginPattern("http://localhost:*");
		config.setAllowCredentials(true);
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}
