package de.yuga.spacebattle.rest.api.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.mission.MissionService;
import de.yuga.spacebattle.backend.services.turn.resources.MarketplaceService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
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
import java.util.Objects;
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
    public static final String STOP_MISSION_ENDPOINT = "stop";

    @Nonnull
    private final MissionService missionService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final MarketplaceService marketplaceService;

    @Autowired
    public MissionApi(@Nonnull final MissionService missionService,
                      @Nonnull final PlanetService planetService,
                      @Nonnull final UserService userService,
                      @Nonnull final MarketplaceService marketplaceService) {
        this.missionService = Preconditions.checkNotNull(missionService, "missionService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
        this.marketplaceService = Preconditions.checkNotNull(marketplaceService, "marketplaceService must not be empty");
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

        final de.yuga.spacebattle.backend.entities.turn.mission.Mission result;
        switch (mission.getMissionType()) {
            case PIRATE_HUNT:
                final Planet planet = planetService.find(Objects.requireNonNull(mission.getVenue()).getIdPlanet());
                Preconditions.checkNotNull(planet, "planet must not be empty");
                result = missionService.createPirateHuntMission(actor, warshipIDs, planet);
                break;
            case CONVOY_PROTECTION:
                final Integer idTradedResource = mission.getIdTradedResource();
                if (idTradedResource == null) {
                    throw new NotifyWebUserException("This will not work, sorry.");
                }
                final TradedResource tradedResource = marketplaceService.findTradedResource(idTradedResource);
                if (tradedResource == null) {
                    throw new NotifyWebUserException("This will not work, sorry.");
                }
                // yes, you can protect every trade if you can address them
                result = missionService.createConvoyProtectionMission(actor, warshipIDs, tradedResource);
                break;
            default:
                throw new NotifyWebUserException("This was a pretty nice try to break the rules.");
        }
        return ResponseEntity.ok(new Mission(result, getPreferredLanguage()));
    }

    @PutMapping(STOP_MISSION_ENDPOINT + "/{idMission}")
    @Operation(summary = "Stops a mission", operationId = "stopMission",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> stopMission(@PathVariable("idMission") final int idMission) {
        missionService.stopMission(idMission, getIdUser());
        return ResponseEntity.ok(true);
    }
}
