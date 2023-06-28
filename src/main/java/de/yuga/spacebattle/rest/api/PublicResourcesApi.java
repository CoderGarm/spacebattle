package de.yuga.spacebattle.rest.api;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.ResourceService;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.misc.Coords;
import de.yuga.spacebattle.rest.dto.misc.CoordsBlob;
import de.yuga.spacebattle.rest.dto.misc.DistanceElement;
import de.yuga.spacebattle.rest.dto.misc.wormhole.Junction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Nonnull;
import java.util.List;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PUBLIC_BASE_ENDPOINT;

//@RestController
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
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CoordsBlob.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllSystemCoordinates() {
        final List<Coords> coords = resourceService.readStarSystems();
        return ResponseEntity.ok(new CoordsBlob(coords));
    }

    @GetMapping("wormhole-junctions")
    @Operation(summary = "Get star systems by coordinates.", operationId = "getAllWormholeJunctions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Junction.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllWormholeJunctions() {
        return ResponseEntity.ok(resourceService.readWormholes());
    }

    @GetMapping("distances")
    @Operation(summary = "Get the known distances.", operationId = "getAllDistances",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = DistanceElement.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllDistances() {
        final List<DistanceElement> coords = resourceService.getAllDistances();
        return ResponseEntity.ok(coords);
    }
}
