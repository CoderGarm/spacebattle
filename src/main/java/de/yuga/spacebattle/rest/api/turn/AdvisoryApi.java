package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.mission.MissionService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import de.yuga.spacebattle.rest.dto.turn.TickAdvice;
import de.yuga.spacebattle.rest.dto.turn.resources.trade.TradeContract;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@RestController
@RolesAllowed({"USER"})
@Tag(name = "AdvisoryApi")
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + AdvisoryApi.ENDPOINT + "/")
public class AdvisoryApi extends BaseApi {

    public static final String ENDPOINT = "advisory";
    private static final String PIRATE_HUNT_ENDPOINT = "pirateHunt";
    private static final String CONVOY_PROTECTION_ENDPOINT = "convoyProtection";
    private static final String INFRASTRUCTURE_ADVICE_ENDPOINT = "infrastructure";

    @Nonnull
    private final MissionService missionService;

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

    @Nonnull
    private final FleetService fleetService;

    @Autowired
    public AdvisoryApi(@Nonnull final MissionService missionService,
                       @Nonnull final ConstructionService constructionService,
                       @Nonnull final BuildingService buildingService,
                       @Nonnull final ResearchService researchService,
                       @Nonnull final JobService jobService,
                       @Nonnull final ShipClassService shipClassService,
                       @Nonnull final FleetService fleetService) {
        this.missionService = Preconditions.checkNotNull(missionService, "missionService must not be empty");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService must not be empty");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService must not be empty");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService must not be empty");
        this.jobService = Preconditions.checkNotNull(jobService, "jobService must not be empty");
        this.shipClassService = Preconditions.checkNotNull(shipClassService, "shipClassService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
    }

    @GetMapping(value = PIRATE_HUNT_ENDPOINT)
    @Operation(summary = "Get all jobs which finished today.", operationId = "getPirateHuntAdvice",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Planet.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPirateHuntAdvice() {
        final Set<de.yuga.spacebattle.backend.entities.orbitals.Planet> planets = missionService.findAllPlanetsWithoutPirateHunt(getIdUser());

        final List<Fleet> anchoredFleets = fleetService.findAllFleetsWithoutMovementByUser(getIdUser());
        final Set<de.yuga.spacebattle.backend.entities.orbitals.Planet> planetsWithAnchoredFleets = anchoredFleets.stream()
                .map(Fleet::getOrbit)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()).stream()
                .map(FleetOrbit::getPlanet)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        planets.removeAll(planetsWithAnchoredFleets);
        return ResponseEntity.ok(planets.stream().map(Planet::new).collect(Collectors.toList()));
    }

