package de.yuga.spacebattle.rest.config.security;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.PasswordConverter;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.config.CorsFilterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * This defines the separation of endpoints for every BoF-API from every other endpoint will be used by vaadin.
 * <p>
 * - extend WebSecurityConfigurerAdapter to be able to define different security settings for UI and API
 * see <a href="https://www.baeldung.com/spring-security-multiple-entry-points">...</a>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        jsr250Enabled = true)
public class HttpSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSecurityConfiguration.class);

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
    public HttpSecurityConfiguration(@Nonnull final UserService userService,
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
        http.cors()
                .configurationSource(new CorsFilterConfiguration())
                .and()
                .csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), new AntPathRequestMatcher("/api/**"))
                .accessDeniedHandler((request, response, ex) -> response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage()))
                .defaultAccessDeniedHandlerFor((request, response, ex) -> response.sendError(HttpServletResponse.SC_FORBIDDEN), new AntPathRequestMatcher("/api/**"));
    }
}



