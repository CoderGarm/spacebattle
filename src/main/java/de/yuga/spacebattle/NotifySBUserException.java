package de.yuga.spacebattle;

public class NotifySBUserException extends RuntimeException {

    public NotifySBUserException() {
    }

    public NotifySBUserException(String message) {
        super(message);
    }

    public NotifySBUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotifySBUserException(Throwable cause) {
        super(cause);
    }

    public NotifySBUserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
