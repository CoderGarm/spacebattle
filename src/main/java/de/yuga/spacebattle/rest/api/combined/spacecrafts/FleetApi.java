package de.yuga.spacebattle.rest.api.combined.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.*;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.Move;
import de.yuga.spacebattle.rest.dto.turn.MoveList;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Api(tags = "FleetApi")
@RolesAllowed("ROLE_USER") // todo how to add direct roles
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + FleetApi.ENDPOINT + "/")
public class FleetApi {

    @Nonnull
    public static final String ENDPOINT = "fleet";
    private static final String FLEET_PER_SYSTEM_ENDPOINT = "inSystem";
    private static final String FLEET_PER_USER_ENDPOINT = "perUser";
    private static final String MERGE_FLEET_ENDPOINT = "merge";
    private static final String MOVE_FLEET_ENDPOINT = "move";
    private static final String MOVE_FLEETS_ENDPOINT = "moveFleets";
    private static final String PLAN_MOVE_FLEET_ENDPOINT = "planMove";
    private static final String PLAN_MOVES_FLEET_ENDPOINT = "planMoves";
    private static final String CANCEL_MOVE_FLEET_ENDPOINT = "cancelMove";
    private static final String FLEET_PER_USER_PER_SYSTEM_ENDPOINT = "fleetDistribution";
    private static final String INTERSTELLAR_MOVEMENT_ENDPOINT = "interstellarMovement";

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final StarSystemService starSystemService;

    @Nonnull
    private final UserService userService;

    @Autowired
    public FleetApi(@Nonnull final FleetService fleetService,
                    @Nonnull final UserService userService,
                    @Nonnull final StarSystemService starSystemService) {
        Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");

        this.fleetService = fleetService;
        this.userService = userService;
        this.starSystemService = starSystemService;
    }

