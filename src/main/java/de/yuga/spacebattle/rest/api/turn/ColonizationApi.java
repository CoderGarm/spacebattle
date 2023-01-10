package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystem;
import de.yuga.spacebattle.rest.dto.turn.Colonization;
import de.yuga.spacebattle.rest.dto.turn.StarSystemColonization;
import de.yuga.spacebattle.rest.dto.turn.StarSystemColonizationListConverter;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "ColonizationApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + ColonizationApi.ENDPOINT + "/")
public class ColonizationApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "colonization";
    private static final String FREE_SYSTEMS_ENDPOINT = "freeSystems";
    private static final String KNOWN_SYSTEMS_ENDPOINT = "knownSystems";
    private static final String ALL_SYSTEMS_ENDPOINT = "all";
    private static final String ALL_PENDING_COLONIZATIONS_ENDPOINT = "pendingColonizations";
    private static final String HOME_SYSTEM_ENDPOINT = "home";
    private static final String BUY_SYSTEM_INFO_ENDPOINT = "buy";
    private static final String COSTS_SYSTEM_INFO_ENDPOINT = "costs";

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
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.colonizationService = Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");
        this.starSystemService = Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
    }

    @PutMapping
    @Operation(summary = "Starts the colonization of a planet for a user.", operationId = "startColonizingPlanet",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.orbitals.Planet.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Colonization.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> startColonizingPlanet(@RequestBody @Nonnull final de.yuga.spacebattle.rest.dto.orbitals.Planet planet) {
        PreconditionWebHelper.checkNotNull(planet, "There must be a planet to colonize.");

        final int idUser = getIdUser();
        final Planet p = planetService.find(planet.getIdPlanet());
        if (p == null) {
            throw new NotifyWebUserException("There must be a planet to colonize, as I said.");
        }
        final User user = userService.findWithKnownStarSystems(idUser);
        if (user == null) {
            throw new NotifyWebUserException("There must be a user who buys the info.");
        }
        if (!user.getKnownStarSystems().contains(p.getSystem())) {
            throw new NotifyWebUserException("You can't colonize an unknown systems.");
        }

        final de.yuga.spacebattle.backend.entities.turn.Colonization colonization = colonizationService.startColonizingPlanet(user, p);
        return ResponseEntity.ok(new Colonization(colonization));
    }

    @PostMapping(value = BUY_SYSTEM_INFO_ENDPOINT)
    @Operation(summary = "Get all not colonized but known systems for a user.", operationId = "buyInformationForSystem",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StarSystem.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StarSystemColonization.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> buyInformationForSystem(@RequestBody StarSystem starSystem) {
        PreconditionWebHelper.checkNotNull(starSystem, "There must be a system to buy infos for.");

        final int idUser = getIdUser();
        final User user = userService.find(idUser);
        if (user == null) {
            throw new NotifyWebUserException("There must be a user who buys the info.");
        }
        final de.yuga.spacebattle.backend.entities.orbitals.StarSystem system = starSystemService.find(starSystem.getIdStarSystem());
        if (system == null) {
            throw new NotifyWebUserException("There must be a system, really.");
        }
        colonizationService.addToKnownSystems(user, system);
        final Set<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownStarSystems = userService.getKnownStarSystems(idUser);
        final List<de.yuga.spacebattle.backend.entities.turn.Colonization> colonizationsForUser = colonizationService.findAllForUser(idUser);
        final StarSystemColonization result = new StarSystemColonization(system, knownStarSystems, colonizationsForUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = HOME_SYSTEM_ENDPOINT)
    @Operation(summary = "Get all not colonized but known systems for a user.", operationId = "getHomeSystem",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StarSystem.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getHomeSystem() {
        final int idUser = getIdUser();
        final Planet mainPlanet = planetService.findMainPlanet(idUser);
        return ResponseEntity.ok(new StarSystem(mainPlanet.getSystem()));
    }

    @GetMapping(value = COSTS_SYSTEM_INFO_ENDPOINT)
    @Operation(summary = "Get the costs to colonize the targeted planet.", operationId = "getColonizationCosts",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.turn.resources.ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getColonizationCosts(@RequestBody final de.yuga.spacebattle.rest.dto.orbitals.Planet planet) {

        final Planet toColonize = planetService.find(planet.getIdPlanet());
        PreconditionWebHelper.checkNotNull(toColonize, "toColonize must not be empty");

        final ResourceDeposit resourceDeposit = ColonizationCostCalculator.getColonizationCosts(toColonize);
        return ResponseEntity.ok(new de.yuga.spacebattle.rest.dto.turn.resources.ResourceDeposit(resourceDeposit));
    }

    @GetMapping(value = ALL_SYSTEMS_ENDPOINT)
    @Operation(summary = "Get all colonizable systems for a user with their distances to all known systems.", operationId = "getColonizationStarSystemsForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = StarSystemColonization.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getColonizationStarSystemsForUser() {

        // todo response to big
        final int idUser = getIdUser();
        final List<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> all = starSystemService.findAllColonizable();
        final Set<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownStarSystems = userService.getKnownStarSystems(idUser);
        final List<de.yuga.spacebattle.backend.entities.turn.Colonization> colonizationsForUser = colonizationService.findAllForUser(idUser);
        return ResponseEntity.ok(StarSystemColonizationListConverter.create(all, knownStarSystems, colonizationsForUser));
    }

    @GetMapping(value = ALL_PENDING_COLONIZATIONS_ENDPOINT)
    @Operation(summary = "Get all pending colonizations for the user with their distances to all known systems.", operationId = "getPendingColonizationsForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = StarSystemColonization.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPendingColonizationsForUser() {

        final int idUser = getIdUser();
        final Set<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownStarSystems = userService.getKnownStarSystems(idUser);
        final List<de.yuga.spacebattle.backend.entities.turn.Colonization> colonizationsForUser = colonizationService.findAllForUser(idUser);
        final Set<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> all = colonizationsForUser.stream()
                .map(c -> c.getTarget().getSystem())
                .collect(Collectors.toSet());
        return ResponseEntity.ok(StarSystemColonizationListConverter.create(all, knownStarSystems, colonizationsForUser));
    }

    @GetMapping(value = FREE_SYSTEMS_ENDPOINT)
    @Operation(summary = "Get all not colonized but known systems for a user.", operationId = "getUnknownStarSystemsForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = StarSystem.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getUnknownStarSystemsForUser() {

        final int idUser = getIdUser();
        final List<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> all = starSystemService.findAll();
        final Set<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownStarSystems = userService.getKnownStarSystems(idUser);
        all.removeAll(knownStarSystems);
        final List<StarSystem> starSystems = all.stream().map(StarSystem::new).collect(Collectors.toList());
        return ResponseEntity.ok(starSystems);
    }

    @GetMapping(value = KNOWN_SYSTEMS_ENDPOINT)
    @Operation(summary = "Get all not known systems for a user.", operationId = "getKnownStarSystemsForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = StarSystem.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getKnownStarSystemsForUser() {
        final int idUser = getIdUser();
        final Set<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> knownStarSystems = userService.getKnownStarSystems(idUser);
        final List<StarSystem> starSystems = knownStarSystems.stream().map(StarSystem::new).collect(Collectors.toList());
        return ResponseEntity.ok(starSystems);
    }
}
