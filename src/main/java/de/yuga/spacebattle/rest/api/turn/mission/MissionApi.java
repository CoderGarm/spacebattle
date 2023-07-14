package de.yuga.spacebattle.rest.api.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.turn.mission.MissionService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.mission.Mission;
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

@Tag(name = "MissionApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + MissionApi.ENDPOINT + "/")
public class MissionApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "mission";

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final StarSystemService starSystemService;

    @Nonnull
    private final MissionService missionService;

    @Nonnull
    private final PlanetService planetService;
    private final UserService userService;

    @Autowired
    public MissionApi(@Nonnull final FleetService fleetService,
                      @Nonnull final MissionService missionService,
                      @Nonnull final StarSystemService starSystemService,
                      @Nonnull final PlanetService planetService, final UserService userService) {
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.missionService = Preconditions.checkNotNull(missionService, "missionService shouldn't be null!");
        this.starSystemService = Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.userService = userService;
    }

    @GetMapping()
    @Operation(summary = "Returns all running missions.", operationId = "getMissions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Mission.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getMissions() {

        final List<de.yuga.spacebattle.backend.entities.turn.mission.Mission> missions = missionService.findAllMissions(getIdUser());
        return ResponseEntity.ok(missions.stream().map(m -> new Mission(m, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @PutMapping()
    @Operation(summary = "Creates a mission", operationId = "setupMission",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Mission.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Mission.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> setupMission(@RequestBody @Nonnull final Mission mission) {

        final Set<Integer> warshipIDs = mission.getWarShipIDs();
        if (mission.getIdMission() != null) {
            final de.yuga.spacebattle.backend.entities.turn.mission.Mission result = missionService.updateMission(mission.getIdMission(), warshipIDs);
            Preconditions.checkNotNull(result, "result must not be empty");
            return ResponseEntity.ok(new Mission(result, getPreferredLanguage()));
        }

        final User actor = userService.find(getIdUser());
        Preconditions.checkNotNull(actor, "actor must not be empty");


        final Planet planet = planetService.find(mission.getVenue().getIdPlanet());
        Preconditions.checkNotNull(planet, "planet must not be empty");
        final de.yuga.spacebattle.backend.entities.turn.mission.Mission result = missionService.createMission(actor, mission.getMissionType(), warshipIDs, planet);
        return ResponseEntity.ok(new Mission(result, getPreferredLanguage()));
    }
}
