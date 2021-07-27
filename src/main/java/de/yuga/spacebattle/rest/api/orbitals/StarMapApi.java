package de.yuga.spacebattle.rest.api.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystem;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystemList;
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

@Api(tags = "StarMapApi")
@RolesAllowed("ROLE_USER") // todo how to add direct roles
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + StarMapApi.ENDPOINT + "/")
public class StarMapApi {

    @Nonnull
    public static final String ENDPOINT = "starMap";
    private static final String STAR_SYSTEMS_ENDPOINT = "star-systems";

    @Nonnull
    private final StarSystemService starSystemService;

    @Nonnull
    private final UserService userService;

    @Autowired
    public StarMapApi(@Nonnull final StarSystemService starSystemService,
                      @Nonnull final UserService userService) {
        Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.starSystemService = starSystemService;
        this.userService = userService;
    }

    @GetMapping(STAR_SYSTEMS_ENDPOINT)
    @ApiOperation(value = "Get all star systems", nickname = "getStarSystems")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StarSystemList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getStarSystems() {
        final List<de.yuga.spacebattle.backend.entities.orbitals.StarSystem> all = starSystemService.findAll();
        return ResponseEntity.ok(new StarSystemList(all));
    }

    @GetMapping(STAR_SYSTEMS_ENDPOINT + "/{idStarSystem}")
    @ApiOperation(value = "Get all planets of this system", nickname = "getStarSystem")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StarSystem.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getStarSystems(@PathVariable("idStarSystem") final int idStarSystem) {

        final de.yuga.spacebattle.backend.entities.orbitals.StarSystem starSystem = starSystemService.find(idStarSystem);
        if (starSystem == null) {
            throw new NotifyWebUserException("Star system not found for id '" + idStarSystem + "'.");
        }
        return ResponseEntity.ok(new StarSystem(starSystem));
    }
}
