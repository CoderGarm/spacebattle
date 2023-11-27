package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EJobType;
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
        if (targetLevel == 1) {
            return BigDecimal.valueOf(amount);
        }
        return new BigDecimal(amount).add(new BigDecimal(amount).multiply(new BigDecimal(targetLevel).multiply(BigDecimal.valueOf(0.2)), ResourceDeposit.MATH_CONTEXT_INTEGER));
    }

    /**
     * Calculates the tick amount which must be worked on this constructable.<br>
     * Takes all forfeit resources into account.
     *
     * @param resourceDeposit the available
     * @return the amount of ticks
     */
    public static int calculateRemainingTicks(@Nonnull final Job job,
                                              @Nonnull final ResourceDeposit ticklyIncome,
                                              @Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(ticklyIncome, "ticklyIncome must not be empty");
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit must not be empty");

        final ResourceDeposit costs = job.getConstructable().getJobCosts();
        final AtomicInteger ticksNeeded = new AtomicInteger(0);

        final EResourceType resourceType = job.getConstructable().getResourceType();
        costs.getForfeitableResource().stream()
                .filter(rt -> rt == resourceType)
                .forEach(r -> {
                    final long cost = job.getPointsLeft();
                    final long income = ticklyIncome.getResourceAmountByType(r);
                    final long available = resourceDeposit.getResourceAmountByType(r);

                    final int ticks = BigDecimal.valueOf(cost - available).divide(BigDecimal.valueOf(income), RoundingMode.CEILING).intValue();
                    if (ticks > 0 && ticksNeeded.get() < ticks) {
                        ticksNeeded.set(ticks);
                    }
                });
        return ticksNeeded.get();
    }

    public static int calculateRemainingTicks(final long empireWideResearchPoints,
                                              final long empireWideResearchPointsLeftOver,
                                              final long cost) {
        if (empireWideResearchPoints <= 0 && cost > empireWideResearchPointsLeftOver) {
            return 999;
        }

        if (cost <= empireWideResearchPointsLeftOver) {
            return 0;
        }

        //noinspection UnnecessaryLocalVariable
        final int ticks = BigDecimal.valueOf(cost - empireWideResearchPointsLeftOver).divide(BigDecimal.valueOf(empireWideResearchPoints), RoundingMode.CEILING).intValue();
        return ticks;
    }

    public static ResourceDeposit calculateJobCost(@Nonnull final Fleet fleet, @Nonnull final EJobType jobType) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(jobType, "jobType must not be empty");
        final ResourceDeposit costs;
        switch (jobType) {
            case REPAIR:
                return calculateRepairJobCost(fleet);
            case UPGRADE:
                costs = new ResourceDeposit(EDepositType.COSTS);
                for (final ShipClass shipClass : fleet.getAliveShips().stream().map(WarShip::getShipClass).filter(ShipClass::hasSuccessor).collect(Collectors.toSet())) {
                    // add the differences in cost for each successor to the current, already paid design
                    ShipClass successor = shipClass.getSuccessor();
                    while (successor != null) {
                        costs.add(successor.getCosts());
                        costs.subtract(shipClass.getCosts());
                        successor = successor.getSuccessor();
                    }
                }
                return costs;
            default:
            case CONSTRUCTION:
                costs = new ResourceDeposit(EDepositType.COSTS);
                fleet.getAllShips().stream().map(WarShip::getShipClass).map(ShipClass::getCosts).forEach(costs::add);
                return costs;
        }
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
