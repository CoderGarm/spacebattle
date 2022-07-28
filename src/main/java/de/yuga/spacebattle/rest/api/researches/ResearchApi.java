package de.yuga.spacebattle.rest.api.researches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.researches.ResearchLevel;
import de.yuga.spacebattle.rest.dto.researches.ResearchTree;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "ResearchApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + ResearchApi.ENDPOINT + "/")
public class ResearchApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "research";
    private static final String BY_USER_ENDPOINT = "byUser";
    private static final String AVAILABLE_BY_USER_ENDPOINT = "availableByUser";
    private static final String TREE_ENDPOINT = "tree";
    private static final String RESEARCH_POSSIBLE_FOR_USER_ENDPOINT = "possibleForUser";

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final PlanetService planetService;

    @Autowired
    public ResearchApi(@Nonnull final ResearchService researchService,
                       @Nonnull final UserService userService,
                       @Nonnull final JobService jobService,
                       @Nonnull final PlanetService planetService) {
        Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");

        this.researchService = researchService;
        this.userService = userService;
        this.jobService = jobService;
        this.planetService = planetService;
    }

    @GetMapping(value = BY_USER_ENDPOINT + "/{idUser}")
    @Operation(summary = "Get all already researched researches for the user.", operationId = "getResearchByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = ResearchLevel.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getResearchByUser(@PathVariable("idUser") final int idUser) {

        final Set<de.yuga.spacebattle.backend.entities.researches.ResearchLevel> researchesForUser = researchService.getResearchesForUser(idUser);
        final List<ResearchLevel> researchLevels = researchesForUser.stream()
                .map(r -> new ResearchLevel(r, getPreferredLanguage()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(researchLevels);
    }

    @PostMapping(value = BY_USER_ENDPOINT + "/{idUser}")
    @Operation(summary = "Starts a research job for the user.", operationId = "startResearchByUser",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResearchLevel.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.turn.Job.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> startResearchByUser(@PathVariable("idUser") final int idUser, @RequestBody final ResearchLevel researchLevel) {

        final User user = userService.find(idUser);
        if (user == null) {
            throw new NotifyWebUserException("No user was found.");
        }
        final Research research = researchService.find(researchLevel.getResearch().getIdResearch());
        if (research == null) {
            throw new NotifyWebUserException("No research was found.");
        }
        final Job researchJob = jobService.createResearchJob(user, research);
        return ResponseEntity.ok(new de.yuga.spacebattle.rest.dto.turn.Job(researchJob, getPreferredLanguage()));
    }

    @GetMapping(value = AVAILABLE_BY_USER_ENDPOINT + "/{idUser}")
    @Operation(summary = "Get all available researches for the user.", operationId = "getAvailableResearchByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = ResearchLevel.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAvailableResearchByUser(@PathVariable("idUser") final int idUser) {
        final List<Research> jobActiveFor = jobService.getResearchesFromActiveJobs(idUser);
        final Map<Research, Integer> researchesForUser = researchService.getUnlockableResearches(idUser, jobActiveFor);
        final List<ResearchLevel> researchLevels = researchesForUser.entrySet().stream()
                .map(entry -> new ResearchLevel(entry.getKey(), entry.getValue(), getPreferredLanguage())).collect(Collectors.toList());
        return ResponseEntity.ok(researchLevels);
    }

    @GetMapping(value = TREE_ENDPOINT)
    @Operation(summary = "Get all researches with their unlocking research.", operationId = "getTree",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResearchTree.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getTree() {
        return ResponseEntity.ok(researchService.getResearchTree(getPreferredLanguage()));
    }

    @GetMapping(value = RESEARCH_POSSIBLE_FOR_USER_ENDPOINT + "/{idUser}")
    @Operation(summary = "Checks if a research job is possible for the user.", operationId = "researchPossibleForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> researchPossibleForUser(@PathVariable("idUser") final int idUser) {
        final User user = userService.find(idUser);
        if (user == null) {
            throw new NotifyWebUserException("No user found for id '" + idUser + "'");
        }
        final Planet researchPlanet = planetService.findResearchPlanet(user);
        if (researchPlanet == null) {
            return ResponseEntity.ok(false);
        }
        final Construction facility = researchPlanet.getConstructionByResource(EResourceType.RESEARCH)
                .stream().findFirst().orElse(null);
        if (facility == null) {
            return ResponseEntity.ok(false);
        }
        boolean researchPossible = facility.getJobs().isEmpty();
        return ResponseEntity.ok(researchPossible);
    }
}
