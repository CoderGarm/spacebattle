package de.yuga.spacebattle.backend.calculator.colonization;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
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
    @Nonnull
    public static ResourceAmount calculateInformationCost(@Nonnull final StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        final Set<Planet> planets = starSystem.getPlanets(); // todo change to increasing costs by lower amount of unknown planets
        final BigDecimal cost = BigDecimal.TEN.multiply(new BigDecimal(planets.size()));
        return new ResourceAmount(EResourceType.CREDITS, cost.longValue());
    }

    /**
     * Calculates the costs to colonize a planet.
     *
     * @param planet the planet which should be colonized
     * @return the costs
     */
    @Nonnull
    private static ResourceAmount calculateColonizationCost(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final long creditsFactors = planet.getMiningFactors().getMiningFactorByType(EResourceType.CREDITS); // todo change to distance to home planet
        final BigDecimal cost = BigDecimal.TEN.multiply(new BigDecimal(creditsFactors), ResourceDeposit.MATH_CONTEXT_INTEGER);
        return new ResourceAmount(EResourceType.CREDITS, cost.longValue());
    }

    @Nonnull
    private static CrewRequirement getCrewRequirementForColonization() {
        final Map<EEducationType, Long> requiredCrew = new HashMap<>();
        requiredCrew.put(EEducationType.NONE, 200L);
        requiredCrew.put(EEducationType.ENLISTED, 50L);
        requiredCrew.put(EEducationType.OFFICER, 20L);
        requiredCrew.put(EEducationType.SCHOOL, 100L);
        requiredCrew.put(EEducationType.COLLEGE, 200L);
        requiredCrew.put(EEducationType.UNIVERSITY, 500L);
        return new CrewRequirement(requiredCrew, EDepositType.COSTS);
    }

    @Nonnull
    public static ResourceDeposit getColonizationCosts(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final CrewRequirement crewRequirement = ColonizationCostCalculator.getCrewRequirementForColonization();
        final ResourceAmount costs = ColonizationCostCalculator.calculateColonizationCost(planet);
        final ResourceDeposit resourceDeposit = new ResourceDeposit(EDepositType.COSTS);
        resourceDeposit.setAbsoluteResourceValue(costs.getRealResourceType(), costs.getAmount());
        resourceDeposit.setCrewRequirement(crewRequirement);
        return resourceDeposit;
    }
}
