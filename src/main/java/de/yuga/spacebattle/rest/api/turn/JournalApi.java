package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.caches.ColonizationCache;
import de.yuga.spacebattle.backend.services.caches.FleetMovementCache;
import de.yuga.spacebattle.backend.services.caches.TransportationCache;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.FinishedColonization;
import de.yuga.spacebattle.rest.dto.turn.FleetMovement;
import de.yuga.spacebattle.rest.dto.turn.Job;
import de.yuga.spacebattle.rest.dto.turn.TransportJob;
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

    @Nonnull
    private final TickService tickService;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final TransportationCache transportationCache;

    @Nonnull
    private final FleetMovementCache fleetMovementCache;

    @Nonnull
    private final ColonizationCache colonizationCache;

    @Autowired
    public JournalApi(@Nonnull final TickService tickService,
                      @Nonnull final JobService jobService,
                      @Nonnull final TransportationCache transportationCache,
                      @Nonnull final FleetMovementCache fleetMovementCache,
                      @Nonnull final ColonizationCache colonizationCache) {
        this.tickService = Preconditions.checkNotNull(tickService, "tickService must not be empty");
        this.jobService = Preconditions.checkNotNull(jobService, "jobService must not be empty");
        this.transportationCache = Preconditions.checkNotNull(transportationCache, "transportationCache must not be empty");
        this.fleetMovementCache = Preconditions.checkNotNull(fleetMovementCache, "fleetMovementCache must not be empty");
        this.colonizationCache = Preconditions.checkNotNull(colonizationCache, "colonizationCache must not be empty");
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
        final Tick today = tickService.getToday();
        return ResponseEntity.ok(fleetMovementCache.getMovements(today, idUser).stream()
                .map(FleetMovement::new)
                .collect(Collectors.toList()));
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
        final int idUser = getIdUser();
        final Tick today = tickService.getToday();
        return ResponseEntity.ok(colonizationCache.getColonizations(today, idUser).stream().map(FinishedColonization::new).collect(Collectors.toList()));
    }
}
