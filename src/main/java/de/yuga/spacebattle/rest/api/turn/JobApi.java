package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
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
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;
import static de.yuga.spacebattle.rest.api.turn.JobApi.ENDPOINT;

@Tag(name = "JobApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + ENDPOINT + "/")
public class JobApi {

    @Nonnull
    public static final String ENDPOINT = "job";
    private static final String JOB_RUNNING_AT_PLANET_ENDPOINT = "runningAt";

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final PlanetService planetService;

    @Autowired
    public JobApi(@Nonnull final JobService jobService, @Nonnull final PlanetService planetService) {
        Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");

        this.jobService = jobService;
        this.planetService = planetService;
    }

    @GetMapping(value = JOB_RUNNING_AT_PLANET_ENDPOINT + "/{idPlanet}")
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
        return ResponseEntity.ok(jobService.findAllJobsByPlanet(idPlanet).stream().map(Job::new).collect(Collectors.toList()));
    }
}
