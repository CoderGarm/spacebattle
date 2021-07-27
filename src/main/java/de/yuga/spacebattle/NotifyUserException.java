package de.yuga.spacebattle;

public class NotifyUserException extends RuntimeException {

    public NotifyUserException() {
    }

    public NotifyUserException(String message) {
        super(message);
    }
}
