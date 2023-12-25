package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.turn.TickAdviceService;
import de.yuga.spacebattle.backend.services.turn.mission.MissionService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import de.yuga.spacebattle.rest.dto.turn.TickAdvice;
import de.yuga.spacebattle.rest.dto.turn.resources.trade.TradeContract;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@RestController
@RolesAllowed({"USER"})
@Tag(name = "AdvisoryApi")
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + AdvisoryApi.ENDPOINT + "/")
public class AdvisoryApi extends BaseApi {

    public static final String ENDPOINT = "advisory";
    private static final String PIRATE_HUNT_ENDPOINT = "pirateHunt";
    private static final String CONVOY_PROTECTION_ENDPOINT = "convoyProtection";
    private static final String INFRASTRUCTURE_ADVICE_ENDPOINT = "infrastructure";

    @Nonnull
    private final MissionService missionService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final TickAdviceService tickAdviceService;

    @Autowired
    public AdvisoryApi(@Nonnull final MissionService missionService,
                       @Nonnull final FleetService fleetService,
                       @Nonnull final TickAdviceService tickAdviceService) {
        this.missionService = Preconditions.checkNotNull(missionService, "missionService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.tickAdviceService = Preconditions.checkNotNull(tickAdviceService, "tickAdviceService must not be empty");
    }

    @GetMapping(value = PIRATE_HUNT_ENDPOINT)
    @Operation(summary = "Get all jobs which finished today.", operationId = "getPirateHuntAdvice",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Planet.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPirateHuntAdvice() {
        final Set<de.yuga.spacebattle.backend.entities.orbitals.Planet> planets = missionService.findAllPlanetsWithoutPirateHunt(getIdUser());

        final List<Fleet> anchoredFleets = fleetService.findAllFleetsWithoutMovementByUser(getIdUser());
        final Set<de.yuga.spacebattle.backend.entities.orbitals.Planet> planetsWithAnchoredFleets = anchoredFleets.stream()
                .map(Fleet::getOrbit)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()).stream()
                .map(FleetOrbit::getPlanet)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        planets.removeAll(planetsWithAnchoredFleets);
        return ResponseEntity.ok(planets.stream().map(Planet::new).collect(Collectors.toList()));
    }

    @GetMapping(value = CONVOY_PROTECTION_ENDPOINT)
    @Operation(summary = "Get all jobs which finished today.", operationId = "getConvoyProtectionAdvice",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = TradeContract.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getConvoyProtectionAdvice() {
        final Set<TradedResource> tradedResources = missionService.findAllConvoysWithoutEscort(getIdUser());
        return ResponseEntity.ok(tradedResources.stream().map(TradeContract::new).collect(Collectors.toList()));
    }

    @GetMapping(value = INFRASTRUCTURE_ADVICE_ENDPOINT)
    @Operation(summary = "Get all jobs which finished today.", operationId = "getConstructionAdvice",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TickAdvice.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getConstructionAdvice() {
        final TickAdvice tickAdvice = tickAdviceService.getConstructionAdvice(getIdUser(), getPreferredLanguage());
        return ResponseEntity.ok(tickAdvice);
    }


}
