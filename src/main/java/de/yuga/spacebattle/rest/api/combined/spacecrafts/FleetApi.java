package de.yuga.spacebattle.rest.api.combined.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.caches.FleetMovementCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMarker;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMerge;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMove;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.FleetMovement;
import de.yuga.spacebattle.rest.dto.turn.Move;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private static final String MERGE_FLEET_ENDPOINT = "merge";
    private static final String MOVE_FLEETS_ENDPOINT = "moveFleets";
    private static final String PLAN_MOVES_FLEET_ENDPOINT = "planMoves";
    private static final String CANCEL_MOVES_FLEET_ENDPOINT = "cancelMoves";
    private static final String FLEET_PER_USER_PER_SYSTEM_ENDPOINT = "fleetDistribution";
    private static final String FINISHED_MOVEMENT_ENDPOINT = "finishedMovement";
    private static final String RENAME_FLEET_ENDPOINT = "rename";

    @Nonnull
    private final FleetMovementCache fleetMovementCache;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final StarSystemService starSystemService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final TickService tickService;

    @Autowired
    public FleetApi(@Nonnull final FleetMovementCache fleetMovementCache,
                    @Nonnull final FleetService fleetService,
                    @Nonnull final UserService userService,
                    @Nonnull final StarSystemService starSystemService,
                    @Nonnull final PlanetService planetService,
                    @Nonnull final TickService tickService) {
        this.fleetMovementCache = Preconditions.checkNotNull(fleetMovementCache, "fleetMovementCache must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.starSystemService = Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.tickService = Preconditions.checkNotNull(tickService, "tickService must not be empty");
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
        if (fleet == null || fleet.getOwner().getId() != getIdUser()) {
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
        final Set<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> fleets = starSystem.getFleets();
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
        final Set<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> allFleets = fleetService.findAllFleetsWithoutInterstellarMovement(idUser);

        final List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> interstellarMovement = fleetService.findAllFleetsWithInterstellarMovement(idUser);
        final List<FleetMarker> result = interstellarMovement.stream().map(FleetMarker::new).collect(Collectors.toList());
        result.addAll(allFleets.stream().map(FleetMarker::new).collect(Collectors.toList()));
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = FLEET_PER_USER_ENDPOINT)
    @Operation(summary = "Get all fleets of an owner.", operationId = "getFleetsForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Fleet.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleetsForUser() {

        final int idUser = getIdUser();

        final User user = userService.find(idUser);
        if (user != null) {
            return ResponseEntity.ok(fleetService.findAllFleetsByUser(user).stream()
                    .map(f -> new Fleet(f, getPreferredLanguage()))
                    .collect(Collectors.toList()));
        }
        throw new NotifyWebUserException("No user found.");
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

    @PostMapping(value = MERGE_FLEET_ENDPOINT)
    @Operation(summary = "Merge two fleets of an owner.", operationId = "mergeFleets",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FleetMerge.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> mergeFleets(@RequestBody @Nonnull final FleetMerge merge) {
        PreconditionWebHelper.checkNotNull(merge, "merge must not be empty");

        fleetService.mergeFleets(merge, getIdUser());

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
                                    schema = @Schema(implementation = Fleet.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> moveFleets(@RequestBody @Nonnull final List<FleetMove> moves) {
        PreconditionWebHelper.checkNotNull(moves, "moves shouldn't be null!");

        // todo validate interstellar flights with propulsion
        final List<de.yuga.spacebattle.backend.entities.turn.Move> plannedMoves = getMultiMove(getIdUser(), moves);
        return ResponseEntity.ok(fleetService.moveFleets(plannedMoves).stream()
                .map(f -> new Fleet(f, getPreferredLanguage()))
                .collect(Collectors.toList()));
    }

    @PostMapping(value = PLAN_MOVES_FLEET_ENDPOINT)
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
    public ResponseEntity<?> planMovements(@RequestBody @Nonnull final List<FleetMove> moves) {
        PreconditionWebHelper.checkNotNull(moves, "moves shouldn't be null!");

        // todo validate interstellar flights with propulsion
        final List<de.yuga.spacebattle.backend.entities.turn.Move> plannedMoves = getMultiMove(getIdUser(), moves);
        return ResponseEntity.ok(plannedMoves.stream().map(Move::new).collect(Collectors.toList()));
    }

    @Parameter(name = "fleetIds", array = @ArraySchema(schema = @Schema(implementation = Integer.class)))
    @PutMapping(value = CANCEL_MOVES_FLEET_ENDPOINT + "/{fleetIds}")
    @Operation(summary = "Cancels a movement of a fleet and creates the way back.", operationId = "cancelMovement",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> cancelMovement(@PathVariable("fleetIds") @Nonnull final List<Integer> fleetIds) {
        PreconditionWebHelper.checkNotNull(fleetIds, "fleetIds must not be empty");

        final int idUser = getIdUser();
        fleetService.cancelFlights(idUser, fleetIds);
        return ResponseEntity.ok(true);
    }

    @GetMapping(value = FINISHED_MOVEMENT_ENDPOINT)
    @Operation(summary = "Get all finished movements of fleets of an owner.", operationId = "getFinishedMovements",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = FleetMovement.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFinishedMovements() {

        final int idUser = getIdUser();
        final Tick today = tickService.getToday();
        return ResponseEntity.ok(fleetMovementCache.getMovements(today, idUser).stream()
                .map(FleetMovement::new)
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

    /**
     * Creates a list of moves for the given list of movements.
     *
     * @param idUser the owner of the fleets
     * @param moves  the planned moves
     * @return the move
     */
    @Nonnull
    private List<de.yuga.spacebattle.backend.entities.turn.Move> getMultiMove(final int idUser, @Nonnull final List<FleetMove> moves) {
        Preconditions.checkNotNull(moves, "moves shouldn't be null!");

        final List<Integer> fleetIdsToMove = moves.stream().map(FleetMove::getIdFleetToMove).collect(Collectors.toList());
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
        final Map<Integer, StarSystem> targetSystemsByIds = starSystemService.findByIds(targetSystemIds).stream().collect(Collectors.toMap(StarSystem::getId, Function.identity()));

        return moves.stream().map(move -> {
            final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = fleetsToMoveById.get(move.getIdFleetToMove());
            final StarSystem targetSystem = targetSystemsByIds.get(move.getIdDestinationSystem());
            final Orbit targetOrbit = move.getDestinationOrbit() != null ? new Orbit(move.getDestinationOrbit()) : null;
            final FleetOrbit destination = new FleetOrbit(targetOrbit, targetSystem);
            return new de.yuga.spacebattle.backend.entities.turn.Move(fleet, destination);
        }).collect(Collectors.toList());
    }

    /**
     * Creates a move for the given movement.
     *
     * @param idUser the owner of the fleet to move
     * @param move   the planned movement
     * @return the move
     */
    @Nonnull
    private de.yuga.spacebattle.backend.entities.turn.Move createSingleMove(final int idUser,
                                                                            @Nonnull final FleetMove move) {
        Preconditions.checkNotNull(move, "move shouldn't be null!");

        final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = fleetService.find(move.getIdFleetToMove());
        PreconditionWebHelper.checkNotNull(fleet, "There is no fleet to move.");

        if (fleet.getOwner().getId() != idUser) {
            throw new NotifyWebUserException("No, you cannot move this single fleet.");
        }

        final FleetOrbit currentLocation = fleet.getOrbit();
        PreconditionWebHelper.checkNotNull(currentLocation, "This move is not possible because the idFleet '" + fleet.getId() + "' is in hyperspace and cannot be moved");

        final Integer idTargetSystem = move.getIdDestinationSystem();
        final StarSystem targetSystem = idTargetSystem != null ? starSystemService.find(idTargetSystem) : null;

        final Orbit targetOrbit = move.getDestinationOrbit() != null ? new Orbit(move.getDestinationOrbit()) : null;
        final FleetOrbit destination = new FleetOrbit(targetOrbit, targetSystem);
        return new de.yuga.spacebattle.backend.entities.turn.Move(fleet, destination);
    }
}
