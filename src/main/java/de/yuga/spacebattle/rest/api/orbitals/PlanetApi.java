package de.yuga.spacebattle.rest.api.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.ShipyardConstructionOrder;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.ShipyardConstructionSelection;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "PlanetApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + PlanetApi.ENDPOINT + "/")
public class PlanetApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "planet";
    private static final String GROUND_CONSTRUCTION_POSSIBLE_ENDPOINT = "groundConstructionPossible";
    private static final String GROUND_BUILD_IT_ENDPOINT = "groundConstructionBuild";
    private static final String SHIPYARD_POSSIBLE_ENDPOINT = "shipyardConstructionPossible";
    private static final String SHIPYARD_BUILD_IT_ENDPOINT = "shipyardConstructionBuild";
    private static final String GET_PLANET_BY_COORDINATES_ENDPOINT = "byCoord";
    private static final String GET_MAIN_PLANET = "main";

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final ShipClassService shipClassService;

    @Autowired
    public PlanetApi(@Nonnull final PlanetService planetService,
                     @Nonnull final JobService jobService,
                     @Nonnull final ShipClassService shipClassService) {
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");

        this.planetService = planetService;
        this.jobService = jobService;
        this.shipClassService = shipClassService;
    }

    @GetMapping(value = "{idUser}")
    @Operation(summary = "Get all planets which are colonized by a user.", operationId = "getPlanetByUsers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = Planet.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPlanets(@PathVariable("idUser") final int idUser) {
        final List<de.yuga.spacebattle.backend.entities.orbitals.Planet> all = planetService.findAllColonizedBy(idUser);
        final List<Planet> planets = all.stream().map(Planet::new).collect(Collectors.toList());
        return ResponseEntity.ok(planets);
    }

    @GetMapping(value = GET_MAIN_PLANET)
    @Operation(summary = "Get the main planet of a user.", operationId = "getMainPlanet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Planet.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getMainPlanet() {
        final int idUser = getIdUser();
        final de.yuga.spacebattle.backend.entities.orbitals.Planet mainPlanet = planetService.findMainPlanet(idUser);
        return ResponseEntity.ok(new Planet(mainPlanet));
    }

    @GetMapping(value = GROUND_CONSTRUCTION_POSSIBLE_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Asks if a building could be build on this planet.", operationId = "isConstructionPossibleOnPlanet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "you can build something or not",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> isConstructionPossible(@PathVariable("idPlanet") final int idPlanet) {
        final de.yuga.spacebattle.backend.entities.orbitals.Planet planet = planetService.find(idPlanet);
        if (planet == null) {
            return ResponseEntity.ok(false);
        }
        final boolean buildingPossible = planet.isConstructionPossible();
        return ResponseEntity.ok(buildingPossible);
    }

    @GetMapping(value = GROUND_BUILD_IT_ENDPOINT + "/{idPlanet}/{idBuilding}")
    @Operation(summary = "Starts a construction on this planet.", operationId = "buildConstruction",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully started",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> buildConstruction(@PathVariable("idPlanet") final int idPlanet, @PathVariable("idBuilding") final int idBuilding) {
        final Job job = jobService.createConstructionYardJob(idPlanet, idBuilding);
        return ResponseEntity.ok(job != null);
    }

    @GetMapping(value = SHIPYARD_POSSIBLE_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Asks if a ship could be build on this planet.", operationId = "isShipyardJobPossibleOnPlanet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "you can build something or not",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> isShipyardJobPossibleOnPlanet(@PathVariable("idPlanet") final int idPlanet) {
        final de.yuga.spacebattle.backend.entities.orbitals.Planet planet = planetService.find(idPlanet);
        if (planet == null) {
            return ResponseEntity.ok(false);
        }
        final boolean buildingPossible = planet.getConstructionByResource(EResourceType.ORBITAL_CONSTRUCTION).stream().anyMatch(c -> c.getJobs().isEmpty());
        return ResponseEntity.ok(buildingPossible);
    }

    @PostMapping(value = SHIPYARD_BUILD_IT_ENDPOINT)
    @Operation(summary = "Starts a construction on this planet.", operationId = "buildShip",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ShipyardConstructionOrder.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully started",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> buildShip(@RequestBody final ShipyardConstructionOrder shipyardConstructionOrder) {
        Preconditions.checkNotNull(shipyardConstructionOrder, "shipyardConstructionOrder shouldn't be null!");

        final int idPlanet = shipyardConstructionOrder.getIdPlanet();
        final de.yuga.spacebattle.backend.entities.orbitals.Planet planet = planetService.find(idPlanet);
        if (planet == null) {
            throw new NotifyWebUserException("There is no planet, sorry");
        }
        final List<ShipyardConstructionSelection> shipJobPayload = shipyardConstructionOrder.getShipJobPayload();
        final List<Integer> idShipClasses = shipJobPayload.stream().map(ShipyardConstructionSelection::getIdShipClass).collect(Collectors.toList());

        final List<ShipClass> foundClasses = shipClassService.find(idShipClasses);
        final Map<Integer, ShipClass> foundClassesByID = foundClasses.stream().collect(Collectors.toMap(AbstractEntityKey::getId, sc -> sc));

        final Map<ShipClass, Integer> jobLoad = shipJobPayload.stream()
                .collect(Collectors.toMap(entry -> foundClassesByID.get(entry.getIdShipClass()), ShipyardConstructionSelection::getAmount));

        final Set<Job> shipyardJobs = jobService.createShipyardJob(planet, jobLoad);
        return ResponseEntity.ok(!shipyardJobs.isEmpty());
    }

    @PostMapping(value = GET_PLANET_BY_COORDINATES_ENDPOINT + "/{idStarSystem}")
    @Operation(summary = "Gets a planet which is matching to the given coordinates.", operationId = "getPlanetByCoordinates",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Orbit.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully started",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Planet.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPlanetByCoordinates(@RequestBody @Nonnull final Orbit orbit, @PathVariable("idStarSystem") final int idStarSystem) {
        PreconditionWebHelper.checkNotNull(orbit, "The given orbit shouldn't be empty!");

        final de.yuga.spacebattle.backend.entities.orbitals.Planet planet = planetService.findByCoordinates(idStarSystem, orbit.getXCoordinate(), orbit.getYCoordinate());
        if (planet != null) {
            return ResponseEntity.ok(new Planet(planet));
        }
        return ResponseEntity.ok().build();

    }
}
