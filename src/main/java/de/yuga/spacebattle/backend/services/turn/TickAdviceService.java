package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import de.yuga.spacebattle.rest.dto.turn.TickAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Service
public class TickAdviceService {

    @Nonnull
    private final ConstructionService constructionService;

    @Nonnull
    private final BuildingService buildingService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final ShipClassService shipClassService;

    @Autowired
    public TickAdviceService(@Nonnull final ConstructionService constructionService,
                             @Nonnull final BuildingService buildingService,
                             @Nonnull final ResearchService researchService,
                             @Nonnull final JobService jobService,
                             @Nonnull final ShipClassService shipClassService) {
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService must not be empty");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService must not be empty");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService must not be empty");
        this.jobService = Preconditions.checkNotNull(jobService, "jobService must not be empty");
        this.shipClassService = Preconditions.checkNotNull(shipClassService, "shipClassService must not be empty");
    }

    @Nonnull
    public TickAdvice getConstructionAdvice(final int idUser, @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        final TickAdvice tickAdvice = new TickAdvice();

        final List<Construction> constructions = constructionService.findAllConstructionsForUser(idUser);

        setConstructionPossibleAdvice(constructions, tickAdvice);

        /* last comes, first serve */
        setRefinementAdvisory(constructions, ERefinementSequence.EDUCATION_MILITARY_II, tickAdvice, preferredLanguage);
        setRefinementAdvisory(constructions, ERefinementSequence.EDUCATION_MILITARY_I, tickAdvice, preferredLanguage);
        setRefinementAdvisory(constructions, ERefinementSequence.EDUCATION_CIVIL_III, tickAdvice, preferredLanguage);

        setRefinementAdvisory(constructions, ERefinementSequence.EDUCATION_CIVIL_II, tickAdvice, preferredLanguage);
        setRefinementAdvisory(constructions, ERefinementSequence.EDUCATION_CIVIL_I, tickAdvice, preferredLanguage);
        setResearchesToShipyardAdvice(idUser, constructions, tickAdvice, preferredLanguage);

        return tickAdvice;
    }


    private void setRefinementAdvisory(@Nonnull final List<Construction> constructions,
                                       @Nonnull final ERefinementSequence refinementSequence,
                                       @Nonnull final TickAdvice tickAdvice,
                                       @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(constructions, "constructions must not be empty");
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        if (constructions.stream().noneMatch(c -> c.getBuilding().getProductionType().getRefinementSequence() == refinementSequence)) {
            createAdviceFor(refinementSequence, tickAdvice, preferredLanguage);
        }
    }

    private void setResearchesToShipyardAdvice(final int idUser,
                                               @Nonnull final List<Construction> constructions,
                                               @Nonnull final TickAdvice tickAdvice,
                                               @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(constructions, "constructions must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");

        final boolean laboratoryBuild = isConstructionPresent(constructions, EResourceType.RESEARCH, preferredLanguage);
        // research lab build?
        if (!laboratoryBuild) {
            createAdviceFor(EResourceType.RESEARCH, tickAdvice, preferredLanguage);
        } else {
            // active research?
            final List<Research> activeJobs = jobService.getResearchesFromActiveJobs(idUser);
            if (activeJobs.isEmpty()) {
                tickAdvice.setResearchPossible(true);
            }
            final Set<ResearchLevel> researchesForUser = researchService.getResearchesForUser(idUser);
            // shipyard researched?
            final boolean shipyardResearched = researchesForUser.stream()
                    .map(ResearchLevel::getResearch)
                    .anyMatch(getResearchPredicateForShipyard());
            if (!shipyardResearched) {
                setShipyardAdvice(researchesForUser, tickAdvice, preferredLanguage);
            } else {
                final boolean shipyardBuild = isConstructionPresent(constructions, EResourceType.ORBITAL_CONSTRUCTION, preferredLanguage);
                // shipyard build?
                if (!shipyardBuild) {
                    createAdviceFor(EResourceType.ORBITAL_CONSTRUCTION, tickAdvice, preferredLanguage);
                } else {
                    final List<ShipClass> shipClasses = shipClassService.findAllLatestByOwner(idUser);
                    if (shipClasses.size() > 1) {
                        tickAdvice.setSuggestedShipClass(EShipClassType.LAC);
                    }
                }
            }
        }
    }

    private static void setConstructionPossibleAdvice(@Nonnull final List<Construction> constructions, @Nonnull final TickAdvice tickAdvice) {
        Preconditions.checkNotNull(constructions, "constructions must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");

        final boolean groundFacilityWithoutJobPresent = constructions.stream()
                .filter(c -> c.getBuilding().getProductionTarget() == EResourceType.CONSTRUCTION)
                .anyMatch(facility -> facility.getJobs().isEmpty());
        // construction possible?
        if (groundFacilityWithoutJobPresent) {
            tickAdvice.setConstructionPossible(true);
        }
    }

    private static boolean isConstructionPresent(@Nonnull final List<Construction> constructions,
                                                 @Nonnull final EResourceType resourceType,
                                                 @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(constructions, "constructions must not be empty");
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        return constructions.stream().anyMatch(c -> c.getBuilding().getProductionTarget() == resourceType);
    }

    private void setShipyardAdvice(@Nonnull final Set<ResearchLevel> researchesForUser,
                                   @Nonnull final TickAdvice tickAdvice,
                                   @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(researchesForUser, "researchesForUser must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        final List<Research> researches = researchService.findAll();
        final Research shipyardResearch = researches.stream().filter(getResearchPredicateForShipyard()).findFirst().orElseThrow(NullPointerException::new);
        final Research unlockedThrough = shipyardResearch.getUnlockedThrough();
        final boolean prerequisiteFulfilled = researchesForUser.stream().anyMatch(rl -> rl.getResearch().equals(unlockedThrough) && rl.getLevel() > 0);
        if (prerequisiteFulfilled) {
            tickAdvice.setSuggestedResearch(new de.yuga.spacebattle.rest.dto.researches.Research(shipyardResearch, preferredLanguage));
        } else if (unlockedThrough != null) {
            tickAdvice.setSuggestedResearch(new de.yuga.spacebattle.rest.dto.researches.Research(unlockedThrough, preferredLanguage));
        }
    }

    private void createAdviceFor(@Nonnull final EResourceType resourceType,
                                 @Nonnull final TickAdvice tickAdvice,
                                 @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        final ProductionType productionType = new ProductionType(resourceType, EProductionCategory.PRODUCE, null);
        final List<de.yuga.spacebattle.backend.entities.buildings.Building> lab = buildingService.findBuildingByProductionType(productionType);
        tickAdvice.setSuggestedBuilding(new Building(lab.get(0), preferredLanguage));
    }

    private void createAdviceFor(@Nonnull final ERefinementSequence refinementSequence,
                                 @Nonnull final TickAdvice tickAdvice,
                                 @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        final ProductionType productionType = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, refinementSequence);
        final List<de.yuga.spacebattle.backend.entities.buildings.Building> lab = buildingService.findBuildingByProductionType(productionType);
        tickAdvice.setSuggestedBuilding(new Building(lab.get(0), preferredLanguage));
    }

    @Nonnull
    private static Predicate<Research> getResearchPredicateForShipyard() {
        return r -> r.getUnlocksBuildings().stream()
                .anyMatch(b -> b.getProductionTarget() == EResourceType.ORBITAL_CONSTRUCTION);
    }
}
