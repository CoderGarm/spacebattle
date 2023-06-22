package de.yuga.spacebattle.rest.config.security;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.config.CorsFilterConfiguration;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * This defines the separation of endpoints for every BoF-API from every other endpoint will be used by vaadin.
 * <p>
 * - extend WebSecurityConfigurerAdapter to be able to define different security settings for UI and API
 * see <a href="https://www.baeldung.com/spring-security-multiple-entry-points">...</a>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class HttpSecurityConfiguration {

    @Nonnull
    private final JwtTokenFilter jwtTokenFilter;

    @Autowired
    public HttpSecurityConfiguration(@Nonnull final JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = Preconditions.checkNotNull(jwtTokenFilter, "jwtTokenFilter shouldn't be null!");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors()
                .configurationSource(new CorsFilterConfiguration())
                .and()
                .csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().exceptionHandling()
                .authenticationEntryPoint((request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), new AntPathRequestMatcher("/api/**"))
                .accessDeniedHandler((request, response, ex) -> response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage()))
                .defaultAccessDeniedHandlerFor((request, response, ex) -> response.sendError(HttpServletResponse.SC_FORBIDDEN), new AntPathRequestMatcher("/api/**"));

        return http.build();
    }

    @Bean
    public RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter() {
        RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
        filter.setPrincipalRequestHeader(HttpHeaders.AUTHORIZATION);
        filter.setExceptionIfHeaderMissing(false);
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/**"));
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    @Bean
    protected AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(new RequestHeaderAuthenticationProvider(jwtTokenFilter)));
    }
}



