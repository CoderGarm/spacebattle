package de.yuga.spacebattle.backend.services.caclulator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.EducationAmountDTO;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.OrbitalStructure;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.MiningFactors;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.caches.PlanetaryResourceCache;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.OrbitalStructureService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.rest.api.error.LogInfo;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.turn.resources.PopulationOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
@Service
public class PopulationControlCalculator {

    private static final List<ERefinementSequence> UNRESTRICTED_EDUCATION = List.of(EDUCATION_CIVIL_II, EDUCATION_CIVIL_I);
    private static final List<ERefinementSequence> GUIDED_EDUCATION = List.of(EDUCATION_MILITARY_II, EDUCATION_MILITARY_I, EDUCATION_CIVIL_III);

    @Nonnull
    private final ConstructionService constructionService;

    @Nonnull
    private final OperationalService operationalService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final OrbitalStructureService orbitalStructureService;

    @Nonnull
    private final PlanetaryResourceCache planetaryResourceCache;

    @Autowired
    public PopulationControlCalculator(@Nonnull final ConstructionService constructionService,
                                       @Nonnull final OperationalService operationalService,
                                       @Nonnull final PlanetService planetService,
                                       @Nonnull final OrbitalStructureService orbitalStructureService,
                                       @Nonnull final PlanetaryResourceCache planetaryResourceCache) {
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService must not be empty");
        this.operationalService = Preconditions.checkNotNull(operationalService, "operationalService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.orbitalStructureService = Preconditions.checkNotNull(orbitalStructureService, "orbitalStructureService must not be empty");
        this.planetaryResourceCache = Preconditions.checkNotNull(planetaryResourceCache, "planetaryResourceCache must not be empty");
    }


    @Async
    public void reloadPopOverview(final int idUser) {
        planetaryResourceCache.invalidePopulationOverview(idUser);
        getPopOverview(idUser);
    }

    @Nonnull
    public PopulationOverview getPopOverview(final int idUser) {

        PopulationOverview populationOverview = planetaryResourceCache.getPopulationOverview(idUser);
        if (populationOverview != null) {
            return populationOverview;
        }

        final List<Integer> planetIDs = planetService.findAllColonizedByForIdPlanet(idUser);
        final long utilizedPopulationForUser = operationalService.getUtilizedPopulationForUser(idUser).getResourceAmountByType(de.yuga.spacebattle.backend.enums.EResourceType.POPULATION);
        populationOverview = new PopulationOverview();
        populationOverview.addPresent(utilizedPopulationForUser);

        final Map<Integer, de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit> resourceCapacities =
                planetService.getResourceCapacities(planetIDs);
        for (final Integer idPlanet : planetIDs) {
            final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit resourceDeposit = planetService.findResourceDeposit(idPlanet);
            Preconditions.checkNotNull(resourceDeposit, "resourceDeposit must not be empty");
            final long capacity = resourceCapacities.get(idPlanet).getResourceAmountByType(de.yuga.spacebattle.backend.enums.EResourceType.POPULATION);
            populationOverview.addCapacity(capacity);
            populationOverview.addPresent(resourceDeposit.getCrewRequirement().getSumOfPopulation());
        }

        planetaryResourceCache.addPopulationOverview(idUser, populationOverview);
        return populationOverview;
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
     * @param idPlanet the planet which should be calculated
     */
    public long getTickOutputForPopulation(final int idPlanet) {

        final Set<Construction> constructionsByResource = constructionService.findConstructionsFor(idPlanet, EResourceType.POPULATION);
        final ResourceDeposit resourceDeposit = planetService.findResourceDeposit(idPlanet);
        if (resourceDeposit == null) {
            return 0;
        }
        final ResourceDeposit utilization = operationalService.getUtilizedPopulationForPlanet(idPlanet);
        final long sumOfPopulation = resourceDeposit.getCrewRequirement().getSumOfPopulation() + utilization.getCrewRequirement().getSumOfPopulation();
        final long sumOfReproductivePopulation
                = ((resourceDeposit.getCrewRequirement().getSumOfReproductivePopulation()
                + utilization.getCrewRequirement().getSumOfReproductivePopulation()) / 2)
                - resourceDeposit.getCrewRequirement().getCrewAmountByType(NONE);
        if (sumOfReproductivePopulation <= 0) {
            // no bum no sum
            return 0;
        }
        return calculateNewbornAmount(idPlanet, constructionsByResource, sumOfPopulation, sumOfReproductivePopulation);
    }

    private long calculateNewbornAmount(final int idPlanet, final Set<Construction> constructionsByResource, final long sumOfPopulation, final long sumOfReproductivePopulation) {
        // collecting all possible producing building
        final Map<EProductionCategory, List<Construction>> constructionMap = getConstructionsMappedByProductionCategory(constructionsByResource);
        final List<Construction> capacity = constructionMap.computeIfAbsent(EProductionCategory.CAPACITY, k -> new ArrayList<>());

        final List<OrbitalStructure> orbitalStructures = orbitalStructureService.findByPlanet(idPlanet);

        BigDecimal K = TickOutputCalculator.getTickOutput(capacity);
        K = K.add(new BigDecimal(calculateAdditionalPopulationCapacityFromStructures(orbitalStructures)));

        if (K.compareTo(BigDecimal.ZERO) == 0) {
            // if no housing presents, no more growth
            return 0;
        }
        // for formula compare https://de.wikipedia.org/wiki/Fortpflanzungsstrategie
        // N equals no the total population (ignoring the non-reproductive population)
        // r equals to the maximal birth rate
        // K equals to the capacity
        final MiningFactors miningFactors = planetService.findMiningFactors(idPlanet);


        if (miningFactors == null) {
            return 0;
        }

        final BigDecimal r = BigDecimal.valueOf(miningFactors.getMiningFactorByType(EResourceType.POPULATION) + calculateAdditionalPopulationFactorIncreasement(orbitalStructures));
        final BigDecimal N = BigDecimal.valueOf(sumOfReproductivePopulation);

        final BigDecimal increasingFactorByCurrentPopulation = r.multiply(N);
        final BigDecimal capacityLimitFactor = BigDecimal.ONE.subtract(BigDecimal.valueOf(sumOfPopulation).divide(K, MATH_CONTEXT_MORE_PRECISION));
        // rounding down to long
        return increasingFactorByCurrentPopulation.multiply(capacityLimitFactor, MATH_CONTEXT_INTEGER).longValue();
    }

    public static int calculateAdditionalPopulationFactorIncreasement(@Nonnull final List<OrbitalStructure> orbitalStructures) {
        Preconditions.checkNotNull(orbitalStructures, "orbitalStructures must not be empty");

        return orbitalStructures.stream().map(OrbitalStructure::getModule).map(OrbitalModule::getPopFactorIncreasement).reduce(0, Integer::sum);
    }

    public static int calculateAdditionalPopulationCapacityFromStructures(@Nonnull final List<OrbitalStructure> orbitalStructures) {
        Preconditions.checkNotNull(orbitalStructures, "orbitalStructures must not be empty");

        return orbitalStructures.stream().map(OrbitalStructure::getModule).map(OrbitalModule::getInhabitants).reduce(0, Integer::sum);
    }

    /**
     * Will calculate the amount of newly born children.<br>
     * If the capacity is exceeded some people will die.
     *
     * @param planet the planet to populate
     */
    public void populatePlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        final long newbornChildren = getTickOutputForPopulation(planet.getId());
        if (newbornChildren > 0) {
            resourceDeposit.updateCrewRequirement(NONE, newbornChildren);
        }
    }

    /**
     * Calculates the refinement of the planet's population.
     *
     * @param planet the planet to educate
     */
    public void educatePopulation(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final ResourceDeposit demand = operationalService.getPopulationDemandForPlanet(planet.getId());
        final Set<Construction> constructionsByResource = constructionService.findConstructionsFor(planet.getId(), EResourceType.POPULATION);
        final Map<ERefinementSequence, Long> educationCapacity = constructionsByResource.stream()
                .filter(c -> c.getBuilding().getProductionTarget() == EResourceType.POPULATION)
                .filter(c -> Objects.nonNull(c.getBuilding().getProductionType().getRefinementSequence()))
                .collect(Collectors.groupingBy(c -> c.getBuilding().getProductionType().getRefinementSequence(),
                        Collectors.mapping(c -> TickOutputCalculator.getTickOutput(c).longValue(), Collectors.reducing(0L, Long::sum))));


        final List<EducationAmountDTO> education = new ArrayList<>();
        if (!demand.isDemandPresent()) {
            doUnguidedEducation(planet, education, educationCapacity);
        } else {
            doGuidedEducation(planet, demand, education, educationCapacity);
        }
        education.stream().filter(Objects::nonNull).forEach(dto -> PopulationControlCalculator.educate(planet, dto));
    }

    /**
     * Created an education plan which have a look at the current demand of education levels.
     */
    public static void doGuidedEducation(@Nonnull final Planet planet,
                                         @Nonnull final ResourceDeposit demand,
                                         @Nonnull final List<EducationAmountDTO> education,
                                         @Nonnull final Map<ERefinementSequence, Long> educationCapacity) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(demand, "demand must not be empty");
        Preconditions.checkNotNull(education, "education must not be empty");
        Preconditions.checkNotNull(educationCapacity, "educationCapacity must not be empty");

        final ResourceDeposit deposit = new ResourceDeposit(planet.getResourceDeposit());
        UNRESTRICTED_EDUCATION.stream().filter(educationCapacity::containsKey).forEach(r -> setUnrestrictedEducation(education, deposit, r, educationCapacity));
        GUIDED_EDUCATION.stream().filter(educationCapacity::containsKey).forEach(r -> calculateDemandWeightedEducation(education, deposit, demand, r, educationCapacity));
    }

