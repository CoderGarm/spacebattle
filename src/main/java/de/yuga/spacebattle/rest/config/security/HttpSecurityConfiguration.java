package de.yuga.spacebattle.rest.config.security;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import de.yuga.spacebattle.backend.services.account.PasswordConverter;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.EndpointDefinition;
import de.yuga.spacebattle.rest.config.CorsFilterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.Nonnull;

/**
 * This defines the separation of endpoints for every BoF-API from every other endpoint will will be used by vaadin.
 * <p>
 * - extend WebSecurityConfigurerAdapter to be able to define different security settings for UI and API
 * see https://www.baeldung.com/spring-security-multiple-entry-points
 */
@Configuration
@EnableWebSecurity
public class HttpSecurityConfiguration {

    /**
     * ONLY FOR http at /api/**
     * all api requests will have "Authorization" header which I can use to authenticate, if valid. no login needed
     */
    @Configuration
    @Order(1)
    public static class RestSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Nonnull
        private final UserService userService;

        @Nonnull
        private final JwtTokenFilter jwtTokenFilter;

        @Override
        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Autowired
        public RestSecurityConfigurationAdapter(@Nonnull final UserService userService,
                                                @Nonnull final JwtTokenFilter jwtTokenFilter) {
            Preconditions.checkNotNull(userService, "userService shouldn't be null!");
            Preconditions.checkNotNull(jwtTokenFilter, "jwtTokenFilter shouldn't be null!");

            this.userService = userService;
            this.jwtTokenFilter = jwtTokenFilter;
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(username -> userService
                    .findByUsername(username)
                    .orElseThrow(
                            () -> new UsernameNotFoundException("User '" + username + "' could not be found."))
            );
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new PasswordConverter();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // Enable CORS and disable CSRF
            http.cors()
                    .configurationSource(new CorsFilterConfiguration())
                    .and().csrf().disable();

            // Set session management to stateless
            http = http
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and();

            // Set unauthorized requests exception handler
            http.exceptionHandling()
                    //.authenticationEntryPoint((request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

            // Set permissions on endpoints
            http.authorizeRequests()
                    // Our public endpoints
                    .antMatchers("/" + EndpointDefinition.PUBLIC_BASE_ENDPOINT + "/**").permitAll()
                    .antMatchers("/vaadin/**").permitAll()
                    .antMatchers("/v2/**").permitAll()
                    .antMatchers("/v3/**").permitAll()
                    .antMatchers("/swagger-ui/**").permitAll()
                    .antMatchers("/swagger-resources/**").permitAll()
                    // Our private endpoints
                    .antMatchers("/" + EndpointDefinition.PRIVATE_BASE_ENDPOINT + "/**")
                    .hasRole(EWebUserRole.USER.toString())
                    .anyRequest()
                    .authenticated();

            // Add JWT token filter
            http.addFilterBefore(
                    jwtTokenFilter,
                    UsernamePasswordAuthenticationFilter.class
            );
        }

        // Used by spring security if CORS is enabled - todo try it
        //@Bean
        public CorsFilter corsFilter() {
            UrlBasedCorsConfigurationSource source =
                    new UrlBasedCorsConfigurationSource();
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.addAllowedOrigin("*");
            config.addAllowedHeader("*");
            config.addAllowedMethod("*");
            source.registerCorsConfiguration("/**", config);
            return new CorsFilter(source);
        }
    }
}

