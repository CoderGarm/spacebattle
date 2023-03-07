package de.yuga.spacebattle.rest.api.error;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;

import javax.annotation.Nonnull;

public class LogInfo {

    @Nonnull
    private String errorMessage = "";

    public LogInfo(@Nonnull final Object subject) {
        Preconditions.checkNotNull(subject, "subject must not be empty");

        final String simpleName = subject.getClass().getSimpleName();
        append("[" + simpleName);
        if (subject instanceof AbstractEntityKey) {
            final int id = ((AbstractEntityKey) subject).getId();
            append(" #" + id);
        }
        appendLN("]");
    }

    @Nonnull
    public LogInfo append(@Nonnull final String msg) {
        Preconditions.checkNotNull(msg, "msg must not be empty");

        errorMessage += msg;
        return this;
    }

    @Nonnull
    public LogInfo appendLN(@Nonnull final String msg) {
        Preconditions.checkNotNull(msg, "msg must not be empty");

        errorMessage += msg + "\n";
        return this;
    }

    @Override
    public String toString() {
        return errorMessage;
    }
}
