package de.yuga.spacebattle.backend.dto.crew;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * This represents a crew or employees must bring with them to run the facility or module or ship.
 */
public class CrewRequirement {

    /**
     * Yeah, just the crew.
     */
    @Nonnull
    private final Map<EEducationType, Long> crewRequirement = new HashMap<>();

    /**
     * Either costs or deposit. Just to know how to handle this crew.
     */
    private final EDepositType subType;

    public CrewRequirement(@Nonnull final Map<EEducationType, Long> crewRequirement,
                           @Nonnull final EDepositType subType) {
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");
        Preconditions.checkNotNull(subType, "subType shouldn't be null!");

        validateAmountsHard(crewRequirement);
        this.crewRequirement.putAll(crewRequirement);
        this.subType = subType;
    }

    public EDepositType getSubType() {
        return subType;
    }

    /**
     * This is used for copy-constructor in case of an colonization.<br>
     * It will set the subType to deposit because
     */
    public CrewRequirement toggleToDepositMode() {
        return new CrewRequirement(crewRequirement, EDepositType.DEPOSITS);
    }

    /**
     * Returns the sum of all {@link EEducationType}s.
     *
     * @return the sum of all people
     */
    public long getSumOfPopulation() {
        return crewRequirement.values().stream().reduce(0L, Long::sum);
    }

    /**
     * Checks if any amount is negative to prevent that.
     *
     * @param crewRequirement the param to check
     */
    private void validateAmountsHard(@Nonnull final Map<EEducationType, Long> crewRequirement) {
        Preconditions.checkNotNull(crewRequirement, "crewRequirement must not be empty");

        crewRequirement.values().stream().filter(integer -> integer < 0).findAny().ifPresent(e -> {
            throw new NotifyWebUserException("You cannot reduce the amount of people below zero!");
        });
    }

    public long getCrewAmountByType(@Nonnull final EEducationType educationType) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        final Long amount = crewRequirement.get(educationType);
        return amount != null ? amount : 0;
    }
}
