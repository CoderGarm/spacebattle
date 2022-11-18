package de.yuga.spacebattle.rest.api.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassCreationService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapabilities;
import de.yuga.spacebattle.rest.dto.constructables.buildings.PlannedConstruction;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.ShipyardConstructionSelection;
import de.yuga.spacebattle.rest.dto.enums.EEducationType;
import de.yuga.spacebattle.rest.dto.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass;
import de.yuga.spacebattle.rest.dto.turn.resources.MiningFactors;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceDeposit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "ResourcesApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + ResourcesApi.ENDPOINT + "/")
public class ResourcesApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "resources";
    private static final String RESOURCE_TYPES_ENDPOINT = "types";
    private static final String HUMAN_RESOURCE_TYPES_ENDPOINT = "educationTypes";
    private static final String MINING_FACTORS_ENDPOINT = "miningFactors";
    private static final String RESOURCE_DEPOSIT_ENDPOINT = "deposit";
    private static final String RESOURCE_DEMAND_ENDPOINT = "demand";
    private static final String RESOURCE_UTILIZATION_ENDPOINT = "utilization";
    private static final String INCOME_ENDPOINT = "income";
    private static final String CAPACITY_ENDPOINT = "capacity";
    private static final String COSTS_ENDPOINT = "costs";
    private static final String SHIPYARD_ORDER_COSTS_ENDPOINT = "costsShipyard";
    private static final String SHIP_CLASS_COSTS_ENDPOINT = "costsShipClass";
    private static final String SHIP_CLASS_CAPS_ENDPOINT = "capsShipClass";
    private static final String FLEET_COSTS_ENDPOINT = "costsFleet";

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final ConstructionService constructionService;

    @Nonnull
    private final ShipClassCreationService shipClassCreationService;

    @Nonnull
    private final ShipClassService shipClassService;

    @Nonnull
    private final FleetService fleetService;

    @Autowired
    public ResourcesApi(@Nonnull final PlanetService planetService,
                        @Nonnull final ConstructionService constructionService,
                        @Nonnull final ShipClassCreationService shipClassCreationService,
                        @Nonnull final ShipClassService shipClassService,
                        @Nonnull final FleetService fleetService) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService shouldn't be null!");
        this.shipClassCreationService = Preconditions.checkNotNull(shipClassCreationService, "shipClassCreationService shouldn't be null!");
        this.shipClassService = Preconditions.checkNotNull(shipClassService, "shipClassService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
    }

    @GetMapping(value = RESOURCE_TYPES_ENDPOINT)
    @Operation(summary = "Get all EResourceTypes.", operationId = "getEResourceTypes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = EResourceType.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEResourceTypes() {
        return ResponseEntity.ok(Arrays.stream(de.yuga.spacebattle.backend.enums.EResourceType.values())
                .map(EResourceType::new)
                .collect(Collectors.toList()));
    }

    @GetMapping(value = HUMAN_RESOURCE_TYPES_ENDPOINT)
    @Operation(summary = "Get all EEducationTypes.", operationId = "getEEducationTypes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = EEducationType.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEEducationTypes() {
        return ResponseEntity.ok(Arrays.stream(de.yuga.spacebattle.backend.enums.EEducationType.values())
                .map(EEducationType::new)
                .collect(Collectors.toList()));
    }

    @GetMapping(value = MINING_FACTORS_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Get the mining factors of the planet.", operationId = "getMiningFactors",
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
        return ResponseEntity.ok(new MiningFactors(planet.getMiningFactors()));
    }

    @GetMapping(value = RESOURCE_DEMAND_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Get all EResourceTypes.", operationId = "getResourceDemand",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getResourceDemand(@PathVariable("idPlanet") final int idPlanet) {

        final Planet planet = planetService.find(idPlanet);
        PreconditionWebHelper.checkNotNull(planet, "planet shouldn't be null!");
        return ResponseEntity.ok(new ResourceDeposit(planet.getResourceDemand()));
    }

    @GetMapping(value = RESOURCE_UTILIZATION_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Get all EResourceTypes.", operationId = "getResourceUtilization",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getResourceUtilization(@PathVariable("idPlanet") final int idPlanet) {

        final Planet planet = planetService.find(idPlanet);
        PreconditionWebHelper.checkNotNull(planet, "planet shouldn't be null!");
        return ResponseEntity.ok(new ResourceDeposit(planet.getResourceUtilization()));
    }

    @GetMapping(value = RESOURCE_DEPOSIT_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Get all EResourceTypes.", operationId = "getResourceDeposit",
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
        return ResponseEntity.ok(new ResourceDeposit(planet.getResourceDeposit()));
    }

    @GetMapping(value = RESOURCE_DEPOSIT_ENDPOINT)
    @Operation(summary = "Get all EResourceTypes.", operationId = "getResourceDepositForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getResourceDepositForUser() {
        final int idUser = getIdUser();
        final List<Planet> planets = planetService.findAllColonizedBy(idUser);
        final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit resourceDeposit = new de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit(EDepositType.DEPOSITS);
        planets.forEach(planet -> {
            final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit deposit = planet.getResourceDeposit();
            resourceDeposit.add(deposit);
        });

        return ResponseEntity.ok(new ResourceDeposit(resourceDeposit));
    }

    @GetMapping(value = INCOME_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Get all incomes by EResourceTypes.", operationId = "getPlanetaryIncome",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPlanetaryIncome(@PathVariable("idPlanet") final int idPlanet) {

        final Planet planet = planetService.find(idPlanet);
        PreconditionWebHelper.checkNotNull(planet, "planet shouldn't be null!");

        final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit ticklyIncome = planet.getTicklyIncome();
        return ResponseEntity.ok(new ResourceDeposit(ticklyIncome));
    }

    @GetMapping(value = CAPACITY_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Get all incomes by EResourceTypes.", operationId = "getPlanetaryCapacity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPlanetaryCapacity(@PathVariable("idPlanet") final int idPlanet) {

        final Planet planet = planetService.find(idPlanet);
        PreconditionWebHelper.checkNotNull(planet, "planet shouldn't be null!");

        final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit capacity = planet.getResourceCapacity();
        return ResponseEntity.ok(new ResourceDeposit(capacity));
    }

    @PostMapping(value = COSTS_ENDPOINT)
    @Operation(summary = "Get the costs of the given construction.", operationId = "getBuildingCosts",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlannedConstruction.class)
                    )
            ),
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
    @Operation(summary = "Get the costs of the given shipyard order.", operationId = "getShipyardOrderCosts",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ShipyardConstructionSelection.class))
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getShipyardOrderCosts(@RequestBody @Nonnull final List<ShipyardConstructionSelection> shipyardConstructionOrder) {
        PreconditionWebHelper.checkNotNull(shipyardConstructionOrder, "Maybe there should be something like a request?!");

        return ResponseEntity.ok(new ResourceDeposit(shipClassCreationService.getCosts(shipyardConstructionOrder)));
    }

    @PostMapping(value = SHIP_CLASS_COSTS_ENDPOINT)
    @Operation(summary = "Get the costs of the given shipyard order.", operationId = "getShipClassCosts",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ShipClass.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getShipClassCosts(@RequestBody @Nonnull final ShipClass shipClass) {
        PreconditionWebHelper.checkNotNull(shipClass, "Maybe there should be something like a request?!");

        final int idUser = getIdUser();
        return ResponseEntity.ok(new ResourceDeposit(shipClassCreationService.getCosts(shipClass, idUser)));
    }

    @GetMapping(value = SHIP_CLASS_COSTS_ENDPOINT + "/{idShipClass}")
    @Operation(summary = "Get the costs of the given shipyard order.", operationId = "getCostsForShipClass",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getCostsForShipClass(@PathVariable("idShipClass") final int idShipClass) {

        final int idUser = getIdUser();
        final de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass shipClass = shipClassService.find(idShipClass);
        if (shipClass == null || shipClass.getOwner().getId() != idUser) {
            PreconditionWebHelper.checkNotNull(shipClass, "shipClass must not be empty");
        }
        return ResponseEntity.ok(new ResourceDeposit(shipClass.getCosts()));
    }

    @GetMapping(value = FLEET_COSTS_ENDPOINT + "/{idFleet}")
    @Operation(summary = "Get the costs of the given shipyard order.", operationId = "getCostsForFleet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getCostsForFleet(@PathVariable("idFleet") final int idFleet) {

        final int idUser = getIdUser();
        final Fleet fleet = fleetService.findById(idFleet);
        if (fleet == null || fleet.getOwner().getId() != idUser) {
            PreconditionWebHelper.checkNotNull(fleet, "fleet must not be empty");
        }
        final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit costs = new de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit(EDepositType.COSTS);
        fleet.getAliveShips().forEach(warShip -> costs.add(warShip.getShipClass().getCosts()));
        return ResponseEntity.ok(new ResourceDeposit(costs));
    }

    @PostMapping(value = SHIP_CLASS_CAPS_ENDPOINT)
    @Operation(summary = "Get the costs of the given shipyard order.", operationId = "getShipClassCaps",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ShipClass.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SpacecraftCapabilities.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getShipClassCaps(@RequestBody @Nonnull final ShipClass shipClass) {
        PreconditionWebHelper.checkNotNull(shipClass, "Maybe there should be something like a request?!");

        final int idUser = getIdUser();
        return ResponseEntity.ok(shipClassCreationService.getCaps(shipClass, idUser));
    }
}
