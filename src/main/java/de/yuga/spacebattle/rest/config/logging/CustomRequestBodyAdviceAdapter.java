package de.yuga.spacebattle.rest.config.logging;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;

@ControllerAdvice
public class CustomRequestBodyAdviceAdapter extends RequestBodyAdviceAdapter {

    @Nonnull
    @Autowired
    private LoggingService loggingService;

    @Nonnull
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public boolean supports(@Nonnull final MethodParameter methodParameter,
                            @Nonnull final Type type,
                            @Nonnull final Class<? extends HttpMessageConverter<?>> aClass) {
        Preconditions.checkNotNull(methodParameter, "methodParameter shouldn't be null!");
        Preconditions.checkNotNull(type, "type shouldn't be null!");
        Preconditions.checkNotNull(aClass, "aClass shouldn't be null!");

        return true;
    }

    @Nonnull
    @Override
    public Object afterBodyRead(@Nonnull final Object body,
                                @Nonnull final HttpInputMessage inputMessage,
                                @Nonnull final MethodParameter parameter,
                                @Nonnull final Type targetType,
                                @Nonnull final Class<? extends HttpMessageConverter<?>> converterType) {
        Preconditions.checkNotNull(body, "body shouldn't be null!");
        Preconditions.checkNotNull(inputMessage, "inputMessage shouldn't be null!");
        Preconditions.checkNotNull(parameter, "parameter shouldn't be null!");
        Preconditions.checkNotNull(targetType, "targetType shouldn't be null!");
        Preconditions.checkNotNull(converterType, "converterType shouldn't be null!");

        loggingService.logRequest(httpServletRequest, body);

        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }
}