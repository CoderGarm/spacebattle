package de.yuga.spacebattle.backend.services.caclulator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.services.caches.PlanetaryResourceCache;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit.MATH_CONTEXT_INTEGER;

@Service
public class TickOutputCalculator {

    @Nonnull
    private final ConstructionService constructionService;

    @Nonnull
    private final PopulationControlCalculator populationControlCalculator;

    @Nonnull
    private final PlanetaryResourceCache planetaryResourceCache;

    @Autowired
    public TickOutputCalculator(@Nonnull final ConstructionService constructionService,
                                @Nonnull final PopulationControlCalculator populationControlCalculator,
                                @Nonnull final PlanetaryResourceCache planetaryResourceCache) {
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService must not be empty");
        this.populationControlCalculator = Preconditions.checkNotNull(populationControlCalculator, "populationControlCalculator must not be empty");
        this.planetaryResourceCache = Preconditions.checkNotNull(planetaryResourceCache, "planetaryResourceCache must not be empty");
    }

    /**
     * Calculates the production per tick of this planet for the given resource type.<br>
     * <b>Attention:</b><br> The {@link EResourceType#POPULATION} will not be accepted.<br>
     * Use the {@link PopulationControlCalculator} instead.<br>
     * <br>
     * <b>Calculation rule:</b><br>
     * Collect all producing constructions and sum up their output by level.
     *
     * @param planet       the planet which should be calculated
     * @param resourceType the resource type
     * @return the pure production
     */
    public long getTickOutput(@Nonnull final Planet planet, @Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");
        Preconditions.checkArgument(EResourceType.POPULATION != resourceType, "resourceType shouldn't be population!");

        final Set<Construction> constructionsByResource = constructionService.findConstructionsFor(planet.getId(), resourceType);
        if (!constructionsByResource.isEmpty()) {
            // collecting all possible producing building
            final Map<EProductionCategory, List<Construction>> constructionMap = constructionsByResource.stream()
                    .collect(Collectors.groupingBy(c -> c.getBuilding().getProductionType().getProductionCategory(),
                            Collectors.mapping(Function.identity(), Collectors.toList())));
            // for normal resources only a pure production is possible
            final List<Construction> constructions = constructionMap.get(EProductionCategory.PRODUCE);
            // sum up all the output of the producing buildings
            return TickOutputCalculator.getTickOutput(constructions).longValue();
        }
        return 0L;
    }

    /**
     * There are some strange rules running:<br>
     * <br>
     * All resources except {@link EResourceType#POPULATION} are normal, as always.<br>
     * Applies to the pop:
     * <ul>
     *     <li>the amount of population is directly applied as resource type and ...
     *     <ul>
     *         <li>if it's below zero ... someone dies per tick</li>
     *         <li>if it's above zero, it will be the amount of newborns per tick</li>
     *     </ul>
     *     </li>
     *     <li>the education types represents a transition which is defined by the refinement type</li>
     *     <li><b>except</b> if it's about {@link EEducationType#NONE} ... then it a a transition from the universe to population</li>
     * </ul>
     *
     * @return the income by tick
     */
    @Nonnull
    public ResourceDeposit getTicklyIncome(final int idPlanet) {

        final ResourceDeposit cachedResult = planetaryResourceCache.getTicklyIncome(idPlanet);
        if (cachedResult != null) {
            return cachedResult;
        }

        final ResourceDeposit income = new ResourceDeposit(EDepositType.INCOME);
        final Map<EResourceType, List<Construction>> resourceConstructionsByType = constructionService.findAllConstructionsOnPlanet(idPlanet).stream()
                .collect(Collectors.groupingBy(c -> c.getBuilding().getProductionTarget(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final List<Construction> populationConstruction = resourceConstructionsByType.getOrDefault(EResourceType.POPULATION, new ArrayList<>());
        //noinspection DataFlowIssue
        final Map<ERefinementSequence, List<Construction>> constructionsByRefinementSequence = populationConstruction.stream()
                .filter(c -> c.getBuilding().getProductionType().getProductionCategory() == EProductionCategory.REFINEMENT)
                .collect(Collectors.groupingBy(c -> c.getBuilding().getProductionType().getRefinementSequence(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        for (final EResourceType eResourceType : EResourceType.valuesWithoutPopulation()) {
            final List<Construction> constructions = resourceConstructionsByType.getOrDefault(eResourceType, new ArrayList<>());
            final BigDecimal ticklyIncome = TickOutputCalculator.getTickOutput(constructions);
            income.setAbsoluteResourceValue(eResourceType, ticklyIncome.longValue());
        }

        constructionsByRefinementSequence.forEach((eRefinementSequence, refinementConstructions) -> {
            final EEducationType educationType = eRefinementSequence.getProduct();
            final BigDecimal ticklyIncome = TickOutputCalculator.getTickOutput(refinementConstructions);
            income.setAbsolutePopulation(educationType, ticklyIncome.longValue());
        });

        final long tickOutputForPopulation = populationControlCalculator.getTickOutputForPopulation(idPlanet);
        income.setAbsolutePopulationValue(tickOutputForPopulation);
        income.setAbsolutePopulation(EEducationType.NONE, tickOutputForPopulation);

        planetaryResourceCache.addTicklyIncome(idPlanet, income);
        return income;
    }

    @Async
    public void reloadTicklyIncome(final int idPlanet) {
        planetaryResourceCache.invalideTicklyIncome(idPlanet);
        getTicklyIncome(idPlanet);
    }

    @Nonnull
    public static BigDecimal getTickOutput(@Nonnull final Collection<Construction> constructions) {
        Preconditions.checkNotNull(constructions, "constructions must not be empty");

        if (constructions.isEmpty()) {
            return BigDecimal.ZERO;
        }

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
        if (building.getProductionTarget() == EResourceType.POPULATION && building.getProductionType().getProductionCategory() != EProductionCategory.PRODUCE) {
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
        return adjustedBaseValue.add(adjustedBaseValue.multiply(increasingFactorPerLevel)).multiply(level);
    }

    @Nonnull
    public static BigDecimal getResearchCosts(final long baseCosts, final int targetLevel) {
        return TickOutputCalculator.getOutput(new BigDecimal(baseCosts), BigDecimal.valueOf(0.2), 1, targetLevel);
    }
}
