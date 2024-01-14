package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.research.EmpireResearchCapability;
import de.yuga.spacebattle.backend.services.caclulator.TickOutputCalculator;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "JobApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + JobApi.ENDPOINT + "/")
public class JobApi extends BaseApi {

    public static final String ENDPOINT = "job";
    private static final String JOB_RUNNING_AT_ENDPOINT = "runningAt";
    private static final String RESEARCH_JOB_RUNNING_AT_ENDPOINT = "runningResearch";
    private static final String JOB_RUNNING_FOR_FLEET_ENDPOINT = "runningForFleet";
    private static final String JOB_CANCEL = "cancel";

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final TickOutputCalculator tickOutputCalculator;

    @Autowired
    public JobApi(@Nonnull final JobService jobService,
                  @Nonnull final PlanetService planetService,
                  @Nonnull final TickOutputCalculator tickOutputCalculator) {
        this.jobService = Preconditions.checkNotNull(jobService, "jobService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.tickOutputCalculator = Preconditions.checkNotNull(tickOutputCalculator, "tickOutputCalculator must not be empty");
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
        final List<de.yuga.spacebattle.backend.entities.turn.Job> allJobsByPlanet = jobService.findAllJobsByPlanet(idPlanet);
        return ResponseEntity.ok(allJobsByPlanet.stream()
                .map(j -> new Job(j, tickOutputCalculator.getTicklyIncome(idPlanet), Objects.requireNonNull(planetService.findResourceDeposit(idPlanet)), getPreferredLanguage()))
                .collect(Collectors.toList()));
    }

    @GetMapping(value = RESEARCH_JOB_RUNNING_AT_ENDPOINT)
    @Operation(summary = "Get all jobs which are running for the questioning user.", operationId = "getResearchJobsForEmpire",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Job.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getResearchJobsForEmpire() {

        final int idUser = getIdUser();
        final List<de.yuga.spacebattle.backend.entities.turn.Job> researchJobs = jobService.findAllResearchJobsForUser(idUser);

        final List<Job> result = new ArrayList<>();

        if (!researchJobs.isEmpty()) {
            final EmpireResearchCapability capability = planetService.getEmpireWideResearchPoints(idUser);
            researchJobs.forEach(j -> result.add(new Job(j, capability, getPreferredLanguage())));
        }

        return ResponseEntity.ok(result);
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

    @GetMapping(value = JOB_CANCEL + "/{idJob}")
    @Operation(summary = "Cancels and refund a job.", operationId = "cancelJob",
            responses = {
                    @ApiResponse(responseCode = "200", description = "you can build something or not",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> cancelJob(@PathVariable("idJob") final int idJob) {
        final int idUser = getIdUser();
        final boolean cancelled = jobService.cancelJob(idUser, idJob);
        return ResponseEntity.ok(cancelled);
    }
}
