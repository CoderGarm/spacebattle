package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Constructable;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        final ResourceDeposit resources = new ResourceDeposit(EDepositType.COSTS);
        final CrewRequirement crewRequirement = costs.getCrewRequirement();
        for (final EResourceType resourceType : EResourceType.valuesWithoutPopulation()) {
            final long amount = costs.getResourceAmountByType(resourceType);
            if (amount == 0) {
                continue;
            }
            final BigDecimal multiply = getLevelCosts(targetLevel, amount);
            resources.setAbsoluteResourceValue(resourceType, multiply.longValue());
        }
        for (final EEducationType educationType : EEducationType.WORKFORCE) {
            final long amount = crewRequirement.getCrewAmountByType(educationType);
            if (amount == 0) {
                continue;
            }
            final BigDecimal multiply = getLevelCosts(targetLevel, amount);
            resources.setAbsolutePopulation(educationType, multiply.longValue());
        }
        return resources;
    }

    @Nonnull
    private static BigDecimal getLevelCosts(final int targetLevel, final long amount) {
        return new BigDecimal(amount).add(new BigDecimal(amount).multiply(new BigDecimal(targetLevel).multiply(BigDecimal.valueOf(0.2)), ResourceDeposit.MATH_CONTEXT_INTEGER));
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
        final AtomicInteger ticksNeeded = new AtomicInteger(1);
        final ResourceDeposit ticklyIncome = facility.getPlanet().getTicklyIncome();

        costs.getForfeitableResource().forEach(r -> {
            final long cost = costs.getResourceAmountByType(r);
            final long income = ticklyIncome.getResourceAmountByType(r);

            final int ticks = BigDecimal.valueOf(cost).divide(BigDecimal.valueOf(income), RoundingMode.CEILING).intValue();
            if (ticks > 0 && ticksNeeded.get() < ticks) {
                ticksNeeded.set(ticks);
            }
        });
        return ticksNeeded.get();
    }

    public static int calculateRemainingTicks(@Nonnull final BigDecimal empireWideResearchPoints,
                                              @Nonnull final Constructable constructable) {
        Preconditions.checkNotNull(empireWideResearchPoints, "empireWideResearchPoints must not be empty");
        Preconditions.checkNotNull(constructable, "constructable must not be empty");

        if (empireWideResearchPoints.compareTo(BigDecimal.ZERO) <= 0) {
            return 999;
        }

        final long cost = constructable.getJobCosts().getResourceAmountByType(EResourceType.RESEARCH);

        //noinspection UnnecessaryLocalVariable
        final int ticks = BigDecimal.valueOf(cost).divide(empireWideResearchPoints, RoundingMode.CEILING).intValue();
        return ticks;
    }

    public static ResourceDeposit calculateJobCost(@Nonnull final Fleet fleet, final boolean isRepairJob) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        if (isRepairJob) {
            return calculateRepairJobCost(fleet);
        }
        final ResourceDeposit costs = new ResourceDeposit(EDepositType.COSTS);
        fleet.getAllShips().stream().map(WarShip::getShipClass).map(ShipClass::getCosts).forEach(costs::add);
        return costs;
    }


    private static ResourceDeposit calculateRepairJobCost(@Nonnull final Fleet toRepair) {
        Preconditions.checkNotNull(toRepair, "toRepair must not be empty");

        final Map<WarShip, WarshipHealthState> referenceWarships = toRepair.getAliveShips().stream()
                .collect(Collectors.toMap(Function.identity(), WarshipHealthState::new));

        final Map<WarShip, WarshipHealthState> damagedStates = toRepair.getAliveShips().stream()
                .collect(Collectors.toMap(Function.identity(), WarshipHealthState::new));

        final ResourceDeposit costs = new ResourceDeposit(EDepositType.COSTS);
        referenceWarships.forEach((warShip, reference) -> { // todo this seems to make no sense
            final WarshipHealthState warshipHealthState = damagedStates.get(warShip);
            final double damageFraction = warshipHealthState.getDamagedFraction(reference);
            final ResourceDeposit costsOverall = warShip.getShipClass().getCosts();
            addReducedDeposit(costs, costsOverall, damageFraction);
        });
        return costs;
    }

    private static void addReducedDeposit(@Nonnull final ResourceDeposit deposit,
                                          @Nonnull final ResourceDeposit toAdd,
                                          final double portionFactor) {
        Preconditions.checkNotNull(deposit, "deposit must not be empty");
        Preconditions.checkNotNull(toAdd, "toAdd must not be empty");

        Arrays.stream(EResourceType.valuesWithoutPopulation()).forEach(r -> {
            final long toAddAmount = toAdd.getResourceAmountByType(r);
            deposit.updateResource(r, (long) (portionFactor * (double) toAddAmount));
        });
    }
}
