package de.yuga.spacebattle.rest.config.context;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestInterceptor extends HandlerInterceptorAdapter {

    @Nonnull
    private final RequestContext requestContext;

    public RequestInterceptor(@Nonnull final RequestContext clientEntity) {
        Preconditions.checkNotNull(clientEntity, "clientEntity must not be empty");

        this.requestContext = clientEntity;
    }

    @Override
    public boolean preHandle(@Nonnull final HttpServletRequest request,
                             @Nullable final HttpServletResponse response,
                             @Nullable final Object handler) {
        PreconditionWebHelper.checkNotNull(request, "request must not be empty");

        final String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        requestContext.setToken(token);
        final String language = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        requestContext.setLanguage(language);

        return true;
    }
}
