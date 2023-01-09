package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit.MATH_CONTEXT_INTEGER;
import static de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit.MATH_CONTEXT_MORE_PRECISION;
import static de.yuga.spacebattle.backend.enums.EEducationType.NONE;
import static de.yuga.spacebattle.backend.enums.ERefinementSequence.*;

/**
 * Runs the calculations for a growing and educating population.
 */
public class PopulationControlCalculator {

    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(PopulationControlCalculator.class);

    private static final List<ERefinementSequence> UNRESTRICTED_EDUCATION = List.of(EDUCATION_CIVIL_II, EDUCATION_CIVIL_I);
    private static final List<ERefinementSequence> GUIDED_EDUCATION = List.of(EDUCATION_MILITARY_II, EDUCATION_MILITARY_I, EDUCATION_CIVIL_III);

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
        final long sumOfPopulationUtilization = planet.getResourceUtilization().getCrewRequirement().getSumOfPopulation();
        final long sumOfPopulation = resourceDeposit.getCrewRequirement().getSumOfPopulation() + sumOfPopulationUtilization;
        // collecting all possible producing building
        final Map<EProductionCategory, List<Construction>> constructionMap = getConstructionsMappedByProductionCategory(constructionsByResource);
        final List<Construction> reproductive = constructionMap.computeIfAbsent(EProductionCategory.PRODUCE, k -> new ArrayList<>());
        final List<Construction> capacity = constructionMap.computeIfAbsent(EProductionCategory.CAPACITY, k -> new ArrayList<>());
        final BigDecimal K = TickOutputCalculator.getTickOutput(capacity);
        if (K.compareTo(BigDecimal.ZERO) == 0) {
            // if no housing presents, no more growth
            return 0;
        }
        // for formula compare https://de.wikipedia.org/wiki/Fortpflanzungsstrategie
        // N equals no the total population (ignoring the non-reproductive population)
        // r equals to the maximal birth rate
        // K equals to the capacity
        final double miningFactor = planet.getMiningFactors().getMiningFactorByType(EResourceType.POPULATION);
        final BigDecimal N = getVirtualAmountOfPops(miningFactor, sumOfPopulation);
        // sum up all the output of the producing and capacity buildings
        BigDecimal r = TickOutputCalculator.getTickOutput(reproductive).divide(BigDecimal.valueOf(100), MATH_CONTEXT_MORE_PRECISION);
        if (r.compareTo(BigDecimal.valueOf(0.9)) > 0) {
            // maximum reproduction rate must not succeed 1 and is limited to 90 %
            r = BigDecimal.valueOf(0.9);
        }

        if (N.compareTo(BigDecimal.ZERO) == 0) {
            // no bum no sum
            return 0;
        }
        final BigDecimal increasingFactorByCurrentPopulation = r.multiply(N);
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
    public static BigDecimal getVirtualAmountOfPops(final double miningFactor, final long sumOfPopulation) {
        BigDecimal popModifier = new BigDecimal(miningFactor);
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
        if (newbornChildren > 0) {
            resourceDeposit.updateCrewRequirement(NONE, newbornChildren);
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
        final Map<ERefinementSequence, Long> educationCapacity = constructionsByResource.stream()
                .filter(c -> c.getBuilding().getProductionTarget() == EResourceType.POPULATION)
                .filter(c -> Objects.nonNull(c.getBuilding().getProductionType().getRefinementSequence()))
                .collect(Collectors.groupingBy(c -> c.getBuilding().getProductionType().getRefinementSequence(),
                        Collectors.mapping(c -> TickOutputCalculator.getTickOutput(c).longValue(), Collectors.reducing(0L, Long::sum))));


        final List<EducationAmountDTO> education = new ArrayList<>();
        if (!planet.isDemandPresent()) {
            doUnguidedEducation(planet, education, educationCapacity);
        } else {
            doGuidedEducation(planet, education, educationCapacity);
        }
        education.stream().filter(Objects::nonNull).forEach(dto -> PopulationControlCalculator.educate(planet, dto));
    }

    /**
     * Created an education plan which have a look at the current demand of education levels.
     */
    @VisibleForTesting
    static void doGuidedEducation(@Nonnull final Planet planet,
                                  @Nonnull final List<EducationAmountDTO> education,
                                  @Nonnull final Map<ERefinementSequence, Long> educationCapacity) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(education, "education must not be empty");
        Preconditions.checkNotNull(educationCapacity, "educationCapacity must not be empty");

        final ResourceDeposit deposit = new ResourceDeposit(planet.getResourceDeposit());
        final ResourceDeposit demand = new ResourceDeposit(planet.getResourceDemand());
        UNRESTRICTED_EDUCATION.stream().filter(educationCapacity::containsKey).forEach(r -> setUnrestrictedEducation(education, deposit, demand, r, educationCapacity));
        GUIDED_EDUCATION.stream().filter(educationCapacity::containsKey).forEach(r -> calculateDemandWeightedEducation(education, deposit, demand, r, educationCapacity));
    }

    /**
     * Created an education plan which educated the most out of the possible.<br>
     * The result will not pay attention to the current demand.
     */
    @VisibleForTesting
    static void doUnguidedEducation(@Nonnull final Planet planet,
                                    @Nonnull final List<EducationAmountDTO> education,
                                    @Nonnull final Map<ERefinementSequence, Long> educationCapacity) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(education, "education must not be empty");
        Preconditions.checkNotNull(educationCapacity, "educationCapacity must not be empty");

