package de.yuga.spacebattle.backend.entities.crew;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This represents a crew or employees must bring with them to run the facility or module or ship.
 */
public class CrewRequirementDTO {

    /**
     * Yeah, just the crew.
     */
    @Nonnull
    private final Map<EEducationType, Long> crewRequirement = new HashMap<>();

    /**
     * Either costs or deposit. Just to know how to handle this crew.
     */
    private final EDepositType subType;

    public CrewRequirementDTO(@Nonnull final Map<EEducationType, Long> crewRequirement,
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
     * Returns the sum of all {@link EEducationType}s.
     *
     * @return the sum of all people
     */
    public long getSumOfPopulation() {
        return crewRequirement.values().stream().reduce(0L, Long::sum);
    }

    /**
     * This is used for copy-constructor in case of an colonization.<br>
     * It will set the subType to deposit because
     */
    public CrewRequirementDTO toggleToDepositMode() {
        return new CrewRequirementDTO(crewRequirement, EDepositType.DEPOSITS);
    }

    /**
     * Checks if any amount is negative to prevent that.
     *
     * @param crewRequirement the param to check
     */
    private void validateAmountsHard(@Nonnull Map<EEducationType, Long> crewRequirement) {
        crewRequirement.values().stream().filter(integer -> integer < 0).findAny().ifPresent(e -> {
            throw new NotifySBUserException("You cannot reduce the amount of people below zero!");
        });
    }

    public void setAbsoluteCrewRequirement(@Nonnull final EEducationType educationType, final long amount) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        if (amount < 0) {
            throw new NotifySBUserException("Not below zero, as I told you!");
        }
        this.crewRequirement.put(educationType, amount);
    }

    public void updateCrewRequirement(@Nonnull final EEducationType educationType, final long amount) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        final Long currentAmount = crewRequirement.get(educationType);
        if (currentAmount == null) {
            setAbsoluteCrewRequirement(educationType, amount);
        } else {
            setAbsoluteCrewRequirement(educationType, currentAmount + amount);
        }
    }

    /**
     * Updates the cloned crew amount with the given addition.
     *
     * @param crewToAdd the new crew
     */
    public void updateCrew(@Nonnull final CrewRequirementDTO crewToAdd) {
        Preconditions.checkNotNull(crewToAdd, "crewToAdd shouldn't be null!");

        crewToAdd.crewRequirement.forEach((educationType, amountToAdd) -> {
            final Long currentAmount = this.crewRequirement.get(educationType);
            final long newAmount = currentAmount == null ? amountToAdd : currentAmount + amountToAdd;
            this.setAbsoluteCrewRequirement(educationType, newAmount);
        });
    }

    public long getCrewAmountByType(@Nonnull final EEducationType educationType) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        final Long amount = crewRequirement.get(educationType);
        return amount != null ? amount : 0;
    }

    /**
     * Checks if it is possible to reduce this amount of people in that way.
     *
     * @param reducingBy the amount of people to check
     * @return <code>true</code> if the reduction is possible, <code>false</code> otherwise
     */
    public boolean isReducingPopulationPossible(@Nonnull final CrewRequirementDTO reducingBy) {
        Preconditions.checkNotNull(reducingBy, "reducingBy shouldn't be null!");

        return Arrays.stream(EEducationType.values()).noneMatch(educationType -> {
            final Long currentAmount = this.crewRequirement.get(educationType);
            final long reducingByThisAmount = reducingBy.getCrewAmountByType(educationType);
            if (reducingByThisAmount == 0) {
                // no update needed
                return false;
            }
            if (currentAmount == null) {
                // no update possible
                return true;
            }
            // fine if false
            return currentAmount < reducingByThisAmount;
        });
    }

    /**
     * Updates this by the educated amount of people.
     *
     * @param from    the education level which will be upgraded
     * @param to      the education level which ist the upgrade target
     * @param howMany the amount of people which are educated
     */
    public void educate(@Nullable final EEducationType from, @Nullable final EEducationType to, @Nullable final BigDecimal howMany) {
        if (from == null || to == null || howMany == null) {
            return;
        }
        final long toUpgrade = howMany.longValue();
        final long sumOfPopulationBeforeEducation = getSumOfPopulation();
        final Long fromAmountBefore = crewRequirement.get(from);
        final Long toAmountBefore = crewRequirement.get(to);
        final long newToAmount;
        final long newFromAmount;
        if (fromAmountBefore < toUpgrade) {
            // set all possible people to new level if they are not to fulfil the complete job
            newToAmount = fromAmountBefore + (toAmountBefore != null ? toAmountBefore : 0);
            newFromAmount = 0L;
        } else {
            newToAmount = toUpgrade + (toAmountBefore != null ? toAmountBefore : 0);
            newFromAmount = fromAmountBefore - toUpgrade;
        }
        crewRequirement.put(to, newToAmount);
        crewRequirement.put(from, newFromAmount);
        if (getSumOfPopulation() != sumOfPopulationBeforeEducation) {
            throw new NotifySBUserException("Oh, this should not happen while educating people.");
        }
    }
}
