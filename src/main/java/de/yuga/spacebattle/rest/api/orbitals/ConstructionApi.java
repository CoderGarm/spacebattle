package de.yuga.spacebattle.rest.api.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
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
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "ConstructionApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + ConstructionApi.ENDPOINT + "/")
public class ConstructionApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "construction";

    private static final String CONSTRUCTABLE = "constructable";

    @Nonnull
    private final ConstructionService constructionService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final PlanetService planetService;

    @Autowired
    public ConstructionApi(@Nonnull final ConstructionService constructionService,
                           @Nonnull final ResearchService researchService,
                           @Nonnull final PlanetService planetService) {
        Preconditions.checkNotNull(constructionService, "constructionService shouldn't be null!");
        Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");

        this.constructionService = constructionService;
        this.researchService = researchService;
        this.planetService = planetService;
    }

    @GetMapping(value = CONSTRUCTABLE + "/{idPlanet}")
    @Operation(summary = "Get all constructions for a planets which could be build.", operationId = "getPossibleConstructionsByPlanet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.constructables.buildings.Construction.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getUpgradeableConstructions(@PathVariable("idPlanet") final int idPlanet) {

        final Planet planet = planetService.find(idPlanet);
        if (planet == null || planet.getOwner() == null) {
            throw new NotifyWebUserException("This planet is not colonized");
        }

        final Set<Construction> upgradeableConstructions = constructionService.getUpgradeableConstructions(planet);
        final Set<Research> researches = upgradeableConstructions.stream().map(Construction::getBuilding).map(Building::getUnlockedThrough).collect(Collectors.toSet());
        final Set<ResearchLevel> levels = researchService.getResearchesForUser(getIdUser(), researches);

        final Set<de.yuga.spacebattle.rest.dto.constructables.buildings.Construction> possibleConstructions = upgradeableConstructions
                .stream()
                .map(e -> {
                    final de.yuga.spacebattle.rest.dto.constructables.buildings.Construction construction = new de.yuga.spacebattle.rest.dto.constructables.buildings.Construction(e, getPreferredLanguage());
                    levels.stream().filter(l -> l.getResearch().equals(e.getBuilding().getUnlockedThrough())).findFirst().filter(l -> l.getLevel() > e.getLevel()).ifPresent(l -> construction.activateNextLevel());
                    return construction;
                })
                .collect(Collectors.toSet());

        return ResponseEntity.ok(possibleConstructions);
    }

    @GetMapping(value = "{idPlanet}")
    @Operation(summary = "Get all constructions on a planets.", operationId = "getConstructionsByPlanet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.constructables.buildings.Construction.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getConstructions(@PathVariable("idPlanet") final int idPlanet) {
        return ResponseEntity.ok(constructionService.findAllConstructionsOnPlanet(idPlanet).stream()
                .map(c -> new de.yuga.spacebattle.rest.dto.constructables.buildings.Construction(c, getPreferredLanguage()))
                .collect(Collectors.toList()));
    }
}
