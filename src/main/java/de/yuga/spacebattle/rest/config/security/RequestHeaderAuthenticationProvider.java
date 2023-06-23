package de.yuga.spacebattle.rest.config.security;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.account.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@Service
public class RequestHeaderAuthenticationProvider implements AuthenticationProvider {

    @Nonnull
    private final UserService userService;

    @Autowired
    public RequestHeaderAuthenticationProvider(@Nonnull final UserService userService) {
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
    }

    @Override
    public Authentication authenticate(@Nonnull final Authentication authentication) throws AuthenticationException {
        Preconditions.checkNotNull(authentication, "authentication must not be empty");


        final UserDetails userDetails = userService
                .findByUsername(String.valueOf(authentication.getPrincipal()))
                .orElseThrow(() -> new BadCredentialsException("Bad Request Header Credentials"));

        final Collection<? extends GrantedAuthority> authorities = userDetails == null ? List.of() : userDetails.getAuthorities();
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(token);
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
