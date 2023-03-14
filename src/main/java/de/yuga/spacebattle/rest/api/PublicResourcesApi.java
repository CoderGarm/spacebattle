package de.yuga.spacebattle.rest.api;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import de.yuga.spacebattle.backend.services.ResourceService;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.List;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PUBLIC_BASE_ENDPOINT;

@RestController
@Tag(name = "PublicResourcesApi")
@RequestMapping(value = "/" + PUBLIC_BASE_ENDPOINT + "/" + PublicResourcesApi.ENDPOINT + "/")
public class PublicResourcesApi extends BaseApi {

    public final static String ENDPOINT = "resources";

    @Nonnull
    private final ResourceService resourceService;

    public PublicResourcesApi(@Nonnull final ResourceService resourceService) {
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
    }

    @GetMapping("system-coordinates")
    @Operation(summary = "Get star systems by coordinates.", operationId = "getAllSystemCoordinates",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MasterOfTheUniverseService.CoordsBlob.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllSystemCoordinates() {
        final List<MasterOfTheUniverseService.Coords> coords = resourceService.readStarSystems();
        return ResponseEntity.ok(new MasterOfTheUniverseService.CoordsBlob(coords));
    }

}
