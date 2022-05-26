package de.yuga.spacebattle.rest.api.buildings;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.Arrays;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "BuildingApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + BuildingApi.ENDPOINT + "/")
public class BuildingApi {

    @Nonnull
    public static final String ENDPOINT = "buildings";
    private static final String E_PRODUCTION_CATEGORY = "EProductionCategory";
    private static final String E_REFINEMENT_SEQUENCE = "ERefinementSequence";

    @Nonnull
    private final BuildingService buildingService;

    @Autowired
    public BuildingApi(@Nonnull final BuildingService buildingService) {
        Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");

        this.buildingService = buildingService;
    }

    @GetMapping(value = "/" + E_PRODUCTION_CATEGORY)
    @Operation(summary = "Get all EProductionCategories.", operationId = "getEProductionCategories",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = EProductionCategory.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEProductionCategories() {
        return ResponseEntity.ok(Arrays.stream(EProductionCategory.values()).collect(Collectors.toList()));
    }

    @GetMapping(value = "/" + E_REFINEMENT_SEQUENCE)
    @Operation(summary = "Get all ERefinementSequences.", operationId = "getERefinementSequences",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.enums.ERefinementSequence.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getERefinementSequences() {
        return ResponseEntity.ok(Arrays.stream(ERefinementSequence.values()).map(de.yuga.spacebattle.rest.dto.enums.ERefinementSequence::new).collect(Collectors.toList()));
    }
}
