package de.yuga.spacebattle.rest.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class CorsFilterConfiguration implements CorsConfigurationSource {

    /**
     * todo heavily increase cors filter
     */
    @Override
    public CorsConfiguration getCorsConfiguration(@Nonnull final HttpServletRequest request) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setMaxAge(3600L);
        return corsConfiguration;
    }
}
