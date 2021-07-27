package de.yuga.spacebattle.rest.config;

import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Defines the exception handling for ever rest endpoint.
 */
@RestControllerAdvice
public class RestControllerResponseExceptionHandler {

    @ExceptionHandler(NotifyWebUserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<FrontendError> handleConversion(NotifyWebUserException ex) {
        return new ResponseEntity<>(new FrontendError(ex), HttpStatus.BAD_REQUEST);
    }
}