    /**
     * Created an education plan which educated the most out of the possible.<br>
     * The result will not pay attention to the current demand.
     */
    public static void doUnguidedEducation(@Nonnull final Planet planet,
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
                                                 @Nonnull final ERefinementSequence refinementSequence,
                                                 @Nonnull final Map<ERefinementSequence, Long> educationCapacity) {
        Preconditions.checkNotNull(education, "education must not be empty");
        Preconditions.checkNotNull(deposit, "deposit must not be empty");
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");
        Preconditions.checkNotNull(educationCapacity, "educationCapacity must not be empty");

        long pupils = deposit.getCrewAmountByType(refinementSequence.getEduct());
        final long cap = educationCapacity.getOrDefault(refinementSequence, 0L);
        pupils = Long.min(pupils, cap);
        education.add(new EducationAmountDTO(pupils, refinementSequence));
        deposit.updateCrewRequirement(refinementSequence.getEduct(), -pupils);
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
        final boolean guidedEducationNeeded = productNeeded > productPresent;

        final long eductPresent = deposit.getCrewAmountByType(refinementSequence.getEduct());
        final long eductNeeded = demand.getCrewAmountByType(refinementSequence.getEduct());

        final long cap = educationCapacity.getOrDefault(refinementSequence, 0L);
        if (guidedEducationNeeded) {
            final long need = productNeeded - productPresent;
            final long free = Math.max(0, eductPresent - eductNeeded);
            long educated = Math.min(need, free);
            educated = Math.min(educated, cap);

            deposit.updateCrewRequirement(refinementSequence.getEduct(), -educated);
            education.add(new EducationAmountDTO(educated, refinementSequence));
        } else {
            // todo this quickfix should relax the pop situation - this must be cleared finally by checking that the guided education only avoid a deadlock
            setUnrestrictedEducation(education, deposit, refinementSequence, educationCapacity);
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
    public static void educate(@Nonnull final Planet planet, @Nonnull final EducationAmountDTO educationAmountDTO) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(educationAmountDTO, "educationAmountDTO shouldn't be null!");

        final LogInfo logInfo = new LogInfo(planet);
        final EEducationType educationSource = educationAmountDTO.getEduct();
        final EEducationType educationGoal = educationAmountDTO.getProduct();
        final ResourceDeposit deposit = planet.getResourceDeposit();
        // just to check if the calculation went wrong
        final long sumOfPopulationBeforeEducation = deposit.getCrewRequirement().getSumOfPopulation();
        final long toUpgrade = educationAmountDTO.getHowManyPupils();
        logInfo.appendLN("Education plan: " + toUpgrade + " pupils from " + educationAmountDTO.getEduct() + " to " + educationAmountDTO.getProduct());

        final long eductAmount = deposit.getCrewAmountByType(educationSource);
        final long toEducateAmount = Long.min(toUpgrade, eductAmount);
        logInfo.appendLN("Found " + toEducateAmount + " pupils from educt");

        deposit.updateCrewRequirement(educationGoal, toEducateAmount);
        deposit.updateCrewRequirement(educationSource, -toEducateAmount);

        final long sumOfPopulationAfterEducation = deposit.getCrewRequirement().getSumOfPopulation();
        logInfo.appendLN("Full sum of before: " + sumOfPopulationBeforeEducation + " vs after education: " + sumOfPopulationAfterEducation);
        if (sumOfPopulationAfterEducation != sumOfPopulationBeforeEducation) {
            throw new NotifyWebUserException("Oh, this should not happen while educating people.", logInfo);
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
