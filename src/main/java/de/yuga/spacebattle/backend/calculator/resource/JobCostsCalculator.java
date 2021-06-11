package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * Calculator for the job relates costs stuff.
 */
public class JobCostsCalculator {

    private JobCostsCalculator() {
    }

    /**
     * Calculates the full costs by the given target level.
     *
     * @param costs       the base costs
     * @param targetLevel the target level
     * @return the costs for the target level
     */
    @Nonnull
    public static ResourceDeposit getCostsForLevel(@Nonnull final ResourceDeposit costs,
                                                   final int targetLevel) {
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");

        final ResourceDeposit resources = new ResourceDeposit();
        final CrewRequirementDTO crewRequirement = costs.getCrewRequirement();
        resources.setSubType(EDepositType.COSTS);
        for (final EResourceType resourceType : EResourceType.valuesWithoutPopulation()) {
            final long resourceAmountByType = costs.getResourceAmountByType(resourceType);
            final BigDecimal multiply;
            multiply = new BigDecimal(resourceAmountByType).multiply(new BigDecimal(targetLevel), ResourceDeposit.MATH_CONTEXT_INTEGER);
            resources.setAbsoluteResourceValue(resourceType, multiply.longValue());
        }
        for (final EEducationType educationType : EEducationType.valuesOfWorkforce()) {
            final long amount = crewRequirement.getCrewAmountByType(educationType);
            if (amount == 0) {
                continue;
            }
            final BigDecimal multiply = new BigDecimal(amount).multiply(new BigDecimal(targetLevel), ResourceDeposit.MATH_CONTEXT_INTEGER);
            resources.setAbsolutePopulation(educationType, multiply.longValue());
        }
        return resources;
    }
}
