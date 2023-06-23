package de.yuga.spacebattle.rest.dto.error;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Just the general response for an json-error.
 * Think about registering new classes in {@link de.yuga.spacebattle.SpacebattleApplication#api()}.
 */
@Schema(description = ".")
public class FrontendError {

    @Nonnull
    @Schema(description = "The Error message.")
    private final String message;

    @Nonnull
    @Schema(description = "If it's not empty, some validation shows violations.")
    private final List<ValidationResult> validationResults = new ArrayList<>();

    public FrontendError(@Nonnull final String message) {
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        this.message = message;
    }

    public FrontendError(@Nonnull final NotifyWebUserException exception) {
        Preconditions.checkNotNull(exception, "exception shouldn't be null!");

        this.message = exception.getMessage();
        final Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
        constraintViolations.forEach(v -> validationResults.add(new ValidationResult(v)));

        final PayingPossibleResult payingPossibleResult = exception.getPayingPossibleResult();
        if (payingPossibleResult != null) {
            final String message = payingPossibleResult.getMessage();
            final List<String> result = payingPossibleResult.getResult();
            result.forEach(property -> validationResults.add(new ValidationResult(property, message)));
        }
    }

    @Nonnull
    public String getMessage() {
        return message;
    }

    @Nonnull
    public List<ValidationResult> getValidationResults() {
        return validationResults;
    }
}
