package de.yuga.spacebattle.rest.api.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassCreationService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.dto.constructables.buildings.PlannedConstruction;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.ShipyardConstructionSelection;
import de.yuga.spacebattle.rest.dto.enums.EResourceTypeList;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass;
import de.yuga.spacebattle.rest.dto.turn.resources.MiningFactors;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceDeposit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Api(tags = "ResourcesApi")
@RolesAllowed("ROLE_USER") // todo how to add direct roles
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + ResourcesApi.ENDPOINT + "/")
public class ResourcesApi {

    @Nonnull
    public static final String ENDPOINT = "resources";
    private static final String RESOURCE_TYPES_ENDPOINT = "types";
    private static final String MINING_FACTORS_ENDPOINT = "miningFactors";
    private static final String RESOURCE_DEPOSIT_ENDPOINT = "resourceDeposit";
    private static final String COSTS_ENDPOINT = "costs";
    private static final String SHIPYARD_ORDER_COSTS_ENDPOINT = "costsShipyard";
    private static final String SHIP_CLASS_COSTS_ENDPOINT = "costsShipClass";

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final ConstructionService constructionService;

    @Nonnull
    private final BuildingService buildingService;

    @Nonnull
    private final ShipClassCreationService shipClassCreationService;

    @Autowired
    public ResourcesApi(@Nonnull final JobService jobService,
                        @Nonnull final UserService userService,
                        @Nonnull final PlanetService planetService,
                        @Nonnull final ConstructionService constructionService,
                        @Nonnull final BuildingService buildingService,
                        @Nonnull final ShipClassCreationService shipClassCreationService) {
        Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(constructionService, "constructionService shouldn't be null!");
        Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");
        Preconditions.checkNotNull(shipClassCreationService, "shipClassCreationService shouldn't be null!");

        this.userService = userService;
        this.jobService = jobService;
        this.planetService = planetService;
        this.constructionService = constructionService;
        this.buildingService = buildingService;
        this.shipClassCreationService = shipClassCreationService;
    }

    @GetMapping(value = RESOURCE_TYPES_ENDPOINT)
    @ApiOperation(value = "Get all EResourceTypes.", nickname = "getEResourceTypes")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EResourceTypeList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEResourceTypes() {
        final List<EResourceType> eResourceTypes = Arrays.stream(EResourceType.values()).collect(Collectors.toList());
        return ResponseEntity.ok(new EResourceTypeList(eResourceTypes));
    }

    @GetMapping(value = MINING_FACTORS_ENDPOINT + "/{idPlanet}")
    @ApiOperation(value = "Get the mining factors of the planet.", nickname = "getMiningFactors")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MiningFactors.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getMiningFactors(@PathVariable("idPlanet") final int idPlanet) {

        final Planet planet = planetService.find(idPlanet);
        PreconditionWebHelper.checkNotNull(planet, "planet shouldn't be null!");
        final de.yuga.spacebattle.backend.entities.turn.resources.MiningFactors miningFactors = planet.getMiningFactors();
        return ResponseEntity.ok(new MiningFactors(miningFactors));
    }


    @GetMapping(value = RESOURCE_DEPOSIT_ENDPOINT + "/{idPlanet}")
    @ApiOperation(value = "Get all EResourceTypes.", nickname = "getResourceDeposit")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getResourceDeposit(@PathVariable("idPlanet") final int idPlanet) {

        final Planet planet = planetService.find(idPlanet);
        PreconditionWebHelper.checkNotNull(planet, "planet shouldn't be null!");
        final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        return ResponseEntity.ok(new ResourceDeposit(resourceDeposit));
    }

    @PostMapping(value = COSTS_ENDPOINT)
    @ApiOperation(value = "Get the costs of the given construction.", nickname = "getBuildingCosts")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getBuildingCosts(@RequestBody @Nonnull final PlannedConstruction construction) {
        PreconditionWebHelper.checkNotNull(construction, "At this stage you should try to build something!");

        final int idBuilding = construction.getIdBuilding();
        final int targetLevel = construction.getTargetLevel();
        final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit costs = constructionService.getCosts(idBuilding, targetLevel);
        if (costs != null) {
            return ResponseEntity.ok(new ResourceDeposit(costs));
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping(value = SHIPYARD_ORDER_COSTS_ENDPOINT)
    @ApiOperation(value = "Get the costs of the given shipyard order.", nickname = "getShipyardOrderCosts")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getShipyardOrderCosts(@RequestBody @Nonnull final List<ShipyardConstructionSelection> shipyardConstructionOrder) {
        PreconditionWebHelper.checkNotNull(shipyardConstructionOrder, "Maybe there should be something like a request?!");

        final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit costs = shipClassCreationService.getCosts(shipyardConstructionOrder);
        if (costs != null) {
            return ResponseEntity.ok(new ResourceDeposit(costs));
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping(value = SHIP_CLASS_COSTS_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get the costs of the given shipyard order.", nickname = "getShipClassCosts")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getShipClassCosts(@RequestBody @Nonnull final ShipClass shipClass, @PathVariable("idUser") final int idUser) {
        PreconditionWebHelper.checkNotNull(shipClass, "Maybe there should be something like a request?!");

        final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit costs = shipClassCreationService.getCosts(shipClass, idUser);
        if (costs != null) {
            return ResponseEntity.ok(new ResourceDeposit(costs));
        } else {
            return ResponseEntity.ok().build();
        }
    }
}
