package de.yuga.spacebattle.rest.api.buildings;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.rest.dto.enums.EProductionCategoryList;
import de.yuga.spacebattle.rest.dto.enums.ERefinementSequenceList;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Api(tags = "BuildingApi")
@RolesAllowed("ROLE_USER") // todo how to add direct roles
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + BuildingApi.ENDPOINT + "/")
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
    @ApiOperation(value = "Get all EProductionCategories.", nickname = "getEProductionCategories")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EProductionCategoryList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEProductionCategories() {
        final List<EProductionCategory> eResourceTypes = Arrays.stream(EProductionCategory.values()).collect(Collectors.toList());
        return ResponseEntity.ok(new EProductionCategoryList(eResourceTypes));
    }

    @GetMapping(value = "/" + E_REFINEMENT_SEQUENCE)
    @ApiOperation(value = "Get all ERefinementSequences.", nickname = "getERefinementSequences")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ERefinementSequenceList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getERefinementSequences() {
        final List<ERefinementSequence> eResourceTypes = Arrays.stream(ERefinementSequence.values()).collect(Collectors.toList());
        return ResponseEntity.ok(new ERefinementSequenceList(eResourceTypes));
    }
}
