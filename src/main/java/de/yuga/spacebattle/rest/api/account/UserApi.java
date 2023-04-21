package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.account.UserPoints;
import de.yuga.spacebattle.backend.services.account.UserPointsService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@RestController
@Tag(name = "UserApi")
@RolesAllowed("USER")
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + UserApi.ENDPOINT + "/")
public class UserApi {

    @Nonnull
    public static final String ENDPOINT = "user";

    @Nonnull
    private final UserService service;

    @Nonnull
    private final UserPointsService userPointsService;

    @Nonnull
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    public UserApi(@Nonnull final UserService userService, final UserPointsService userPointsService) {
        this.service = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.userPointsService = Preconditions.checkNotNull(userPointsService, "userPointsService must not be empty");
    }

    @GetMapping
    @Operation(summary = "Get the list of users", operationId = "getAllUsers",
            description = "Get the list of users registered in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = UserJson.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(UserJson::new).collect(Collectors.toList()));
    }

    @GetMapping(value = "/points/{idUser}")
    @Operation(summary = "Get the list of users", operationId = "getUsersPoints",
            description = "Get the list of users registered in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = UserJson.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getUsersPoints(@PathVariable("idUser") final int idUser) {
        final UserPoints points = userPointsService.getPoints(idUser);
        return ResponseEntity.ok(service.findAll().stream().map(UserJson::new).collect(Collectors.toList()));
    }

    @DeleteMapping(value = "/{idUser}")
    @Operation(summary = "Deletes a single user", operationId = "deleteUser",
            description = "Deletes a user which is not any longer registered in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful"),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("idUser") @Nonnull final Integer idUser) {
        PreconditionWebHelper.checkNotNull(idUser, "The idUser shouldn't be null!");
        PreconditionWebHelper.checkArgument(idUser < -1 || idUser == 0, "The idUser must be valid");

        boolean delete = service.delete(idUser);
        if (!delete) {
            throw new NotifyWebUserException("User wasn't deleted for idUser '" + idUser + "'.");
        }
    }

    @GetMapping(value = "byNameLike/{username}")
    @Operation(summary = "Get a single user by it's idUser", operationId = "getUsersByLikeUserName",
            description = "Returns a list of users which usernames matches the search string",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = UserJson.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> findByLikeUsername(@PathVariable("username") final String username) {
        if (StringUtils.isBlank(username)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(service.findLikeUsername(username).stream().map(UserJson::new).collect(Collectors.toList()));
    }
}
