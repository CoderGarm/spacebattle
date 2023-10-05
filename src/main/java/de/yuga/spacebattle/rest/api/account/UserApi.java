package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.account.UserPoints;
import de.yuga.spacebattle.backend.dto.account.UserSettings;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.UserSetting;
import de.yuga.spacebattle.backend.services.MailService;
import de.yuga.spacebattle.backend.services.account.UserPointsService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.account.Player;
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
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@RestController
@Tag(name = "UserApi")
@RolesAllowed("USER")
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + UserApi.ENDPOINT + "/")
public class UserApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "user";

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final UserPointsService userPointsService;

    @Nonnull
    private final MailService mailService;

    @Nonnull
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    public UserApi(@Nonnull final UserService userService,
                   @Nonnull final UserPointsService userPointsService,
                   @Nonnull final MailService mailService) {
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.userPointsService = Preconditions.checkNotNull(userPointsService, "userPointsService must not be empty");
        this.mailService = Preconditions.checkNotNull(mailService, "mailService must not be empty");
    }

    @GetMapping
    @Operation(summary = "Get the list of users", operationId = "getAllUsers",
            description = "Get the list of users registered in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Player.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(userService.findAll().stream().map(Player::new).collect(Collectors.toList()));
    }

    @GetMapping(value = "/points/{idUser}")
    @Operation(summary = "Get the list of users", operationId = "getUsersPoints",
            description = "Get the list of users registered in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserPoints.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getUsersPoints(@PathVariable("idUser") final int idUser) {
        final UserPoints points = userPointsService.getPoints(idUser);
        return ResponseEntity.ok(points);
    }

    @GetMapping(value = "/settings")
    @Operation(summary = "Changes settings for the user.", operationId = "getSettings",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserSettings.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getSettings() {
        final UserSetting userSetting = this.userService.getSettings(getIdUser());

        return ResponseEntity.ok(new UserSettings(userSetting));
    }

    @PutMapping(value = "/settings", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Changes settings for the user.", operationId = "changeSettings",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserSettings.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> changeSettings(@RequestBody @Nonnull final UserSettings settings) {
        PreconditionWebHelper.checkNotNull(settings, "settings must not be empty");

        final UserSetting userSetting = this.userService.updateSettings(settings, getIdUser());

        return ResponseEntity.ok(new UserSettings(userSetting));
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

        boolean delete = userService.delete(idUser);
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
                                    schema = @Schema(implementation = Player.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> findByLikeUsername(@PathVariable("username") final String username) {
        if (StringUtils.isBlank(username)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(userService.findLikeUsername(username).stream().map(Player::new).collect(Collectors.toList()));
    }

    @PostMapping(value = "/requestEMailChange/{eMail}")
    @Operation(summary = "Triggers the password change mail.", operationId = "requestEMailChange",
            description = "Triggers the eMail change mail.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public void requestEMailChange(@PathVariable @Nonnull final String eMail) {
        PreconditionWebHelper.checkNotNull(eMail, "eMail shouldn't be null!");

        final User user = userService.find(getIdUser());
        if (user != null) {
            user.getUserSetting().setEmail(eMail);
            user.getUserSetting().setEMailVerified(false);
            user.getUserSetting().setNoEMailWanted(false);
            final Set<ConstraintViolation<UserSetting>> constraintViolations = validator.validateProperty(user.getUserSetting(), "email");
            if (constraintViolations.isEmpty()) {
                final User saved = userService.save(user);
                mailService.sendMailVerificationMessage(Objects.requireNonNull(saved));
            } else {
                throw new NotifyWebUserException("Changing the eMail failed.", constraintViolations);
            }
        }
    }
}
