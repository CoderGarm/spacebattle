package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.JobList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.List;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;
import static de.yuga.spacebattle.rest.api.turn.JobApi.ENDPOINT;

@Api(tags = "JobApi")
@RolesAllowed("ROLE_USER")
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + ENDPOINT + "/")
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
    @ApiOperation(value = "Get all jobs which are running on this planet.", nickname = "getJobsOnPlanet")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = JobList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getJobsOnPlanet(@PathVariable("idPlanet") final int idPlanet) {
        final List<Job> allOnPlanet = jobService.findAllJobsByPlanet(idPlanet);
        return ResponseEntity.ok(new JobList(allOnPlanet));
    }
}
