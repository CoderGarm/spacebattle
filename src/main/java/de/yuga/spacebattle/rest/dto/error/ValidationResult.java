package de.yuga.spacebattle.rest.dto.error;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolation;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ValidationResult {

    @Nonnull
    private final String property;

    @Nonnull
    private final String message;

    public ValidationResult(@Nonnull final ConstraintViolation<?> violation) {
        this(violation.getPropertyPath().toString(), violation.getMessage());
    }

    public ValidationResult(@Nonnull final String property, @Nonnull final String message) {
        Preconditions.checkNotNull(property, "property shouldn't be null!");
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        this.property = property;
        this.message = message;
    }

    @Nonnull
    public String getProperty() {
        return property;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }
}
