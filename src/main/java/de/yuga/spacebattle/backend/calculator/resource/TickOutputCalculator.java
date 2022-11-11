package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit.MATH_CONTEXT_INTEGER;

public class TickOutputCalculator {

    private TickOutputCalculator() {
    }

    @Nonnull
    public static BigDecimal getTickOutput(@Nonnull final Collection<Construction> constructions) {
        Preconditions.checkNotNull(constructions, "constructions must not be empty");

        if (constructions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        final Set<EResourceType> resourceTypes = constructions.stream().map(c -> c.getBuilding().getProductionTarget()).collect(Collectors.toSet());
        Preconditions.checkArgument(resourceTypes.size() == 1, "resourceTypes must contain one element");
        final Set<EProductionCategory> productionCategories = constructions.stream().map(c -> c.getBuilding().getProductionType().getProductionCategory()).collect(Collectors.toSet());
        Preconditions.checkArgument(productionCategories.size() == 1, "productionCategories must contain one element");
        final Set<Planet> planets = constructions.stream().map(Construction::getPlanet).collect(Collectors.toSet());
        Preconditions.checkArgument(planets.size() == 1, "planets must contain one element");

        return constructions.stream().map(TickOutputCalculator::getTickOutput).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Nonnull
    public static BigDecimal getTickOutput(@Nonnull final Construction construction) {
        Preconditions.checkNotNull(construction, "construction must not be empty");

        final Building building = construction.getBuilding();
        final BigDecimal baseValue = BigDecimal.valueOf(building.getBaseValue());
        final BigDecimal increasingFactorPerLevel = building.getIncreasingFactorPerLevel();
        final int constructionLevel = construction.getOperationalLevel();
        double miningFactor = construction.getPlanet().getMiningFactors().getMiningFactorByType(building.getProductionTarget());
        if (EResourceType.POPULATION == building.getProductionTarget() && EProductionCategory.CAPACITY != building.getProductionType().getProductionCategory()) {
            miningFactor = 1;
        }
        // the mining factor of the population affects only the capacity, not the production directly
        return getOutput(baseValue, increasingFactorPerLevel, miningFactor, constructionLevel);
    }

    @Nonnull
    public static BigDecimal getOutput(@Nonnull final BigDecimal baseValue,
                                       @Nonnull final BigDecimal increasingFactorPerLevel,
                                       final double miningFactor,
                                       final int constructionLevel) {
        Preconditions.checkNotNull(baseValue, "baseValue must not be empty");
        Preconditions.checkNotNull(increasingFactorPerLevel, "increasingFactorPerLevel must not be empty");

        final BigDecimal adjustedBaseValue = baseValue.multiply(new BigDecimal(miningFactor), MATH_CONTEXT_INTEGER);
        final BigDecimal level = BigDecimal.valueOf(constructionLevel);
        if (constructionLevel == 1) {
            return adjustedBaseValue;
        }
        return adjustedBaseValue.add(adjustedBaseValue.multiply(increasingFactorPerLevel).multiply(level));
    }
}