    @GetMapping(value = CONVOY_PROTECTION_ENDPOINT)
    @Operation(summary = "Get all jobs which finished today.", operationId = "getConvoyProtectionAdvice",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = TradeContract.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getConvoyProtectionAdvice() {
        final Set<TradedResource> tradedResources = missionService.findAllConvoysWithoutEscort(getIdUser());
        return ResponseEntity.ok(tradedResources.stream().map(TradeContract::new).collect(Collectors.toList()));
    }

    @GetMapping(value = INFRASTRUCTURE_ADVICE_ENDPOINT)
    @Operation(summary = "Get all jobs which finished today.", operationId = "getConstructionAdvice",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TickAdvice.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getConstructionAdvice() {
        final TickAdvice tickAdvice = new TickAdvice();

        final List<Construction> constructions = constructionService.findAllConstructionsForUser(getIdUser());

        setConstructionPossibleAdvice(constructions, tickAdvice);

        /* last comes, first serve */
        setRefinementAdvisory(constructions, ERefinementSequence.EDUCATION_MILITARY_II, tickAdvice);
        setRefinementAdvisory(constructions, ERefinementSequence.EDUCATION_MILITARY_I, tickAdvice);
        setRefinementAdvisory(constructions, ERefinementSequence.EDUCATION_CIVIL_III, tickAdvice);

        setRefinementAdvisory(constructions, ERefinementSequence.EDUCATION_CIVIL_II, tickAdvice);
        setRefinementAdvisory(constructions, ERefinementSequence.EDUCATION_CIVIL_I, tickAdvice);
        setResearchesToShipyardAdvice(constructions, tickAdvice);

        return ResponseEntity.ok(tickAdvice);
    }

    private void setRefinementAdvisory(@Nonnull final List<Construction> constructions,
                                       @Nonnull final ERefinementSequence refinementSequence,
                                       @Nonnull final TickAdvice tickAdvice) {
        Preconditions.checkNotNull(constructions, "constructions must not be empty");
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");

        if (constructions.stream().noneMatch(c -> c.getBuilding().getProductionType().getRefinementSequence() == refinementSequence)) {
            createAdviceFor(refinementSequence, tickAdvice);
        }
    }

    private void setResearchesToShipyardAdvice(@Nonnull final List<Construction> constructions, @Nonnull final TickAdvice tickAdvice) {
        Preconditions.checkNotNull(constructions, "constructions must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");

        final boolean laboratoryBuild = isConstructionPresent(constructions, EResourceType.RESEARCH);
        // research lab build?
        if (!laboratoryBuild) {
            createAdviceFor(EResourceType.RESEARCH, tickAdvice);
        } else {
            // active research?
            final List<Research> activeJobs = jobService.getResearchesFromActiveJobs(getIdUser());
            if (activeJobs.isEmpty()) {
                tickAdvice.setResearchPossible(true);
            }
            final Set<ResearchLevel> researchesForUser = researchService.getResearchesForUser(getIdUser());
            // shipyard researched?
            final boolean shipyardResearched = researchesForUser.stream()
                    .map(ResearchLevel::getResearch)
                    .anyMatch(getResearchPredicateForShipyard());
            if (!shipyardResearched) {
                setShipyardAdvice(researchesForUser, tickAdvice);
            } else {
                final boolean shipyardBuild = isConstructionPresent(constructions, EResourceType.ORBITAL_CONSTRUCTION);
                // shipyard build?
                if (!shipyardBuild) {
                    createAdviceFor(EResourceType.ORBITAL_CONSTRUCTION, tickAdvice);
                } else {
                    final List<ShipClass> shipClasses = shipClassService.findAllLatestByOwner(getIdUser());
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

    private static boolean isConstructionPresent(@Nonnull final List<Construction> constructions, @Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(constructions, "constructions must not be empty");
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");

        return constructions.stream().anyMatch(c -> c.getBuilding().getProductionTarget() == resourceType);
    }

    private void setShipyardAdvice(@Nonnull final Set<ResearchLevel> researchesForUser, @Nonnull final TickAdvice tickAdvice) {
        Preconditions.checkNotNull(researchesForUser, "researchesForUser must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");

        final List<Research> researches = researchService.findAll();
        final Research shipyardResearch = researches.stream().filter(getResearchPredicateForShipyard()).findFirst().orElseThrow(NullPointerException::new);
        final Research unlockedThrough = shipyardResearch.getUnlockedThrough();
        final boolean prerequisiteFulfilled = researchesForUser.stream().anyMatch(rl -> rl.getResearch().equals(unlockedThrough) && rl.getLevel() > 0);
        if (prerequisiteFulfilled) {
            tickAdvice.setSuggestedResearch(new de.yuga.spacebattle.rest.dto.researches.Research(shipyardResearch, getPreferredLanguage()));
        } else if (unlockedThrough != null) {
            tickAdvice.setSuggestedResearch(new de.yuga.spacebattle.rest.dto.researches.Research(unlockedThrough, getPreferredLanguage()));
        }
    }

    private void createAdviceFor(@Nonnull final EResourceType resourceType, @Nonnull final TickAdvice tickAdvice) {
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");

        final ProductionType productionType = new ProductionType(resourceType, EProductionCategory.PRODUCE, null);
        final List<de.yuga.spacebattle.backend.entities.buildings.Building> lab = buildingService.findBuildingByProductionType(productionType);
        tickAdvice.setSuggestedBuilding(new Building(lab.get(0), getPreferredLanguage()));
    }

    private void createAdviceFor(@Nonnull final ERefinementSequence refinementSequence, @Nonnull final TickAdvice tickAdvice) {
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");
        Preconditions.checkNotNull(tickAdvice, "tickAdvice must not be empty");

        final ProductionType productionType = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, refinementSequence);
        final List<de.yuga.spacebattle.backend.entities.buildings.Building> lab = buildingService.findBuildingByProductionType(productionType);
        tickAdvice.setSuggestedBuilding(new Building(lab.get(0), getPreferredLanguage()));
    }

    @Nonnull
    private static Predicate<Research> getResearchPredicateForShipyard() {
        return r -> r.getUnlocksBuildings().stream()
                .anyMatch(b -> b.getProductionTarget() == EResourceType.ORBITAL_CONSTRUCTION);
    }
}
