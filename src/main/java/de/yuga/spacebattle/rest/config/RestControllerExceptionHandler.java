package de.yuga.spacebattle.rest.config;

import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.config.role.HttpForbiddenException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Defines the exception handling for ever rest endpoint.
 */
@RestControllerAdvice
public class RestControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestControllerExceptionHandler.class);

    @ExceptionHandler(NotifyWebUserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<FrontendError> handleConversion(@Nonnull final NotifyWebUserException ex) {
        return new ResponseEntity<>(new FrontendError(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<FrontendError> handleConversion(@Nonnull final Exception ex) {
        final String stacktrace = ExceptionUtils.getStackTrace(ex);
        final String uuid = UUID.randomUUID().toString().split("-")[0];
        LOGGER.warn(uuid + "\n" + stacktrace);
        return new ResponseEntity<>(new FrontendError("Something went wrong. Send a mail and mention '" + uuid + "'."), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected ResponseEntity<FrontendError> handleConversion(@Nonnull final AccessDeniedException ex) {
        return new ResponseEntity<>(new FrontendError("Denied."), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    protected ResponseEntity<FrontendError> handleConversion(@Nonnull final HttpForbiddenException ex) {
        return new ResponseEntity<>(new FrontendError("Forbidden."), HttpStatus.FORBIDDEN);
    }
}
