package de.yuga.spacebattle.rest.api;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.ResourceService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
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
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PUBLIC_BASE_ENDPOINT;

@RestController
@Tag(name = "PublicResourcesApi")
@RequestMapping(value = "/" + PUBLIC_BASE_ENDPOINT + "/" + PublicResourcesApi.ENDPOINT + "/")
public class PublicResourcesApi extends BaseApi {

    public final static String ENDPOINT = "resources";

    @Nonnull
    private final ResourceService resourceService;

    @Nonnull
    private final UserService userService;

    public PublicResourcesApi(@Nonnull final ResourceService resourceService,
                              @Nonnull final UserService userService) {
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
    }

    @GetMapping("user-names")
    @Operation(summary = "Get all usernames.", operationId = "getUsernames",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = String.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getUsernames() {
        final Set<String> names = userService.findAll().stream().map(User::getUsername).collect(Collectors.toSet());
        return ResponseEntity.ok(names);
    }

    /*
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
    */
}
