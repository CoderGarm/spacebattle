package de.yuga.spacebattle.rest.api.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import de.yuga.spacebattle.backend.services.turn.tick.PlanetTickRunner;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.ShipyardConstructionOrder;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.ShipyardConstructionSelection;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
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
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private static final String SHIPYARD_EXISTS_ENDPOINT = "shipyardExists";
    private static final String SHIPYARD_BUILD_IT_ENDPOINT = "shipyardConstructionBuild";
    private static final String GET_PLANET_BY_COORDINATES_ENDPOINT = "byCoord";
    private static final String GET_MAIN_PLANET = "main";
    private static final String REPAIR_FLEET_ENDPOINT = SHIPYARD_BUILD_IT_ENDPOINT + "/repair";
    private static final String UPGRADE_FLEET_ENDPOINT = SHIPYARD_BUILD_IT_ENDPOINT + "/upgrade";
    private static final String TRANSPORTATION_ENDPOINT = "transportation";

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final ShipClassService shipClassService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final PlanetTickRunner planetTickRunner;

    @Nonnull
    private final TickTimeService tickTimeService;

    @Nonnull
    private final ConstructionService constructionService;

    @Autowired
    public PlanetApi(@Nonnull final PlanetService planetService,
                     @Nonnull final JobService jobService,
                     @Nonnull final ShipClassService shipClassService,
                     @Nonnull final FleetService fleetService,
                     @Nonnull final PlanetTickRunner planetTickRunner,
                     @Nonnull final TickTimeService tickTimeService,
                     @Nonnull final ConstructionService constructionService) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.jobService = Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        this.shipClassService = Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.planetTickRunner = Preconditions.checkNotNull(planetTickRunner, "planetTickRunner must not be empty");
        this.tickTimeService = Preconditions.checkNotNull(tickTimeService, "tickTimeService must not be empty");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService must not be empty");
    }

    @GetMapping
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
    public ResponseEntity<?> getPlanets() {
        final int idUser = getIdUser();
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

    @GetMapping(value = GET_MAIN_PLANET + "/coords")
    @Operation(summary = "Get the main planet of a user.", operationId = "getMainPlanetCoords",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getMainPlanetCoords() {
        final int idUser = getIdUser();
        final de.yuga.spacebattle.backend.entities.orbitals.Planet mainPlanet = planetService.findMainPlanet(idUser);
        return ResponseEntity.ok(new de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit(new FleetOrbit(mainPlanet.getOrbit(), mainPlanet.getSystem())));
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
        final boolean buildingPossible = constructionService.isStandardJobForTargetPossibleAtPlanet(idPlanet, EResourceType.CONSTRUCTION);
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
        if (JobService.isLocalInstaJobPossible(job.getFacility().getPlanet(), job)) {
            planetTickRunner.tickInstaConstruction(job, tickTimeService.getToday());
        }
        return ResponseEntity.ok(true);
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
        final boolean buildingPossible = constructionService.isStandardJobForTargetPossibleAtPlanet(idPlanet, EResourceType.ORBITAL_CONSTRUCTION);
        return ResponseEntity.ok(buildingPossible);
    }

    @GetMapping(value = SHIPYARD_EXISTS_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Asks if a ship could be build on this planet.", operationId = "isShipyardExistsOnPlanet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "you can build something or not",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> isShipyardExistsOnPlanet(@PathVariable("idPlanet") final int idPlanet) {
        final de.yuga.spacebattle.backend.entities.orbitals.Planet planet = planetService.find(idPlanet);
        if (planet == null) {
            return ResponseEntity.ok(false);
        }
        final boolean exists = constructionService.hasPlanetProductionForTarget(idPlanet, EResourceType.ORBITAL_CONSTRUCTION);
        return ResponseEntity.ok(exists);
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

        /* fixme reduce to idplanet */
        final Job job = jobService.createShipyardJob(planet, jobLoad);
        if (JobService.isLocalInstaJobPossible(planet, job)) {
            planetTickRunner.tickInstaShipyard(job, tickTimeService.getToday());
        }
        return ResponseEntity.ok(true);
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

    @PostMapping(value = REPAIR_FLEET_ENDPOINT + "/{idFleet}")
    @Operation(summary = "Repairs the fleet.", operationId = "repairFleets",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> repairFleets(@PathVariable("idFleet") final int idFleet) {

        final FleetPlanetDto fleetPlanetDto = getResult(idFleet);

        final Job job = jobService.startShipyardRepairJob(fleetPlanetDto.planet, fleetPlanetDto.fleet);
        if (JobService.isLocalInstaJobPossible(fleetPlanetDto.planet, job)) {
            planetTickRunner.tickInstaShipyard(job, tickTimeService.getToday());
        }
        return ResponseEntity.ok(true);
    }

    @Nonnull
    private FleetPlanetDto getResult(final int idFleet) {
        final int idUser = getIdUser();
        final Fleet fleet = fleetService.find(idFleet);
        PreconditionWebHelper.checkNotNull(fleet, "fleet must not be empty");
        final de.yuga.spacebattle.backend.entities.orbitals.Planet planet = getPlanetOrbitedByFleet(fleet);

        if (planet == null || planet.getOwner() == null || (planet.getOwner().getId() != idUser || fleet.getOwner().getId() != idUser)) {
            throw new NotifyWebUserException("This will not work that way.");
        }
        return new FleetPlanetDto(fleet, planet);
    }

    private static class FleetPlanetDto {
        public final Fleet fleet;
        public final de.yuga.spacebattle.backend.entities.orbitals.Planet planet;

        public FleetPlanetDto(final Fleet fleet, final de.yuga.spacebattle.backend.entities.orbitals.Planet planet) {
            this.fleet = fleet;
            this.planet = planet;
        }
    }

    @PostMapping(value = UPGRADE_FLEET_ENDPOINT + "/{idFleet}")
    @Operation(summary = "Upgrades the fleet to the last successor classes.", operationId = "upgradeFleets",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> upgradeFleets(@PathVariable("idFleet") final int idFleet) {


        final FleetPlanetDto fleetPlanetDto = getResult(idFleet);
        final Job job = jobService.createShipyardUpgradeJob(fleetPlanetDto.planet, fleetPlanetDto.fleet);
        if (JobService.isLocalInstaJobPossible(fleetPlanetDto.planet, job)) {
            planetTickRunner.tickInstaShipyard(job, tickTimeService.getToday());
        }
        return ResponseEntity.ok(true);
    }

    @Nullable
    private de.yuga.spacebattle.backend.entities.orbitals.Planet getPlanetOrbitedByFleet(@Nonnull final Fleet fleet) {
        PreconditionWebHelper.checkNotNull(fleet, "fleet must not be empty");

        final FleetOrbit orbit = fleet.getOrbit();
        PreconditionWebHelper.checkNotNull(orbit, "orbit must not be empty");

        final StarSystem system = orbit.getSystem();
        PreconditionWebHelper.checkNotNull(system, "system must not be empty");

        final de.yuga.spacebattle.backend.entities.orbitals.Orbit planetaryOrbit = orbit.getOrbit();
        PreconditionWebHelper.checkNotNull(planetaryOrbit, "planetaryOrbit must not be empty");

        final Map<de.yuga.spacebattle.backend.entities.orbitals.Orbit, de.yuga.spacebattle.backend.entities.orbitals.Planet> planetsByOrbit = system.getPlanets().stream()
                .collect(Collectors.toMap(de.yuga.spacebattle.backend.entities.orbitals.Planet::getOrbit, Function.identity()));

        final de.yuga.spacebattle.backend.entities.orbitals.Orbit foundPlanetaryOrbit = planetsByOrbit.keySet().stream()
                .filter(o -> o.compareTo(planetaryOrbit) == 0)
                .findFirst()
                .orElseThrow(() -> new NotifyWebUserException("It would be pretty good if there is a planet."));

        return planetsByOrbit.get(foundPlanetaryOrbit);
    }

    @GetMapping(value = TRANSPORTATION_ENDPOINT + "/demand/{idPlanet}")
    @Operation(summary = "Returns the transportation need of this planet.", operationId = "getTransportationDemand",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully started",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getTransportationDemand(@PathVariable("idPlanet") final int idPlanet) {
        final de.yuga.spacebattle.backend.entities.orbitals.Planet planet = planetService.find(idPlanet);
        if (planet == null || planet.getOwner() == null || planet.getOwner().getId() != getIdUser()) {
            throw new NotifyWebUserException("Well, no.");
        }
        return ResponseEntity.ok(new ResourceDeposit(planet.getResourceTransportationDemand()));
    }

    @PutMapping(value = TRANSPORTATION_ENDPOINT + "/demand/{idPlanet}")
    @Operation(summary = "Sets the transportation demand.", operationId = "setTransportationDemand",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResourceDeposit.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully started",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> setTransportationDemand(@PathVariable("idPlanet") final int idPlanet,
                                                     @RequestBody @Nonnull final ResourceDeposit demand) {
        de.yuga.spacebattle.backend.entities.orbitals.Planet planet = planetService.find(idPlanet);
        if (planet == null || planet.getOwner() == null || planet.getOwner().getId() != getIdUser()) {
            throw new NotifyWebUserException("Well, no.");
        }

        final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit planetaryDemand = planet.getResourceTransportationDemand();
        updateResourceTransportation(demand, planetaryDemand);
        planet = planetService.save(planet);
        return ResponseEntity.ok(new ResourceDeposit(planet.getResourceTransportationDemand()));
    }

    @GetMapping(value = TRANSPORTATION_ENDPOINT + "/delivery/{idPlanet}")
    @Operation(summary = "Returns the transportation delivery of this planet.", operationId = "getTransportationDelivery",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully started",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getTransportationDelivery(@PathVariable("idPlanet") final int idPlanet) {
        final de.yuga.spacebattle.backend.entities.orbitals.Planet planet = planetService.find(idPlanet);
        if (planet == null || planet.getOwner() == null || planet.getOwner().getId() != getIdUser()) {
            throw new NotifyWebUserException("Well, no.");
        }
        return ResponseEntity.ok(new ResourceDeposit(planet.getResourceTransportationDelivery()));
    }

    @PutMapping(value = TRANSPORTATION_ENDPOINT + "/delivery/{idPlanet}")
    @Operation(summary = "Sets the transportation delivery.", operationId = "setTransportationDelivery",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResourceDeposit.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully started",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceDeposit.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> setTransportationDelivery(@PathVariable("idPlanet") final int idPlanet,
                                                       @RequestBody @Nonnull final ResourceDeposit delivery) {
        de.yuga.spacebattle.backend.entities.orbitals.Planet planet = planetService.find(idPlanet);
        if (planet == null || planet.getOwner() == null || planet.getOwner().getId() != getIdUser()) {
            throw new NotifyWebUserException("Well, no.");
        }

        final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit planetaryDelivery = planet.getResourceTransportationDelivery();
        updateResourceTransportation(delivery, planetaryDelivery);
        planet = planetService.save(planet);
        return ResponseEntity.ok(new ResourceDeposit(planet.getResourceTransportationDelivery()));
    }

    private void updateResourceTransportation(@Nonnull final ResourceDeposit data,
                                              @Nonnull final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit toUpdate) {
        Preconditions.checkNotNull(data, "data must not be empty");
        Preconditions.checkNotNull(toUpdate, "toUpdate must not be empty");

        data.getResources().forEach(res -> {
            final EResourceType realResourceType = res.getRealType();
            toUpdate.setAbsoluteResourceValue(realResourceType, res.getAmount());
        });
        data.getHumanResources().forEach(res -> {
            final EEducationType realResourceType = res.getRealType();
            toUpdate.setAbsoluteCrewRequirement(realResourceType, res.getAmount());
        });
    }
}
