package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EStarNation;
import de.yuga.spacebattle.backend.services.ResourceService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.dto.account.RolePlayData;
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

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@RestController
@Tag(name = "RolePlayApi")
@RolesAllowed("USER")
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + RolePlayApi.ENDPOINT + "/")
public class RolePlayApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "rpg";
    public static final String SHIP_NAMES_ENDPOINT = "shipNames";

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ResourceService resourceService;

    @Autowired
    public RolePlayApi(@Nonnull final UserService userService,
                       @Nonnull final ResourceService resourceService) {
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
    }

    @GetMapping(SHIP_NAMES_ENDPOINT)
    @Operation(summary = "Get the list of ship names", operationId = "getShipNamesForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = String.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getShipNamesForUser() {
        return ResponseEntity.ok(userService.getShipNamesFor(getIdUser()));
    }

    @GetMapping("/" + SHIP_NAMES_ENDPOINT + "/{starNation}")
    @Operation(summary = "Get the list of ship names", operationId = "getShipNamesFor",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = String.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getShipNamesFor(@PathVariable("starNation") @Nonnull final EStarNation starNation) {
        PreconditionWebHelper.checkNotNull(starNation, "starNation must not be empty");

        return ResponseEntity.ok(resourceService.readShipNamesFromList(starNation));
    }


    @GetMapping("/shipTemplate")
    @Operation(summary = "Get the list of ship names", operationId = "getShipNameTemplates",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RolePlayData.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getShipNameTemplates() {
        final RolePlayData rolePlayData = new RolePlayData();
        rolePlayData.setShipNameTemplates(userService.getShipNameTemplates(getIdUser()));
        return ResponseEntity.ok(rolePlayData);
    }

}
