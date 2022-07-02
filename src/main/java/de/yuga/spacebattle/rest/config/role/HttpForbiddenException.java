package de.yuga.spacebattle.rest.config.role;

import javax.annotation.Nullable;

public class HttpForbiddenException extends RuntimeException {

    public HttpForbiddenException(@Nullable final String message) {
        super(message);
    }
}
