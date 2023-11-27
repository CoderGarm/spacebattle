package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.mission.MissionItem;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.services.caches.ColonizationCache;
import de.yuga.spacebattle.backend.services.caches.MissionCache;
import de.yuga.spacebattle.backend.services.caches.OperationalCache;
import de.yuga.spacebattle.backend.services.caches.TransportationCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.MoveService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import de.yuga.spacebattle.backend.services.turn.TransportJobService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.backend.services.turn.resources.MarketplaceService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.*;
import de.yuga.spacebattle.rest.dto.turn.mission.MissionReport;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@RestController
@RolesAllowed({"USER"})
@Tag(name = "JournalApi")
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + JournalApi.ENDPOINT + "/")
public class JournalApi extends BaseApi {

    public static final String ENDPOINT = "journal";
    private static final String JOB_FINISHED_ENDPOINT = "finished";
    private static final String TRANSPORT_JOB_ENDPOINT = "transports";
    private static final String FINISHED_MOVEMENT_ENDPOINT = "finishedMovement";
    private static final String FINISHED_COLONIZATIONS_ENDPOINT = "finishedColonizations";
    private static final String OPERATIONALS_ENDPOINT = "operationals";
    private static final String MISSIONS_ENDPOINT = "missions";

    @Nonnull
    private final TickTimeService tickService;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final BattleReportService battleReportService;

    @Nonnull
    private final TransportationCache transportationCache;

    @Nonnull
    private final ColonizationCache colonizationCache;

    @Nonnull
    private final OperationalCache operationalCache;

    @Nonnull
    private final MissionCache missionCache;

    @Nonnull
    private final OperationalService operationalService;

    @Nonnull
    private final MarketplaceService marketplaceService;

    @Nonnull
    private final MoveService moveService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final TransportJobService transportJobService;

    @Autowired
    public JournalApi(@Nonnull final TickTimeService tickService,
                      @Nonnull final JobService jobService,
                      @Nonnull final BattleReportService battleReportService,
                      @Nonnull final TransportationCache transportationCache,
                      @Nonnull final ColonizationCache colonizationCache,
                      @Nonnull final OperationalCache operationalCache,
                      @Nonnull final MissionCache missionCache,
                      @Nonnull final OperationalService operationalService,
                      @Nonnull final MarketplaceService marketplaceService,
                      @Nonnull final MoveService moveService,
                      @Nonnull final PlanetService planetService,
                      @Nonnull final FleetService fleetService,
                      @Nonnull final TransportJobService transportJobService) {
        this.tickService = Preconditions.checkNotNull(tickService, "tickService must not be empty");
        this.jobService = Preconditions.checkNotNull(jobService, "jobService must not be empty");
        this.battleReportService = Preconditions.checkNotNull(battleReportService, "battleReportService must not be empty");
        this.transportationCache = Preconditions.checkNotNull(transportationCache, "transportationCache must not be empty");
        this.colonizationCache = Preconditions.checkNotNull(colonizationCache, "colonizationCache must not be empty");
        this.operationalCache = Preconditions.checkNotNull(operationalCache, "operationalCache must not be empty");
        this.missionCache = Preconditions.checkNotNull(missionCache, "missionCache must not be empty");
        this.operationalService = Preconditions.checkNotNull(operationalService, "operationalService must not be empty");
        this.marketplaceService = Preconditions.checkNotNull(marketplaceService, "marketplaceService must not be empty");
        this.moveService = Preconditions.checkNotNull(moveService, "moveService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.transportJobService = Preconditions.checkNotNull(transportJobService, "transportJobService must not be empty");
    }

    @GetMapping(value = JOB_FINISHED_ENDPOINT)
    @Operation(summary = "Get all jobs which finished today.", operationId = "getFinishedJobs",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Job.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFinishedJobs() {

        final int idUser = getIdUser();
        return ResponseEntity.ok(jobService.findTodayFinishedJobsForUser(idUser).stream()
                .map(j -> new Job(j, getPreferredLanguage()))
                .collect(Collectors.toList()));
    }


    @GetMapping(value = TRANSPORT_JOB_ENDPOINT)
    @Operation(summary = "Get all jobs which are running on this planet.", operationId = "getTransportJobs",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = TransportJob.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getTransportJobs() {
        final de.yuga.spacebattle.backend.entities.turn.Tick today = tickService.getToday();
        final int idUser = getIdUser();
        final List<TransportJob> result = transportationCache.getTransports(today, idUser).stream()
                .map(TransportJob::new)
                .collect(Collectors.toList());
        result.addAll(transportationCache.getOrbitalTransports(today, idUser).stream()
                .map(TransportJob::new)
                .collect(Collectors.toList()));

        result.addAll(transportJobService.findFinishedFor(today, idUser).stream()
                .map(t -> new TransportJob(t, t.getTransferredShips(), getPreferredLanguage()))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(result);
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

        /* fixme introduce orbital scanner module as resolution */

        final Set<Integer> systemIDs = planetService.findAllSystemIDsForUser(idUser);
        systemIDs.addAll(fleetService.findAllSystemIDsWithFleetsForUser(idUser));

        final de.yuga.spacebattle.backend.entities.turn.Tick today = tickService.getToday();
        final List<de.yuga.spacebattle.backend.entities.turn.Move> finishedMovements = moveService.findFinishedInSystems(today, systemIDs);
        final List<FleetMovement> result = new ArrayList<>();
        finishedMovements.stream()
                .filter(m -> Objects.nonNull(m.getFleetSnapshot())).forEach(move -> {
                    result.add(new FleetMovement(move, getIdUser()));
                });
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = FINISHED_COLONIZATIONS_ENDPOINT)
    @Operation(summary = "Get all finished colonizations.", operationId = "getFinishedColonizations",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = FinishedColonization.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFinishedColonizations() {
        return ResponseEntity.ok(colonizationCache.getColonizations(tickService.getToday(), getIdUser()).stream().map(FinishedColonization::new).collect(Collectors.toList()));
    }

    @GetMapping(value = OPERATIONALS_ENDPOINT)
    @Operation(summary = "Get all newly active operationals.", operationId = "getNewlyActiveOperationals",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Commissioning.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getNewlyActiveOperationals() {
        return ResponseEntity.ok(operationalCache.getOperationals(tickService.getToday(), getIdUser()).stream()
                .map(o -> new Commissioning(o, getPreferredLanguage()))
                .collect(Collectors.toList()));
    }

    @GetMapping(value = OPERATIONALS_ENDPOINT + "/pending")
    @Operation(summary = "Get all operationals which are awaiting activation.", operationId = "getOperationalsWaitingForActivation",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Commissioning.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getOperationalsWaitingForActivation() {
        return ResponseEntity.ok(operationalService.getCommissioningForUser(tickService.getToday(), getIdUser()).values().stream()
                .map(o -> new Commissioning(o, getPreferredLanguage()))
                .collect(Collectors.toList()));
    }

    @GetMapping(value = MISSIONS_ENDPOINT)
    @Operation(summary = "Get the today's mission results.", operationId = "getMissionResults",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MissionReport.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getMissionResults() {
        final de.yuga.spacebattle.backend.entities.turn.Tick today = tickService.getToday();
        final boolean newBattleReports = battleReportService.hasNewReportsSince(getIdUser(), today.getNo() - 1);
        final List<MissionItem> missionItems = missionCache.get(today, getIdUser());
        final List<TradedResource> finishedTrades = marketplaceService.findFinishedForUser(getIdUser());
        return ResponseEntity.ok(new MissionReport(newBattleReports, missionItems, finishedTrades, getPreferredLanguage()));
    }
}