    @GetMapping(value = FLEET_PER_SYSTEM_ENDPOINT + "/{idStarSystem}")
    @ApiOperation(value = "Get all fleets inside of a star system.", nickname = "getFleetsBySystem")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FleetList.class))),
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
        return ResponseEntity.ok(new FleetList(fleets));
    }

    @GetMapping(value = INTERSTELLAR_MOVEMENT_ENDPOINT)
    @ApiOperation(value = "Get all fleets inside of a star system.", nickname = "getInterstellarMovingFleets")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FleetList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getInterstellarMovingFleets() {

        final List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> fleets = fleetService.findAllFleetsWithInterstellarMovement();
        return ResponseEntity.ok(new FleetList(fleets));
    }

    @GetMapping(value = FLEET_PER_SYSTEM_ENDPOINT + "/{idStarSystem}/" + FLEET_PER_USER_ENDPOINT + "/{idOwner}")
    @ApiOperation(value = "Get all fleets inside of a star system for a specific user.", nickname = "getFleetsBySystemAndOwner")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FleetList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleetsBySystemAndOwner(@PathVariable("idStarSystem") final int idStarSystem,
                                                       @PathVariable("idOwner") final int idOwner) {

        final List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> fleets = fleetService.findAllFleetsBy(idStarSystem, idOwner);
        return ResponseEntity.ok(new FleetList(fleets));
    }

    @GetMapping(value = FLEET_PER_USER_PER_SYSTEM_ENDPOINT)
    @ApiOperation(value = "Get all the star systems which are holding fleets with the fleet's owner.", nickname = "getFleetDistribution")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FleetDistributionPerUserList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleetDistribution() {

        final List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> allFleets = fleetService.findAllFleetsWithoutMovement();
        final Map<StarSystem, Set<User>> userBySystem = allFleets
                .stream()
                .filter(fleet -> fleet.getOrbit() != null)
                .filter(de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet::isFTLCapable)
                .collect(Collectors.groupingBy(fleet -> {
                            assert fleet.getOrbit() != null;
                            return fleet.getOrbit().getSystem();
                        },
                        Collectors.mapping(de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet::getOwner, Collectors.toSet())));

        final List<FleetDistributionPerUser> result = userBySystem.entrySet().stream()
                .map(FleetDistributionPerUser::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = FLEET_PER_USER_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all fleets of an owner.", nickname = "getFleetsForUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FleetList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleetsForUser(@PathVariable("idUser") final int idUser) {

        final User user = userService.find(idUser);
        if (user != null) {
            final List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> allFleetsByUser = fleetService.findAllFleetsByUser(user);
            return ResponseEntity.ok(new FleetList(allFleetsByUser));
        }
        throw new NotifyWebUserException("No user found.");
    }

    @PostMapping(value = MERGE_FLEET_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Merge two fleets of an owner.", nickname = "mergeFleets")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Fleet.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> mergeFleets(@PathVariable("idUser") final int idUser, @RequestBody FleetMerge merge) {

        final List<Integer> fleetIDs = List.of(merge.getIdFleetToMerge(), merge.getIdFleetMergeTarget());
        final List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> fleetList = fleetService.findByIds(fleetIDs);
        fleetList.stream().filter(fleet -> fleet.getOwner().getId() != idUser).findAny().ifPresent(fleet -> {
            throw new NotifyWebUserException("You cannot do that.");
        });

        final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleetSubject = fleetList.stream().filter(fleet -> fleet.getId() == merge.getIdFleetMergeTarget()).findFirst().orElseThrow(() -> {
            throw new NotifyWebUserException("You cannot do that.");
        });
        final Set<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> hashSet = fleetList.stream().filter(fl -> fl.getId() != merge.getIdFleetMergeTarget()).collect(Collectors.toSet());
        final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = fleetService.mergeFleets(fleetSubject, hashSet);

        return ResponseEntity.ok(new Fleet(fleet));
    }

    @PostMapping(value = MOVE_FLEET_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Moves a fleet to another celestial.", nickname = "moveFleet")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Fleet.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> moveFleet(@PathVariable("idUser") final int idUser, @RequestBody FleetMove move) {

        // todo validate interstellar flights with propulsion
        final de.yuga.spacebattle.backend.entities.turn.Move m = createSingleMove(idUser, move);
        final List<de.yuga.spacebattle.backend.entities.turn.Move> plannedMoves = new ArrayList<>();
        plannedMoves.add(m);
        List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> inMotion = fleetService.moveFleets(plannedMoves);
        return ResponseEntity.ok(new Fleet(inMotion.get(0)));
    }

    @PostMapping(value = MOVE_FLEETS_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Moves a fleet to another celestial.", nickname = "moveFleets")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FleetList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> moveFleets(@PathVariable("idUser") final int idUser, @RequestBody @Nonnull final List<FleetMove> moves) {
        PreconditionWebHelper.checkNotNull(moves, "moves shouldn't be null!");

        // todo validate interstellar flights with propulsion
        final List<de.yuga.spacebattle.backend.entities.turn.Move> plannedMoves = getMultiMove(idUser, moves);
        final List<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> inMotion = fleetService.moveFleets(plannedMoves);
        return ResponseEntity.ok(new FleetList(inMotion));
    }

    @PostMapping(value = PLAN_MOVE_FLEET_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Plan a movement of a fleet to another celestial.", nickname = "planMovement")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Move.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> planMovement(@PathVariable("idUser") final int idUser, @RequestBody FleetMove move) {

        // todo validate interstellar flights with propulsion
        final de.yuga.spacebattle.backend.entities.turn.Move m = createSingleMove(idUser, move);
        return ResponseEntity.ok(new Move(m));
    }

    @PostMapping(value = PLAN_MOVES_FLEET_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Plan a movement of a fleet to another celestial.", nickname = "planMovements")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MoveList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> planMovements(@PathVariable("idUser") final int idUser, @RequestBody @Nonnull final List<FleetMove> moves) {
        PreconditionWebHelper.checkNotNull(moves, "moves shouldn't be null!");

        // todo validate interstellar flights with propulsion
        final List<de.yuga.spacebattle.backend.entities.turn.Move> plannedMoves = getMultiMove(idUser, moves);
        return ResponseEntity.ok(new MoveList(plannedMoves));
    }

    @PutMapping(value = CANCEL_MOVE_FLEET_ENDPOINT + "/{idUser}/{idFleet}")
    @ApiOperation(value = "Cancels a movement of a fleet and creates the way back.", nickname = "cancelMovement")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Fleet.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFleetsForUser(@PathVariable("idUser") final int idUser, @PathVariable("idFleet") final int idFleet) {
        final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet cancelFlight = fleetService.cancelFlight(idUser, idFleet);
        return ResponseEntity.ok(new Fleet(cancelFlight));
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

        final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = fleetService.findById(move.getIdFleetToMove());
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