        final List<EducationAmountDTO> educationAmountDTOs = new ArrayList<>();
        for (final ERefinementSequence refinementSequence : values()) {
            final long cap = educationCapacity.getOrDefault(refinementSequence, 0L);
            if (cap > 0) {
                final long guysPresent = planet.getResourceDeposit().getCrewAmountByType(refinementSequence.getEduct());
                final long pupils = Long.min(cap, guysPresent);
                educationAmountDTOs.add(new EducationAmountDTO(pupils, refinementSequence));
            }
        }
        education.addAll(balanceEducation(planet, educationAmountDTOs));
    }

    private static void setUnrestrictedEducation(@Nonnull final List<EducationAmountDTO> education,
                                                 @Nonnull final ResourceDeposit deposit,
                                                 @Nonnull final ResourceDeposit demand,
                                                 @Nonnull final ERefinementSequence refinementSequence,
                                                 @Nonnull final Map<ERefinementSequence, Long> educationCapacity) {
        Preconditions.checkNotNull(education, "education must not be empty");
        Preconditions.checkNotNull(deposit, "deposit must not be empty");
        Preconditions.checkNotNull(demand, "demand must not be empty");
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");
        Preconditions.checkNotNull(educationCapacity, "educationCapacity must not be empty");

        long pupils = deposit.getCrewAmountByType(refinementSequence.getEduct());
        final long cap = educationCapacity.getOrDefault(refinementSequence, 0L);
        pupils = Long.min(pupils, cap);
        education.add(new EducationAmountDTO(pupils, refinementSequence));
        deposit.updateCrewRequirement(refinementSequence.getEduct(), -pupils);
        demand.updateCrewRequirement(refinementSequence.getProduct(), -pupils);
    }

    private static void calculateDemandWeightedEducation(@Nonnull final List<EducationAmountDTO> education,
                                                         @Nonnull final ResourceDeposit deposit,
                                                         @Nonnull final ResourceDeposit demand,
                                                         @Nonnull final ERefinementSequence refinementSequence,
                                                         @Nonnull final Map<ERefinementSequence, Long> educationCapacity) {
        Preconditions.checkNotNull(education, "education must not be empty");
        Preconditions.checkNotNull(deposit, "deposit must not be empty");
        Preconditions.checkNotNull(demand, "demand must not be empty");
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");
        Preconditions.checkNotNull(educationCapacity, "educationCapacity must not be empty");

        final long productPresent = deposit.getCrewAmountByType(refinementSequence.getProduct());
        final long productNeeded = demand.getCrewAmountByType(refinementSequence.getProduct());

        final long eductPresent = deposit.getCrewAmountByType(refinementSequence.getEduct());
        final long eductNeeded = demand.getCrewAmountByType(refinementSequence.getEduct());

        final long cap = educationCapacity.getOrDefault(refinementSequence, 0L);
        if (productNeeded > productPresent) {
            final long need = productNeeded - productPresent;
            final long free = eductPresent - eductNeeded;
            long educated = Math.min(need, free);
            educated = Math.min(educated, cap);

            deposit.updateCrewRequirement(refinementSequence.getEduct(), -educated);
            demand.updateCrewRequirement(refinementSequence.getProduct(), -educated);
            education.add(new EducationAmountDTO(educated, refinementSequence));
        }
    }

    /**
     * Balances the needs of the planet to reach two goals:
     * 1st: educate every needed group
     * 2nd: leave over some lower education level
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
            long availablePeople = resourceDeposit.getCrewAmountByType(educt);
            if (availablePeople == 0) {
                continue;
            }
            // reduce the available amount to left 20 % of population in old education level
            final BigDecimal available = new BigDecimal(availablePeople);
            availablePeople = available.subtract(available.multiply(BigDecimal.valueOf(0.2), MATH_CONTEXT_MORE_PRECISION)).longValue();

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
            final List<EducationAmountDTO> collect = educationAmountByEduct.get(educt).stream().map(e -> e.reduceAmountBy(modifier)).collect(Collectors.toList());
            result.addAll(collect);
        }
        return result;
    }

    /**
     * Updates this by the educated amount of people.
     *
     * @param educationAmountDTO the education parameters
     */
    @VisibleForTesting
    static void educate(@Nonnull final Planet planet, @Nonnull final EducationAmountDTO educationAmountDTO) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(educationAmountDTO, "educationAmountDTO shouldn't be null!");

        final EEducationType educationSource = educationAmountDTO.getEduct();
        final EEducationType educationGoal = educationAmountDTO.getProduct();
        final ResourceDeposit deposit = planet.getResourceDeposit();
        final ResourceDeposit demand = planet.getResourceDemand();
        // just to check if the calculation went wrong
        final long sumOfPopulationBeforeEducation = deposit.getCrewRequirement().getSumOfPopulation();
        final long toUpgrade = educationAmountDTO.getHowManyPupils();
        final long fromAmountBefore = deposit.getCrewAmountByType(educationSource);
        final long educatedAmount = Long.min(toUpgrade, fromAmountBefore);

        deposit.updateCrewRequirement(educationGoal, educatedAmount);
        deposit.updateCrewRequirement(educationSource, -educatedAmount);
        demand.updateCrewRequirement(educationGoal, -educatedAmount);

        if (deposit.getCrewRequirement().getSumOfPopulation() != sumOfPopulationBeforeEducation) {
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
