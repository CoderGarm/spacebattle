package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit.MATH_CONTEXT_INTEGER;
import static de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit.MATH_CONTEXT_MORE_PRECISION;

public class TickOutputCalculator {

    private TickOutputCalculator() {
    }

    /**
     * Convenience delegator for {@link #getTickOutputByLevel(Planet, Building, int)}.
     *
     * @param construction the construction to calculate
     * @return the tickly production
     */
    @Nonnull
    public static Long getTickOutputByLevel(@Nonnull final Construction construction) {
        Preconditions.checkNotNull(construction, "construction shouldn't be null!");

        final Planet planet = construction.getPlanet();
        final Building building = construction.getBuilding();
        final int level = construction.getLevel();
        return getTickOutputByLevel(planet, building, level);
    }

    /**
     * Calculates the tickly output of this construction.<br>
     * <b>Attention:</b> only for non-{@link EResourceType#POPULATION}.
     *
     * @return the tickly production
     */
    @Nonnull
    public static Long getTickOutputByLevel(@Nonnull final Planet planet,
                                            @Nonnull final Building building,
                                            final int level) {
        Preconditions.checkNotNull(building, "building shouldn't be null!");
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkArgument(EResourceType.POPULATION != building.getProductionTarget(), " shouldn't be population!");

        final EResourceType productionTarget = building.getProductionTarget();
        // calculate level-based increasing factor
        final BigDecimal increasingFactorPerLevel = building.getIncreasingFactorPerLevel();
        BigDecimal increasingFactorAtLevel = BigDecimal.ZERO;
        if (level != 1) {
            increasingFactorAtLevel = increasingFactorPerLevel.multiply(new BigDecimal(level));
        }
        final BigDecimal absoluteIncreasingFactor = BigDecimal.ONE.add(increasingFactorAtLevel);
        // calculate absolute output of construction
        final int baseValue = building.getBaseValue();
        final BigDecimal absoluteOutputAtLevel = new BigDecimal(baseValue).multiply(absoluteIncreasingFactor);
        // calculate planetary mining factor
        final long miningFactor = planet.getMiningFactors().getResourceAmountByType(productionTarget);
        final BigDecimal miningFactorAsPercent = BigDecimal.ONE.add(new BigDecimal(miningFactor).divide(BigDecimal.TEN.movePointRight(1), MATH_CONTEXT_INTEGER));
        // calculate absolute output of planet
        return absoluteOutputAtLevel.multiply(miningFactorAsPercent, MATH_CONTEXT_INTEGER).longValue();
    }

    /**
     * Convenience delegator for {@link #getTickOutputByLevel(Planet, Building, int)}.
     *
     * @param construction the construction to calculate
     * @return the tickly production
     */
    @Nonnull
    public static BigDecimal getTickOutputByLevelForPopulation(@Nonnull final Construction construction) {
        Preconditions.checkNotNull(construction, "construction shouldn't be null!");

        final Planet planet = construction.getPlanet();
        final Building building = construction.getBuilding();
        final int level = construction.getLevel();
        return getTickOutputByLevelForPopulation(planet, building, level);
    }

    /**
     * Calculates the tickly output of this construction.<br>
     * <b>Attention:</b> only for {@link EResourceType#POPULATION}.
     *
     * @return the tickly production
     */
    @Nonnull
    public static BigDecimal getTickOutputByLevelForPopulation(@Nonnull final Planet planet,
                                                               @Nonnull final Building building,
                                                               final int level) {
        Preconditions.checkNotNull(building, "building shouldn't be null!");
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkArgument(EResourceType.POPULATION == building.getProductionTarget(), " must be population!");

        // calculate level-based increasing factor
        final BigDecimal increasingFactorPerLevel = building.getIncreasingFactorPerLevel();
        BigDecimal increasingFactorAtLevel = BigDecimal.ZERO;
        if (level != 1) {
            increasingFactorAtLevel = increasingFactorPerLevel.multiply(new BigDecimal(level));
        }
        final BigDecimal absoluteIncreasingFactor = BigDecimal.ONE.add(increasingFactorAtLevel);
        // calculate absolute output of construction
        final BigDecimal baseValue = new BigDecimal(building.getBaseValue());
        final int divisor = building.getProductionType().getProductionCategory().getDivisor();
        final BigDecimal absoluteOutputAtLevel = baseValue.divide(new BigDecimal(divisor), MATH_CONTEXT_MORE_PRECISION).multiply(absoluteIncreasingFactor);
        // calculate planetary mining factor
        final long miningFactor = planet.getMiningFactors().getResourceAmountByType(EResourceType.POPULATION);
        final BigDecimal miningFactorAsPercent = BigDecimal.ONE.add(new BigDecimal(miningFactor).divide(BigDecimal.TEN.movePointRight(1), MATH_CONTEXT_MORE_PRECISION));
        // calculate absolute output of planet
        return absoluteOutputAtLevel.multiply(miningFactorAsPercent);
    }
}
