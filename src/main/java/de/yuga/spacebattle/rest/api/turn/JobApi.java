package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.CacheStore;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.Job;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "JobApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + JobApi.ENDPOINT + "/")
public class JobApi extends BaseApi {

    @Nonnull
    private final CacheStore<Integer, de.yuga.spacebattle.backend.entities.turn.Tick> cache = new CacheStore<>(2, TimeUnit.DAYS);

    public static final String ENDPOINT = "job";
    private static final String JOB_RUNNING_AT_ENDPOINT = "runningAt";
    private static final String JOB_FINISHED_ENDPOINT = "finished";
    private static final String JOB_UNKNOWN_FINISHED_PRESENT_ENDPOINT = "unknownFinishedPresent";
    private static final String JOB_RUNNING_FOR_FLEET_ENDPOINT = "runningForFleet";

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final TickService tickService;

    @Autowired
    public JobApi(@Nonnull final JobService jobService,
                  @Nonnull final TickService tickService) {
        this.jobService = Preconditions.checkNotNull(jobService, "jobService must not be empty");
        this.tickService = Preconditions.checkNotNull(tickService, "tickService must not be empty");
    }

    @GetMapping(value = JOB_RUNNING_AT_ENDPOINT + "/{idPlanet}")
    @Operation(summary = "Get all jobs which are running on this planet.", operationId = "getJobsOnPlanet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Job.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getJobsOnPlanet(@PathVariable("idPlanet") final int idPlanet) {
        return ResponseEntity.ok(jobService.findAllJobsByPlanet(idPlanet).stream()
                .map(j -> new Job(j, getPreferredLanguage()))
                .collect(Collectors.toList()));
    }

    @GetMapping(value = JOB_RUNNING_AT_ENDPOINT)
    @Operation(summary = "Get all jobs which are running for the questioning user.", operationId = "getJobsForEmpire",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Job.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getJobsForEmpire() {

        final int idUser = getIdUser();
        return ResponseEntity.ok(jobService.findAllJobsForUser(idUser).stream()
                .map(j -> new Job(j, getPreferredLanguage()))
                .collect(Collectors.toList()));
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

        final de.yuga.spacebattle.backend.entities.turn.Tick today = tickService.getToday();
        final de.yuga.spacebattle.backend.entities.turn.Tick lastTick = cache.get(idUser);
        if (lastTick == null || lastTick.compareTo(today) < 0) {
            cache.add(idUser, today);
        }

        return ResponseEntity.ok(jobService.findTodayFinishedJobsForUser(idUser).stream()
                .map(j -> new Job(j, getPreferredLanguage()))
                .collect(Collectors.toList()));
    }

    @GetMapping(value = JOB_UNKNOWN_FINISHED_PRESENT_ENDPOINT)
    @Operation(summary = "Get all jobs which finished today and wasn't questioned before.", operationId = "areUnknownFinishedJobsPresent",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> areUnknownFinishedJobsPresent() {

        final int idUser = getIdUser();
        final de.yuga.spacebattle.backend.entities.turn.Tick today = tickService.getToday();
        final de.yuga.spacebattle.backend.entities.turn.Tick lastTick = cache.get(idUser);
        if (lastTick == null || lastTick.compareTo(today) < 0) {
            return ResponseEntity.ok(jobService.areTodayFinishedJobsForUserPresent(idUser));
        }
        return ResponseEntity.ok(false);
    }

    @GetMapping(value = JOB_RUNNING_FOR_FLEET_ENDPOINT + "/{idFleet}")
    @Operation(summary = "Get all jobs which are running for the questioning user.", operationId = "jobRunningForFleet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "you can build something or not",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> jobRunningForFleet(@PathVariable("idFleet") final int idFleet) {
        final int idUser = getIdUser();

        final boolean isRunning = jobService.isJobRunningFor(idUser, idFleet);
        return ResponseEntity.ok(isRunning);
    }
}
