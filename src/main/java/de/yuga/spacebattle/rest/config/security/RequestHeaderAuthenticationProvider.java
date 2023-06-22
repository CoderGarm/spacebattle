package de.yuga.spacebattle.rest.config.security;

import com.google.common.base.Preconditions;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class RequestHeaderAuthenticationProvider implements AuthenticationProvider {

    @Nonnull
    private final JwtTokenFilter jwtTokenFilter;

    public RequestHeaderAuthenticationProvider(@Nonnull final JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = Preconditions.checkNotNull(jwtTokenFilter, "jwtTokenFilter shouldn't be null!");
    }

    @Override
    public Authentication authenticate(@Nonnull final Authentication authentication) throws AuthenticationException {
        Preconditions.checkNotNull(authentication, "authentication must not be empty");

        if (!authentication.isAuthenticated()) {
            throw new BadCredentialsException("Bad Request Header Credentials");
        }

        return new PreAuthenticatedAuthenticationToken(authentication.getPrincipal(), null, new ArrayList<>());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
