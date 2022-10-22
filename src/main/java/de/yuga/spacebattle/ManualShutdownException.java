package de.yuga.spacebattle;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public class ManualShutdownException extends RuntimeException {

    @Nonnull
    private final String action;

    public ManualShutdownException(@Nonnull final String message, @Nonnull final String action) {
        super(message);
        this.action = Preconditions.checkNotNull(action, "action must not be empty");
    }

    @Nonnull
    public String getAction() {
        return action;
    }
}
