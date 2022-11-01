package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;
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
        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        final long sumOfPopulation = resourceDeposit.getCrewRequirement().getSumOfPopulation();
        // collecting all possible producing building
        final Map<EProductionCategory, List<Construction>> constructionMap = getConstructionsMappedByProductionCategory(constructionsByResource);
        final List<Construction> reproductive = constructionMap.computeIfAbsent(EProductionCategory.PRODUCE, k -> new ArrayList<>());
        final List<Construction> capacity = constructionMap.computeIfAbsent(EProductionCategory.CAPACITY, k -> new ArrayList<>());
        // for formula compare https://de.wikipedia.org/wiki/Fortpflanzungsstrategie
        // N equals no the total population (ignoring the non-reproductive population)
        // r equals to the maximal birth rate
        // K equals to the capacity
        final long miningFactor = planet.getMiningFactors().getMiningFactorByType(EResourceType.POPULATION);
        final BigDecimal N = getVirtualAmountOfPops(miningFactor, sumOfPopulation);
        // sum up all the output of the producing and capacity buildings
        BigDecimal r = reproductive.stream().map(TickOutputCalculator::getTickOutputByLevelForPopulation).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (r.compareTo(BigDecimal.valueOf(0.9)) > 0) {
            // maximum reproduction rate must not succeed 1 and is limited to 90 %
            r = BigDecimal.valueOf(0.9);
        }

        final BigDecimal K = capacity.stream()
                .map(TickOutputCalculator::getTickOutputByLevelForPopulation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (N.compareTo(BigDecimal.ZERO) == 0) {
            // no bum no sum
            return 0;
        }
        final BigDecimal increasingFactorByCurrentPopulation = r.multiply(N);
        if (K.compareTo(BigDecimal.ZERO) == 0) {
            // if no housing presents, a lot of people must die
            return increasingFactorByCurrentPopulation.abs().negate().multiply(N, MATH_CONTEXT_INTEGER).longValue();
        }
        final BigDecimal capacityLimitFactor = BigDecimal.ONE.subtract(N.divide(K, MATH_CONTEXT_MORE_PRECISION));
        // rounding down to long
        return increasingFactorByCurrentPopulation.multiply(capacityLimitFactor, MATH_CONTEXT_INTEGER).longValue();
    }

    /**
     * Calculates the virtual amount of people on a planet.<br>
     * The virtual amount is the real amount modified about the possibility of a planet to hold human life.
     *
     * @param miningFactor    the population mining factor
     * @param sumOfPopulation the real amount of people on a planet
     * @return the virtual amount
     */
    @Nonnull
    public static BigDecimal getVirtualAmountOfPops(final long miningFactor, final long sumOfPopulation) {
        BigDecimal popModifier = new BigDecimal(miningFactor).divide(BigDecimal.valueOf(100), MATH_CONTEXT_MORE_PRECISION);
        final int compareTo = popModifier.compareTo(BigDecimal.ONE);
        if (compareTo < 0) {
            // bei 0,7 -> 1,3
            popModifier = BigDecimal.ONE.add(BigDecimal.ONE.subtract(popModifier));
        } else if (compareTo > 0) {
            // bei 1,8 -> 0,8
            popModifier = popModifier.subtract(BigDecimal.ONE);
        }
        return new BigDecimal(sumOfPopulation).multiply(popModifier);
    }

    /**
     * Will calculate the amount of newly born children.<br>
     * If the capacity is exceeded some people will die.
     *
     * @param planet the planet to populate
     */
    public static void populatePlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        final long newbornChildren = getTickOutputForPopulation(planet);
        final long currentNewbornChildren = resourceDeposit.getCrewAmountByType(EEducationType.NONE);
        final long absoluteAmountOfDyingPeople = currentNewbornChildren + newbornChildren;
        if (absoluteAmountOfDyingPeople <= 0) {
            // if capacity is exceeded some newborn will die, sadly but true
            resourceDeposit.setAbsolutePopulation(EEducationType.NONE, 0);
            final List<EEducationType> educationTypes = new ArrayList<>(Arrays.asList(EEducationType.values()));
            educationTypes.remove(EEducationType.NONE);
            final long dyingPeoplePerType = Math.abs(absoluteAmountOfDyingPeople / educationTypes.size());
            educationTypes.forEach(educationType -> {
                // as well as the other people
                final long crewAmountByType = resourceDeposit.getCrewAmountByType(educationType);
                if (crewAmountByType - dyingPeoplePerType <= 0) {
                    resourceDeposit.setAbsolutePopulation(educationType, 0);
                } else {
                    resourceDeposit.updateCrewRequirement(educationType, -1 * dyingPeoplePerType);
                }
            });
        } else {
            resourceDeposit.updateCrewRequirement(EEducationType.NONE, newbornChildren);
        }
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
        // collecting all possible producing building
        final Map<EProductionCategory, List<Construction>> constructionMap = getConstructionsMappedByProductionCategory(constructionsByResource);
        final List<Construction> constructionsRefinement = constructionMap.computeIfAbsent(EProductionCategory.REFINEMENT, k -> new ArrayList<>());
        final List<EducationAmountDTO> educationAmountDTOs = new ArrayList<>();
        for (final Construction c : constructionsRefinement) {
            final ProductionType productionType = c.getBuilding().getProductionType();
            final ERefinementSequence refinementSequence = productionType.getRefinementSequence();
            if (refinementSequence == null) {
                continue;
            }
            final EEducationType educt = refinementSequence.getEduct();
            final EEducationType product = refinementSequence.getProduct();
            final BigDecimal educationCapacity = TickOutputCalculator.getTickOutputByLevelForPopulation(c);
            educationAmountDTOs.add(new EducationAmountDTO(educationCapacity.longValue(), educt, product));
        }
        final List<EducationAmountDTO> balancedEducation = balanceEducation(planet, educationAmountDTOs);
        balancedEducation.forEach(dto -> PopulationControlCalculator.educate(planet, dto));
    }

    /**
     * Balances the needs of the planet to reach two goals:
     * 1st: educate every needed group
     * 2nd: leave over some of the lower education level
     *
     * @param educationAmountDTOs the parameters
     * @return the balanced DTOs
     */
    private static List<EducationAmountDTO> balanceEducation(@Nonnull final Planet planet, @Nonnull final List<EducationAmountDTO> educationAmountDTOs) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(educationAmountDTOs, "educationAmountDTOs shouldn't be null!");

        final List<EducationAmountDTO> result = new ArrayList<>();
        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        final Map<EEducationType, Set<EducationAmountDTO>> educationAmountByEduct = educationAmountDTOs.stream()
                .collect(Collectors.groupingBy(EducationAmountDTO::getEduct, Collectors.mapping(e -> e, Collectors.toSet())));

        final Map<EEducationType, BigDecimal> modifierMap = new HashMap<>();
        for (final EEducationType educt : educationAmountByEduct.keySet()) {
            final long availablePeople = resourceDeposit.getCrewAmountByType(educt);
            final Set<EducationAmountDTO> hasSameEduct = educationAmountByEduct.get(educt);
            final long completeNeed = hasSameEduct.stream().map(EducationAmountDTO::getHowManyPupils).reduce(0L, Long::sum);
            if (completeNeed < availablePeople) {
                // everything is fine - more people are present then needed
                result.addAll(hasSameEduct);
                continue;
            }
            final long absoluteDifference = completeNeed - availablePeople;
            // example: trying to educate 10 people but only 8 are present: proportional value is 0.2 (or 20 %)
            // so it is necessary to reduce the amount of all education-requests by at least 20 %
            final BigDecimal fractionOfDifferenceToCompleteNeed = new BigDecimal(absoluteDifference).divide(new BigDecimal(completeNeed), MATH_CONTEXT_MORE_PRECISION);
            // put 10 % on top to leave some people in their old education level
            final BigDecimal resultingModifier = fractionOfDifferenceToCompleteNeed.multiply(BigDecimal.TEN.movePointLeft(2));
            modifierMap.put(educt, resultingModifier);
        }
        // add modified results
        for (final EEducationType educt : modifierMap.keySet()) {
            final BigDecimal modifier = modifierMap.get(educt);
            educationAmountByEduct.get(educt).forEach(e -> e.reduceAmountBy(modifier));
            result.addAll(educationAmountByEduct.get(educt));
        }
        return result;
    }

    /**
     * Updates this by the educated amount of people.
     *
     * @param educationAmountDTO the education parameters
     */
    private static void educate(@Nonnull final Planet planet, @Nonnull final EducationAmountDTO educationAmountDTO) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(educationAmountDTO, "educationAmountDTO shouldn't be null!");

        final EEducationType from = educationAmountDTO.getEduct();
        final EEducationType to = educationAmountDTO.getProduct();
        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        // just to check if the calculation went wrong
        final long sumOfPopulationBeforeEducation = resourceDeposit.getCrewRequirement().getSumOfPopulation();
        final long toUpgrade = educationAmountDTO.getHowManyPupils();
        final long fromAmountBefore = resourceDeposit.getCrewAmountByType(from);
        final long toAmountBefore = resourceDeposit.getCrewAmountByType(to);
        final long newToAmount;
        final long newFromAmount;
        if (fromAmountBefore < toUpgrade) {
            // set all possible people to new level if they are not to fulfil the complete job
            newToAmount = fromAmountBefore + toAmountBefore;
            newFromAmount = 0L;
        } else {
            newToAmount = toUpgrade + toAmountBefore;
            newFromAmount = fromAmountBefore - toUpgrade;
        }
        resourceDeposit.setAbsolutePopulation(to, newToAmount);
        resourceDeposit.setAbsolutePopulation(from, newFromAmount);
        if (resourceDeposit.getCrewRequirement().getSumOfPopulation() != sumOfPopulationBeforeEducation) {
            throw new NotifyWebUserException("Oh, this should not happen while educating people.");
        }
    }

    /**
     * Simply maps all constructions by their {@link EProductionCategory}.
     *
     * @param constructionsByResource the constructions to map
     * @return the mapped constructions
     */
    public static Map<EProductionCategory, List<Construction>> getConstructionsMappedByProductionCategory(Set<Construction> constructionsByResource) {
        return constructionsByResource.stream()
                .collect(Collectors.groupingBy(c -> c.getBuilding().getProductionType().getProductionCategory(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));
    }
}
