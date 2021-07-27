package de.yuga.spacebattle.backend.calculator.colonization;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Runs calculations for the colonization topic.
 */
public class ColonizationCostCalculator {

    private ColonizationCostCalculator() {
    }

    /**
     * Calculates the costs to buy information about a system in order to colonize it.
     *
     * @param starSystem the system which information should bought
     * @return the costs
     */
    public static ResourceAmount calculateInformationCost(@Nonnull final StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        final Set<Planet> planets = starSystem.getPlanets();
        final BigDecimal cost = BigDecimal.TEN.multiply(new BigDecimal(planets.size()));
        return new ResourceAmount(EResourceType.CREDITS, cost.longValue());
    }

    /**
     * Calculates the costs to colonize a planet.
     *
     * @param planet the planet which should be colonized
     * @return the costs
     */
    public static ResourceAmount calculateColonizationCost(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final long creditsFactors = planet.getMiningFactors().getResourceAmountByType(EResourceType.CREDITS);
        final BigDecimal cost = BigDecimal.TEN.multiply(new BigDecimal(creditsFactors), ResourceDeposit.MATH_CONTEXT_INTEGER);
        return new ResourceAmount(EResourceType.CREDITS, cost.longValue());
    }
}
