package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.turn.Constructable;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

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
        final CrewRequirement crewRequirement = costs.getCrewRequirement();
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

    /**
     * Calculates the tick amount which must be worked on this constructable.<br>
     * Takes all forfeit resources into account.
     *
     * @param facility      the facility which will produce it
     * @param constructable what will be produced
     * @return the amount of ticks
     */
    public static int calculateRemainingTicks(@Nonnull final Construction facility, @Nonnull final Constructable constructable) {
        Preconditions.checkNotNull(facility, "facility shouldn't be null!");
        Preconditions.checkNotNull(constructable, "constructable shouldn't be null!");

        final ResourceDeposit costs = constructable.getJobCosts();
        final AtomicInteger ticksNeeded = new AtomicInteger(0);
        final ResourceDeposit ticklyIncome = facility.getPlanet().getTicklyIncome();

        costs.getForfeitableResource().forEach(r -> {
            final long cost = costs.getResourceAmountByType(r);
            final long income = ticklyIncome.getResourceAmountByType(r);

            final int ticks = BigDecimal.valueOf(cost).divide(BigDecimal.valueOf(income), DistanceCalculator.MC).intValue();
            if (ticksNeeded.get() < ticks) {
                ticksNeeded.set(ticks);
            }
        });
        return ticksNeeded.get();
    }
}
