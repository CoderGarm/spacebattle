package de.yuga.spacebattle.rest.config.logging;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ControllerAdvice
public class CustomResponseBodyAdviceAdapter implements ResponseBodyAdvice<Object> {

    @Nonnull
    private final LoggingService loggingService;

    @Autowired
    public CustomResponseBodyAdviceAdapter(@Nonnull final LoggingService loggingService) {
        Preconditions.checkNotNull(loggingService, "loggingService shouldn't be null!");

        this.loggingService = loggingService;
    }

    @Override
    public boolean supports(@Nonnull final MethodParameter methodParameter,
                            @Nonnull final Class<? extends HttpMessageConverter<?>> aClass) {
        Preconditions.checkNotNull(methodParameter, "methodParameter shouldn't be null!");
        Preconditions.checkNotNull(aClass, "aClass shouldn't be null!");

        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable final Object o,
                                  @Nonnull final MethodParameter methodParameter,
                                  @Nonnull final MediaType mediaType,
                                  @Nonnull final Class<? extends HttpMessageConverter<?>> aClass,
                                  @Nonnull final ServerHttpRequest serverHttpRequest,
                                  @Nonnull final ServerHttpResponse serverHttpResponse) {
        Preconditions.checkNotNull(methodParameter, "methodParameter shouldn't be null!");
        Preconditions.checkNotNull(mediaType, "mediaType shouldn't be null!");
        Preconditions.checkNotNull(aClass, "aClass shouldn't be null!");
        Preconditions.checkNotNull(serverHttpRequest, "serverHttpRequest shouldn't be null!");
        Preconditions.checkNotNull(serverHttpResponse, "serverHttpResponse shouldn't be null!");

        if (serverHttpRequest instanceof ServletServerHttpRequest &&
                serverHttpResponse instanceof ServletServerHttpResponse) {
            loggingService.logResponse(
                    ((ServletServerHttpRequest) serverHttpRequest).getServletRequest(),
                    ((ServletServerHttpResponse) serverHttpResponse).getServletResponse(), o);
        }

        return o;
    }
}
