package de.yuga.spacebattle.backend.entities.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PayingPossibleResult {

    @Nonnull
    private final List<EResourceType> resourceTypes = new ArrayList<>();

    @Nonnull
    private final List<EEducationType> educationTypes = new ArrayList<>();

    @Nonnull
    private final String message = "too expensive";

    public PayingPossibleResult() {
    }

    public void addProblem(@Nonnull final Enum<?> problematicType) {
        Preconditions.checkNotNull(problematicType, "problematicType shouldn't be null!");

        if (problematicType instanceof EResourceType) {
            resourceTypes.add((EResourceType) problematicType);
        } else if (problematicType instanceof EEducationType) {
            educationTypes.add((EEducationType) problematicType);
        } else {
            throw new NotifyWebUserException("No, this is not an validation result!");
        }
    }

    public boolean isValid() {
        return resourceTypes.isEmpty() && educationTypes.isEmpty();
    }

    public List<String> getResult() {
        final List<String> collect = resourceTypes.stream().map(Object::toString).collect(Collectors.toList());
        collect.addAll(educationTypes.stream().map(Object::toString).collect(Collectors.toList()));
        return collect;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }
}
