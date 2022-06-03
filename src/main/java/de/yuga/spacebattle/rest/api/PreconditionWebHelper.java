package de.yuga.spacebattle.rest.api;

import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.config.RestControllerExceptionHandler;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.Nonnull;

/**
 * Just a bunch of helper methods to validate arguments and in case of a violation a {@link NotifyWebUserException} will be thrown.
 * This exception will be caught by {@link RestControllerExceptionHandler} and transformed into a rest response.
 */
public class PreconditionWebHelper {

    /**
     * Checks if the parameter is null and if it is then a {@link NotifyWebUserException} with the given message will be thrown.
     *
     * @param reference    the argument to check
     * @param errorMessage the error message to promote
     * @return the reference
     */
    @Nonnull
    public static Object checkNotNull(Object reference, @Nullable Object errorMessage) {
        if (reference == null) {
            throw new NotifyWebUserException(String.valueOf(errorMessage));
        }
        return reference;
    }

    /**
     * Checks if the given expression is true and if not a {@link NotifyWebUserException} with the given message will be thrown.
     *
     * @param expression   the expression to check
     * @param errorMessage the error message to promote
     */
    public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
        if (expression) {
            throw new NotifyWebUserException(String.valueOf(errorMessage));
        }
    }

}
