package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit.MATH_CONTEXT_INTEGER;
import static de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit.MATH_CONTEXT_MORE_PRECISION;

/**
 * Runs the calculations for a growing and educating population.
 */
public class PopulationControlCalculator {

    private PopulationControlCalculator() {
    }

    /**
     * Calculates the production per tick of this planet for the given resource type.<br>
     * Will add the pure production of {@link EEducationType#NONE} to the planet's resources.<br>
     * <b>Attention:</b> Only {@link EResourceType#POPULATION} will be accepted.<br>
     * For other {@link EResourceType} use the {@link PopulationControlCalculator} instead.<br>
     * <br>
     * <b>Calculation rule:</b><br>
     * Collect all producing constructions and sum up their output by level -> producing {@link EEducationType#NONE}.<br>
     * Amount of new people will be calculated by the r/K selection theory of the special field of biology
     *
     * @param planet the planet which should be calculated
     */
    public static long getTickOutputForPopulation(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final Set<Construction> constructionsByResource = planet.getConstructionByResource(EResourceType.POPULATION);
        if (constructionsByResource.isEmpty()) {
            // nothing to do
            return 0;
        }

        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        final long sumOfPopulation = resourceDeposit.getCrewRequirement().getSumOfPopulation();
        // collecting all possible producing building
        final Map<EProductionCategory, List<Construction>> constructionMap = getConstructionsMappedByProductionCategory(constructionsByResource);
        final List<Construction> constructionsProducing = constructionMap.computeIfAbsent(EProductionCategory.PRODUCE, k -> new ArrayList<>());
        final List<Construction> constructionsCapacity = constructionMap.computeIfAbsent(EProductionCategory.CAPACITY, k -> new ArrayList<>());
        // for formula compare https://de.wikipedia.org/wiki/Fortpflanzungsstrategie
        // N equals no the total population (ignoring the non-reproductive population)
        // r equals to the maximal birth rate
        // K equals to the capacity
        final BigDecimal N = new BigDecimal(sumOfPopulation);
        // sum up all the output of the producing and capacity buildings
        final BigDecimal r = constructionsProducing.stream().map(TickOutputCalculator::getTickOutputByLevelForPopulation).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (r.compareTo(BigDecimal.ONE) > 0) {
            // todo how to make sure that r is below 1?
            throw new NotifySBUserException("chef, you have to repair that!");
        }

        final BigDecimal K = constructionsCapacity.stream().map(TickOutputCalculator::getTickOutputByLevelForPopulation).reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal increasingFactorByCurrentPopulation = r.multiply(N);
        final BigDecimal capacityLimitFactor = BigDecimal.ONE.subtract(N.divide(K, MATH_CONTEXT_MORE_PRECISION));
        // rounding down to long
        return increasingFactorByCurrentPopulation.multiply(capacityLimitFactor, MATH_CONTEXT_INTEGER).longValue();
    }

    public static void populatePlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        final long newbornChildren = getTickOutputForPopulation(planet);
        // todo make sure that the population capacity will not be overridden
        resourceDeposit.getCrewRequirement().updateCrewRequirement(EEducationType.NONE, newbornChildren);
    }

    /**
     * Calculates the refinement of the planet's population.
     *
     * @param planet the planet to educate
     */
    public static void educatePopulation(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final Set<Construction> constructionsByResource = planet.getConstructionByResource(EResourceType.POPULATION);
        if (constructionsByResource.isEmpty()) {
            // nothing to do
            return;
        }
        final CrewRequirementDTO currentPopulation = planet.getResourceDeposit().getCrewRequirement();
        // collecting all possible producing building
        final Map<EProductionCategory, List<Construction>> constructionMap = getConstructionsMappedByProductionCategory(constructionsByResource);
        final List<Construction> constructionsRefinement = constructionMap.computeIfAbsent(EProductionCategory.REFINEMENT, k -> new ArrayList<>());
        for (final Construction c : constructionsRefinement) {
            final ProductionType productionType = c.getBuilding().getProductionType();
            final ERefinementSequence refinementSequence = productionType.getRefinementSequence();
            if (refinementSequence == null) {
                continue;
            }
            final EEducationType educt = refinementSequence.getEduct();
            final EEducationType product = refinementSequence.getProduct();
            final BigDecimal educationCapacity = TickOutputCalculator.getTickOutputByLevelForPopulation(c);
            // currently the sequence of constructions defines which education job will be fulfilled if there are more than one which needs the same educt
            currentPopulation.educate(educt, product, educationCapacity);
        }
    }

    /**
     * Simply maps all constructions by their {@link EProductionCategory}.
     *
     * @param constructionsByResource the constructions to map
     * @return the mapped constructions
     */
    private static Map<EProductionCategory, List<Construction>> getConstructionsMappedByProductionCategory(Set<Construction> constructionsByResource) {
        return constructionsByResource.stream()
                .collect(Collectors.groupingBy(c -> c.getBuilding().getProductionType().getProductionCategory(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));
    }
}
