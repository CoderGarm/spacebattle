package de.yuga.spacebattle.rest.api.error;

import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

public class NotifyWebUserException extends RuntimeException {

    @Nonnull
    private final Set<ConstraintViolation<?>> constraintViolations = new HashSet<>();

    @Nullable
    private PayingPossibleResult payingPossibleResult;

    public NotifyWebUserException() {
    }

    public NotifyWebUserException(String message) {
        super(message);
    }

    public NotifyWebUserException(@Nonnull final String message, @Nullable final PayingPossibleResult payingPossibleResult) {
        super(message);

        this.payingPossibleResult = payingPossibleResult;
    }

    public <T> NotifyWebUserException(@Nonnull final String message, @Nonnull final Set<ConstraintViolation<T>> constraintViolations) {
        super(message);

        this.constraintViolations.addAll(constraintViolations);
    }

    @Nonnull
    public Set<ConstraintViolation<?>> getConstraintViolations() {
        return constraintViolations;
    }

    @Nullable
    public PayingPossibleResult getPayingPossibleResult() {
        return payingPossibleResult;
    }
}
