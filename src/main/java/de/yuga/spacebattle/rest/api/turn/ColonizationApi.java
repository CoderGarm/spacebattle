package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystem;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystemList;
import de.yuga.spacebattle.rest.dto.turn.Colonization;
import de.yuga.spacebattle.rest.dto.turn.StarSystemColonizationList;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Api(tags = "ColonizationApi")
@RolesAllowed("ROLE_USER")
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + ColonizationApi.ENDPOINT + "/")
public class ColonizationApi {

    @Nonnull
    public static final String ENDPOINT = "colonization";
    private static final String FREE_SYSTEMS_ENDPOINT = "freeSystems";
    private static final String KNOWN_SYSTEMS_ENDPOINT = "knownSystems";
    private static final String ALL_SYSTEMS_ENDPOINT = "all";
    private static final String ALL_PENDING_COLONIZATIONS_ENDPOINT = "pendingColonizations";
    private static final String HOME_SYSTEM_ENDPOINT = "home";
    private static final String BUY_SYSTEM_INFO_ENDPOINT = "buy";

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final StarSystemService starSystemService;

    @Nonnull
    private final PlanetService planetService;

    @Autowired
    public ColonizationApi(@Nonnull final UserService userService,
                           @Nonnull final ColonizationService colonizationService,
                           @Nonnull final StarSystemService starSystemService,
                           @Nonnull final PlanetService planetService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");
        Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");

        this.userService = userService;
        this.colonizationService = colonizationService;
        this.starSystemService = starSystemService;
        this.planetService = planetService;
    }

    @PutMapping(value = "/{idUser}")
    @ApiOperation(value = "Starts the colonization of a planet for a user.", nickname = "startColonizingPlanet")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Colonization.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> startColonizingPlanet(@PathVariable("idUser") final int idUser, @RequestBody de.yuga.spacebattle.rest.dto.orbitals.Planet planet) {
        PreconditionWebHelper.checkNotNull(planet, "There must be a planet to colonize.");

        final Planet p = planetService.find(planet.getIdPlanet());
        if (p == null) {
            throw new NotifyWebUserException("There must be a planet to colonize, as I said.");
        }
        final User user = userService.getWithKnownStarSystems(idUser);
        if (user == null) {
            throw new NotifyWebUserException("There must be a user who buys the info.");
        }
        if (!user.getKnownStarSystems().contains(p.getSystem())) {
            throw new NotifyWebUserException("You can't colonize an unknown systems.");
        }

        final de.yuga.spacebattle.backend.entities.turn.Colonization colonization = colonizationService.startColonizingPlanet(user, p);
        return ResponseEntity.ok(new Colonization(colonization));
    }

    @PostMapping(value = BUY_SYSTEM_INFO_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all not colonized but known systems for a user.", nickname = "buyInformationForSystem")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> buyInformationForSystem(@PathVariable("idUser") final int idUser, @RequestBody StarSystem starSystem) {
        PreconditionWebHelper.checkNotNull(starSystem, "There must be a system to buy infos for.");

        final User user = userService.find(idUser);
        if (user == null) {
            throw new NotifyWebUserException("There must be a user who buys the info.");
        }
        final de.yuga.spacebattle.backend.entities.orbitals.StarSystem system = starSystemService.find(starSystem.getIdStarSystem());
        if (system == null) {
            throw new NotifyWebUserException("There must be a system, really.");
        }
        colonizationService.addToKnownSystems(user, system);
        return ResponseEntity.ok(true);
    }

    @GetMapping(value = HOME_SYSTEM_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all not colonized but known systems for a user.", nickname = "getHomeSystem")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StarSystem.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getHomeSystem(@PathVariable("idUser") final int idUser) {

        final Planet mainPlanet = planetService.findMainPlanet(idUser);
        return ResponseEntity.ok(new StarSystem(mainPlanet.getSystem()));
    }

    @GetMapping(value = ALL_SYSTEMS_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all colonizable systems for a user with their distances to all known systems.", nickname = "getColonizationStarSystemsForUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StarSystemColonizationList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getColonizationStarSystemsForUser(@PathVariable("idUser") final int idUser) {

        final List<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> all = starSystemService.findAllUncolonized();
        final Set<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownStarSystems = userService.getKnownStarSystems(idUser);
        final List<de.yuga.spacebattle.backend.entities.turn.Colonization> colonizationsForUser = colonizationService.findAllForUser(idUser);
        return ResponseEntity.ok(new StarSystemColonizationList(all, knownStarSystems, colonizationsForUser));
    }

    @GetMapping(value = ALL_PENDING_COLONIZATIONS_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all pending colonizations for the user with their distances to all known systems.", nickname = "getPendingColonizationsForUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StarSystemColonizationList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPendingColonizationsForUser(@PathVariable("idUser") final int idUser) {

        final Set<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownStarSystems = userService.getKnownStarSystems(idUser);
        final List<de.yuga.spacebattle.backend.entities.turn.Colonization> colonizationsForUser = colonizationService.findAllForUser(idUser);
        final List<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> all = colonizationsForUser.stream()
                .map(c -> c.getTarget().getSystem())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new StarSystemColonizationList(all, knownStarSystems, colonizationsForUser));
    }

    @GetMapping(value = FREE_SYSTEMS_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all not colonized but known systems for a user.", nickname = "getUnknownStarSystemsForUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StarSystemList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getUnknownStarSystemsForUser(@PathVariable("idUser") final int idUser) {

        final List<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> all = starSystemService.findAll();
        final Set<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownStarSystems = userService.getKnownStarSystems(idUser);
        all.removeAll(knownStarSystems);
        return ResponseEntity.ok(new StarSystemList(all));
    }

    @GetMapping(value = KNOWN_SYSTEMS_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all not known systems for a user.", nickname = "getKnownStarSystemsForUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StarSystemList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getKnownStarSystemsForUser(@PathVariable("idUser") final int idUser) {

        final Set<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownStarSystems = userService.getKnownStarSystems(idUser);
        return ResponseEntity.ok(new StarSystemList(knownStarSystems));
    }
}
