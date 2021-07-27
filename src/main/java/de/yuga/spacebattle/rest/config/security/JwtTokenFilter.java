package de.yuga.spacebattle.rest.config.security;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.account.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Nonnull
    private final JwtTokenUtil jwtTokenUtil;

    @Nonnull
    private final UserService userService;

    @Autowired
    public JwtTokenFilter(@Nonnull final JwtTokenUtil jwtTokenUtil,
                          @Nonnull final UserService userService) {
        Preconditions.checkNotNull(jwtTokenUtil, "jwtTokenUtil shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(@Nonnull final HttpServletRequest request,
                                    @Nonnull final HttpServletResponse response,
                                    @Nonnull final FilterChain chain) throws ServletException, IOException {
        Preconditions.checkNotNull(request, "request shouldn't be null!");
        Preconditions.checkNotNull(response, "response shouldn't be null!");
        Preconditions.checkNotNull(chain, "chain shouldn't be null!");

        // Get authorization header and validate
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(header) || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Get jwt token and validate
        final String token = header.split(" ")[1].trim();
        if (!jwtTokenUtil.validate(token)) {
            chain.doFilter(request, response);
            return;
        }

        // Get user identity and set it on the spring security context
        String username = jwtTokenUtil.getUsernameFromAccessToken(token);
        UserDetails userDetails = userService
                .findByUsername(username)
                .orElse(null);

        UsernamePasswordAuthenticationToken
                authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                userDetails == null ?
                        List.of() : userDetails.getAuthorities()
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

}
