package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.account.UserJsonList;
import de.yuga.spacebattle.rest.dto.account.UserReq;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Api(tags = "UserApi")
@RolesAllowed("ROLE_USER") // todo how to add direct roles
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + UserApi.ENDPOINT + "/")
public class UserApi {

    @Nonnull
    public static final String ENDPOINT = "user";

    @Nonnull
    private final UserService service;

    @Nonnull
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    public UserApi(@Nonnull final UserService userService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.service = userService;
    }

    @GetMapping
    @ApiOperation(value = "Get the list of users", nickname = "getAllUsers")
    @Operation(
            description = "Get the list of users registered in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    //array = @ArraySchema(arraySchema = @Schema(implementation = UserJson.class)), commented out because of not working
                                    schema = @Schema(implementation = UserJsonList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> findAll() {
        final List<User> userList = service.findAll();
        final List<UserJson> userJsons = userList.stream().map(UserJson::new).collect(Collectors.toList());
        return ResponseEntity.ok(userJsons);
    }

    @GetMapping(value = "{idUser}")
    @ApiOperation(value = "Get a single user by it's idUser", nickname = "getSingleUser")
    @Operation(
            description = "Returns a user which is  registered in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserJson.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> findById(@PathVariable("idUser") final int idUser) {

        final User foundUser = service.find(idUser);
        if (foundUser == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(new UserJson(foundUser));
    }

    @PutMapping
    @ApiOperation(value = "Updates a single user", nickname = "updateUser")
    @Operation(
            description = "Updates and returns a user which is now registered in the system. Every changed field except the idUser will be updated. The user id must be present.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserJson.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> update(@RequestBody @Nonnull final UserReq userJson) {
        PreconditionWebHelper.checkNotNull(userJson, "userJson shouldn't be null!");
        PreconditionWebHelper.checkNotNull(userJson.getIdUser(), "idUser shouldn't be null!");

        final Set<ConstraintViolation<UserReq>> validate = validator.validate(userJson);
        if (validate.isEmpty()) {
            final User user = service.find(userJson.getIdUser());
            if (user == null) {
                throw new NotifyWebUserException("User wasn't found for idUser '");
            }

            user.setEmail(userJson.getEmail());
            user.setPassword(userJson.getPassword());
            final User saved = service.save(user);
            return ResponseEntity.ok(new UserJson(saved));
        }
        throw new NotifyWebUserException("User wasn't updated for idUser '", validate);
    }

    @DeleteMapping(value = "/{idUser}")
    @ApiOperation(value = "Deletes a single user", nickname = "deleteUser")
    @Operation(
            description = "Deletes a user which is not any longer registered in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful"),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> delete(@PathVariable("idUser") @Nonnull final Integer idUser) {
        PreconditionWebHelper.checkNotNull(idUser, "The idUser shouldn't be null!");
        PreconditionWebHelper.checkArgument(idUser < -1 || idUser == 0, "The idUser must be valid");

        boolean delete = service.delete(idUser);
        if (delete) {
            return ResponseEntity.ok().build();
        }
        throw new NotifyWebUserException("User wasn't deleted for idUser '" + idUser + "'.");
    }

    @GetMapping(value = "byNameLike/{username}")
    @ApiOperation(value = "Get a single user by it's idUser", nickname = "getUsersByLikeUserName")
    @Operation(
            description = "Returns a list of users which usernames matches the search string",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserJsonList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> findByLikeUsername(@PathVariable("username") final String username) {
        if (StringUtils.isBlank(username)) {
            return ResponseEntity.ok().build();
        }
        final List<User> foundUsers = service.findLikeUsername(username);
        return ResponseEntity.ok(new UserJsonList(foundUsers));
    }
}
