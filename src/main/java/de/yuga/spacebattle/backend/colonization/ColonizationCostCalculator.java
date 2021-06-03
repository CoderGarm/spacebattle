package de.yuga.spacebattle.backend.colonization;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.misc.details.EResourceAmountDTO;

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
    public static EResourceAmountDTO calculateInformationCost(@Nonnull final StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        final Set<Planet> planets = starSystem.getPlanets();
        final BigDecimal cost = BigDecimal.TEN.multiply(new BigDecimal(planets.size()));
        return new EResourceAmountDTO(EResourceType.CREDITS, cost, null);
    }

    /**
     * Calculates the costs to colonize a planet.
     *
     * @param planet the planet which should be colonized
     * @return the costs
     */
    public static EResourceAmountDTO calculateColonizationCost(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final BigDecimal creditsFactors = planet.getResourceFactors().getResourceAmountByType(EResourceType.CREDITS);
        final BigDecimal cost = BigDecimal.TEN.multiply(creditsFactors);
        return new EResourceAmountDTO(EResourceType.CREDITS, cost, null);
    }
}
