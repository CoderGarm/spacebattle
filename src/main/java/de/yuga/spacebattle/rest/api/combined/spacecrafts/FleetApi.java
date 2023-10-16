package de.yuga.spacebattle.rest.api.combined.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.*;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.Move;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "FleetApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + FleetApi.ENDPOINT + "/")
public class FleetApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "fleet";
    private static final String FLEET_PER_SYSTEM_ENDPOINT = "inSystem";
    private static final String FLEET_PER_PLANET_ENDPOINT = "atPlanet";
    private static final String FLEET_PER_USER_ENDPOINT = "perUser";
    private static final String MOVING_FLEET_PER_USER_ENDPOINT = "movingPerUser";
    private static final String MULTI_FLEET_ACTION_ENDPOINT = "multiaction";
    private static final String MERGE_FLEETS_ENDPOINT = "merge";
    private static final String SPLIT_FLEETS_ENDPOINT = "split";
    private static final String MOVE_FLEETS_ENDPOINT = "moveFleets";
    private static final String PLAN_MOVES_ENDPOINT = "planMoves";
    private static final String CANCEL_MOVES_ENDPOINT = "cancelMoves";
    private static final String FLEET_PER_USER_PER_SYSTEM_ENDPOINT = "fleetDistribution";
    private static final String RENAME_FLEET_ENDPOINT = "rename";
    private static final String WARSHIP_POOLING_ENDPOINT = "pool";
    private static final String RETIRE_FLEET_ENDPOINT = "retire";
    private static final String WRECK_WARSHIP_ENDPOINT = "wreckWarship";
    private static final String MOTHBALL_WARSHIP_ENDPOINT = "mothballWarship";

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final StarSystemService starSystemService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final OperationalService operationalService;

    @Autowired
    public FleetApi(@Nonnull final FleetService fleetService,
                    @Nonnull final StarSystemService starSystemService,
                    @Nonnull final PlanetService planetService,
                    @Nonnull final WarShipService warShipService,
                    @Nonnull final OperationalService operationalService) {
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.starSystemService = Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.operationalService = Preconditions.checkNotNull(operationalService, "operationalService must not be empty");
    }

    @GetMapping(value = "{idFleet}")
    @Operation(summary = "Get the fleet.", operationId = "getFleet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Fleet.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleet(@PathVariable("idFleet") final int idFleet) {

        final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = fleetService.find(idFleet);
        if (fleet == null) {
            throw new NotifyWebUserException("There should be the fleet, you searched for.");
        }

        return ResponseEntity.ok(new Fleet(fleet, getPreferredLanguage()));
    }

    @GetMapping(value = FLEET_PER_SYSTEM_ENDPOINT + "/{idStarSystem}")
    @Operation(summary = "Get all fleets inside of a star system.", operationId = "getFleetsBySystem",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = FleetMarker.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleetsBySystem(@PathVariable("idStarSystem") final int idStarSystem) {

        final StarSystem starSystem = starSystemService.find(idStarSystem);
        if (starSystem == null) {
            throw new NotifyWebUserException("There should be a star system, you searches for.");
        }
        final List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> fleets = fleetService.findAllAliveFleetsInSystems(List.of(idStarSystem));
        final int idUser = getIdUser();
        final boolean ownsPlanet = starSystem.getPlanets().stream().filter(p -> p.getOwner() != null).anyMatch(p -> p.getOwner().getId() == idUser);
        final boolean hasFleetInside = fleets.stream().anyMatch(f -> f.getOwner().getId() == idUser);
        if (!ownsPlanet && !hasFleetInside) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        return ResponseEntity.ok(fleets.stream()
                .filter(de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet::isAlive)
                .map(FleetMarker::new)
                .collect(Collectors.toList()));
    }

    @GetMapping(value = FLEET_PER_PLANET_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Get all fleets inside the orbit of a planet.", operationId = "getFleetsByPlanet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Fleet.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleetsByPlanet(@PathVariable("idPlanet") final int idPlanet) {

        final int idUser = getIdUser();
        final Planet planet = planetService.find(idPlanet);
        PreconditionWebHelper.checkNotNull(planet, "planet must not be empty");
        final Set<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> allFleetsByPlanet = fleetService.findAllAnchoredForPlanet(planet).stream()
                .filter(f -> f.getOwner().getId() == idUser).collect(Collectors.toSet());
        return ResponseEntity.ok(allFleetsByPlanet.stream()
                .map(f -> new Fleet(f, getPreferredLanguage()))
                .collect(Collectors.toList()));
    }

    @GetMapping(value = FLEET_PER_SYSTEM_ENDPOINT + "/{idStarSystem}/" + FLEET_PER_USER_ENDPOINT + "/{idOwner}")
    @Operation(summary = "Get all fleets inside of a star system for a specific user.", operationId = "getFleetsBySystemAndOwner",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Fleet.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleetsBySystemAndOwner(@PathVariable("idStarSystem") final int idStarSystem,
                                                       @PathVariable("idOwner") final int idOwner) {

        return ResponseEntity.ok(fleetService.findAllFleetsBy(idStarSystem, idOwner).stream()
                .map(f -> new Fleet(f, getPreferredLanguage()))
                .collect(Collectors.toList()));
    }

    @GetMapping(value = FLEET_PER_USER_PER_SYSTEM_ENDPOINT)
    @Operation(summary = "Get all the star systems which are holding fleets with the fleet's owner.", operationId = "getFleetDistribution",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = FleetMarker.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleetDistribution() {

        final int idUser = getIdUser();
        final List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> byUser = fleetService.findAllFleetsByUser(idUser);

        final Set<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> fleets = new HashSet<>();

        final List<Planet> allColonizedBy = planetService.findAllColonizedBy(idUser);
        final Set<Integer> systemIDsWithColonies = allColonizedBy.stream().map(Planet::getSystem).map(StarSystem::getId).collect(Collectors.toSet());
        final Set<Integer> systemIDsWithPresence = byUser.stream()
                .filter(f -> f.getOrbit() != null)
                .filter(f -> f.getOrbit().getSystem() != null)
                .map(f -> f.getOrbit().getSystem())
                .map(StarSystem::getId)
                .collect(Collectors.toSet());

        systemIDsWithColonies.addAll(systemIDsWithPresence);
        final List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> allAliveFleetsInSystemsWithColonies = fleetService.findAllAliveFleetsInSystems(systemIDsWithColonies);

        fleets.addAll(byUser);
        fleets.addAll(allAliveFleetsInSystemsWithColonies);

        return ResponseEntity.ok(fleets.stream().filter(f -> !f.getAliveShips().isEmpty()).map(FleetMarker::new).collect(Collectors.toSet()));
    }

    @GetMapping(value = FLEET_PER_USER_ENDPOINT)
    @Operation(summary = "Get all fleets of an owner.", operationId = "getFleetsForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = AbstractId.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleetsForUser() {
        return ResponseEntity.ok(fleetService.findAllAliveFleetsBy(getIdUser()));
    }

    @GetMapping(value = MOVING_FLEET_PER_USER_ENDPOINT)
    @Operation(summary = "Get all moving fleets of an owner.", operationId = "getMovingFleetsForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Fleet.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getMovingFleetsForUser() {

        final int idUser = getIdUser();
        return ResponseEntity.ok(fleetService.findAllFleetsWithMovement(idUser).stream()
                .map(f -> new Fleet(f, getPreferredLanguage()))
                .collect(Collectors.toList()));

    }

    @PostMapping(value = MULTI_FLEET_ACTION_ENDPOINT)
    @Operation(summary = "Transfer the warships between existing fleets.", operationId = "multiActionFleetFormation",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FleetFormationMultiAction.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FleetFormationMultiActionResult.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> multiActionFleetFormation(@RequestBody @Nonnull final FleetFormationMultiAction multiAction) {
        PreconditionWebHelper.checkNotNull(multiAction, "multiAction must not be empty");

        final FleetMerge fleetMerge = multiAction.getFleetMerge();
        final FleetMergeResult fleetMergeResult = fleetMerge != null ? fleetService.mergeFleets(fleetMerge, getIdUser()) : null;

        final List<Integer> warshipIDs = multiAction.getShipsToPool();
        fleetService.poolWarships(getIdUser(), warshipIDs);

        final FleetSplit fleetSplit = multiAction.getFleetSplit();
        final Set<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> splitFleets = fleetSplit != null ? fleetService.splitFleets(fleetSplit, getIdUser()) : Set.of();

        return ResponseEntity.ok(new FleetFormationMultiActionResult(fleetMergeResult, splitFleets, getPreferredLanguage()));
    }

    @PostMapping(value = MERGE_FLEETS_ENDPOINT)
    @Operation(summary = "Transfer the warships between existing fleets.", operationId = "mergeFleets",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FleetMerge.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FleetMergeResult.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> mergeFleets(@RequestBody @Nonnull final FleetMerge merge) {
        PreconditionWebHelper.checkNotNull(merge, "merge must not be empty");

        return ResponseEntity.ok(fleetService.mergeFleets(merge, getIdUser()));
    }

    @PostMapping(value = SPLIT_FLEETS_ENDPOINT)
    @Operation(summary = "Split an existing fleet into multiple fleets.", operationId = "splitFleets",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FleetSplit.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Fleet.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> splitFleets(@RequestBody @Nonnull final FleetSplit fleetSplit) {
        PreconditionWebHelper.checkNotNull(fleetSplit, "fleetSplit must not be empty");

        return ResponseEntity.ok(fleetService.splitFleets(fleetSplit, getIdUser()).stream().map(f -> new Fleet(f, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @PutMapping(value = RENAME_FLEET_ENDPOINT + "/warship")
    @Operation(summary = "Rename a ship.", operationId = "renameWarship",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractId.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> renameWarship(@RequestBody @Nonnull final AbstractId warShip) {
        PreconditionWebHelper.checkNotNull(warShip, "warShip shouldn't be null!");
        Preconditions.checkArgument(StringUtils.isNotEmpty(warShip.getName()), "name must not be empty");

        warShipService.rename(getIdUser(), warShip.getId(), warShip.getName());
        return ResponseEntity.ok(true);
    }

    @PostMapping(value = MOVE_FLEETS_ENDPOINT)
    @Operation(summary = "Moves a fleet to another celestial.", operationId = "moveFleets",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = FleetMove.class))
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = FleetMarker.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> moveFleets(@RequestBody @Nonnull final Set<FleetMove> moves) {
        PreconditionWebHelper.checkNotNull(moves, "moves shouldn't be null!");

        // todo validate interstellar flights with propulsion
        final List<de.yuga.spacebattle.backend.entities.turn.Move> plannedMoves = getMultiMove(getIdUser(), moves);
        return ResponseEntity.ok(fleetService.moveFleets(plannedMoves).stream()
                .map(FleetMarker::new)
                .collect(Collectors.toList()));
    }

    @PostMapping(value = PLAN_MOVES_ENDPOINT)
    @Operation(summary = "Plan a movement of a fleet to another celestial.", operationId = "planMovements",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = FleetMove.class))
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Move.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> planMovements(@RequestBody @Nonnull final Set<FleetMove> moves) {
        PreconditionWebHelper.checkNotNull(moves, "moves shouldn't be null!");

        // todo validate interstellar flights with propulsion
        final List<de.yuga.spacebattle.backend.entities.turn.Move> plannedMoves = getMultiMove(getIdUser(), moves);
        return ResponseEntity.ok(plannedMoves.stream().map(Move::new).collect(Collectors.toList()));
    }

    @Parameter(name = "fleetIds", array = @ArraySchema(schema = @Schema(implementation = Integer.class)))
    @PutMapping(value = CANCEL_MOVES_ENDPOINT + "/{fleetIds}")
    @Operation(summary = "Cancels a movement of a fleet and creates the way back.", operationId = "cancelMovements",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = FleetMarker.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> cancelMovements(@PathVariable("fleetIds") @Nonnull final List<Integer> fleetIds) {
        PreconditionWebHelper.checkNotNull(fleetIds, "fleetIds must not be empty");

        final int idUser = getIdUser();
        final Set<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> cancelFlights = fleetService.cancelFlights(idUser, fleetIds);
        return ResponseEntity.ok(cancelFlights.stream()
                .map(FleetMarker::new)
                .collect(Collectors.toList()));
    }

    @PutMapping(value = RENAME_FLEET_ENDPOINT + "/{idFleet}/{name}")
    @Operation(summary = "Renames a fleet.", operationId = "renameFleet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> renameFleet(@PathVariable("idFleet") final int idFleet, @PathVariable("name") final String name) {

        final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = fleetService.find(idFleet);
        if (fleet == null || fleet.getOwner().getId() != getIdUser()) {
            throw new NotifyWebUserException("Nope, I guess not.");
        }

        fleet.setName(name);
        fleetService.save(fleet);
        return ResponseEntity.ok(true);
    }

    @PutMapping(value = WARSHIP_POOLING_ENDPOINT)
    @Operation(summary = "Send ships to the pool", operationId = "sendWarshipsToPool",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Integer.class)))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> sendWarshipsToPool(@RequestBody @Nonnull final List<Integer> warshipIDs) {
        Preconditions.checkNotNull(warshipIDs, "warshipIDs must not be empty");

        final int idUser = getIdUser();
        fleetService.poolWarships(idUser, warshipIDs);
        return ResponseEntity.ok(true);
    }

    @GetMapping(value = WARSHIP_POOLING_ENDPOINT)
    @Operation(summary = "Renames a fleet.", operationId = "getPooledWarships",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = WarShip.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPooledWarships() {
        final int idUser = getIdUser();
        final Set<de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip> pooledShips = fleetService.findPooledWarships(idUser);
        return ResponseEntity.ok(pooledShips.stream().map(w -> new WarShip(w, w.getWarshipHealthState(), getPreferredLanguage())));
    }

    @PutMapping(value = RETIRE_FLEET_ENDPOINT + "/{idFleet}")
    @Operation(summary = "Renames a fleet.", operationId = "retireFleet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> retireFleet(@PathVariable("idFleet") final int idFleet) {

        final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = fleetService.find(idFleet);
        if (fleet == null || fleet.getOwner().getId() != getIdUser()) {
            throw new NotifyWebUserException("Nope, I guess not.");
        }

        fleetService.markAsDestroyed(fleet);
        return ResponseEntity.ok(true);
    }

    @PutMapping(value = MOTHBALL_WARSHIP_ENDPOINT + "/{idWarship}")
    @Operation(summary = "Renames a fleet.", operationId = "mothballWarship",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> mothballWarship(@PathVariable("idWarship") final int idWarship) {

        final de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip warShip = warShipService.findById(idWarship);
        if (warShip == null) {
            return ResponseEntity.ok(true);
        }
        if (warShip.getShipClass().getOwner().getId() != getIdUser()) {
            throw new NotifyWebUserException("Nope, I guess not.");
        }
        fleetService.mothballShip(warShip);
        return ResponseEntity.ok(true);
    }

    @PutMapping(value = WRECK_WARSHIP_ENDPOINT + "/{idWarship}")
    @Operation(summary = "Renames a fleet.", operationId = "wreckWarship",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> wreckWarship(@PathVariable("idWarship") final int idWarship) {

        final de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip warShip = warShipService.findById(idWarship);
        if (warShip == null) {
            return ResponseEntity.ok(true);
        }
        if (warShip.getShipClass().getOwner().getId() != getIdUser()) {
            throw new NotifyWebUserException("Nope, I guess not.");
        }

        fleetService.retire(warShip);
        return ResponseEntity.ok(true);
    }

    /**
     * Creates a list of moves for the given list of movements <b>but didn't persist them intentionally</b>.
     *
     * @param idUser the owner of the fleets
     * @param moves  the planned moves
     * @return the move
     */
    @Nonnull
    private List<de.yuga.spacebattle.backend.entities.turn.Move> getMultiMove(final int idUser, @Nonnull final Set<FleetMove> moves) {
        Preconditions.checkNotNull(moves, "moves shouldn't be null!");

        final Set<Integer> fleetIdsToMove = moves.stream().map(FleetMove::getIdFleetToMove).collect(Collectors.toSet());
        final Map<Integer, de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> fleetsToMoveById = fleetService.findByIds(fleetIdsToMove)
                .stream()
                .collect(Collectors.toMap(de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet::getId, Function.identity()));

        if (fleetsToMoveById.isEmpty()) {
            throw new NotifyWebUserException("There are no fleets to move.");
        }
        fleetsToMoveById.values()
                .stream()
                // filter not matching IDs
                .filter(fleet -> fleet.getOwner().getId() == idUser)
                .findAny()
                // if there is at least one not matching id -> fire and forget
                .orElseThrow(() -> new NotifyWebUserException("No, you cannot plan the movement of these fleets."));

        final List<Integer> targetSystemIds = moves.stream()
                .map(FleetMove::getIdDestinationSystem)
                .filter(Objects::nonNull).collect(Collectors.toList());
        final Map<Integer, StarSystem> targetSystemsByIds = starSystemService.findAll(targetSystemIds).stream().collect(Collectors.toMap(StarSystem::getId, Function.identity()));

        return moves.stream().map(move -> {
                    final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = fleetsToMoveById.get(move.getIdFleetToMove());
                    if (fleet.getMove() != null) {
                        return null;
                    }
                    final StarSystem targetSystem = targetSystemsByIds.get(move.getIdDestinationSystem());
                    final Orbit targetOrbit = move.getDestinationOrbit() != null ? new Orbit(move.getDestinationOrbit()) : null;
                    final FleetOrbit destination = new FleetOrbit(targetOrbit, targetSystem);
                    return new de.yuga.spacebattle.backend.entities.turn.Move(fleet, destination);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
