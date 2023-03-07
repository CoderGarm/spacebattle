package de.yuga.spacebattle.rest.api.error;

import com.google.common.base.Preconditions;
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

    @Nullable
    private LogInfo logInfo;

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

    public NotifyWebUserException(@Nonnull final String message, @Nonnull final LogInfo logInfo) {
        super(message);
        Preconditions.checkNotNull(logInfo, "logInfo must not be empty");

        this.logInfo = logInfo;
    }

    @Nonnull
    public Set<ConstraintViolation<?>> getConstraintViolations() {
        return constraintViolations;
    }

    @Nullable
    public PayingPossibleResult getPayingPossibleResult() {
        return payingPossibleResult;
    }

    @Nullable
    public LogInfo getLogInfo() {
        return logInfo;
    }

    public boolean isLoggingNecessary() {
        return logInfo != null;
    }

    @Override
    public String toString() {
        return logInfo != null ? logInfo.toString() : "";
    }
}
