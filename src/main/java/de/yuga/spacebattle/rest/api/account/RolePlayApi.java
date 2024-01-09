package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.RolePlaySetting;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EStarNation;
import de.yuga.spacebattle.backend.services.ResourceService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.account.RolePlayData;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@RestController
@Tag(name = "RolePlayApi")
@RolesAllowed("USER")
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + RolePlayApi.ENDPOINT + "/")
public class RolePlayApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "rpg";
    public static final String SHIP_NAMES_ENDPOINT = "shipNames";
    public static final String SHIP_PREFIX_ENDPOINT = "shipPrefix";
    public static final String SHIP_TEMPLATE_ENDPOINT = "shipTemplate";

    @Nonnull
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

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

    @GetMapping
    @Operation(summary = "Get the rpg data", operationId = "getRPGData",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RolePlayData.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getRPGData() {
        final User user = userService.find(getIdUser());
        if (user == null) {
            throw new NotifyWebUserException("Nothing there, mate!");
        }
        return ResponseEntity.ok(new RolePlayData(user.getRolePlaySetting()));
    }

    @PutMapping
    @Operation(summary = "Get the rpg data", operationId = "setRPGData",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RolePlayData.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> setRPGData(@RequestBody @Nonnull final RolePlayData data) {
        Preconditions.checkNotNull(data, "data must not be empty");

        final User user = userService.find(getIdUser());
        if (user == null) {
            throw new NotifyWebUserException("Nothing there, mate!");
        }

        final RolePlaySetting rolePlaySetting = user.getRolePlaySetting();
        rolePlaySetting.setTitle(data.getTitle());
        rolePlaySetting.setTitleAbbreviation(data.getTitleAbbreviation());
        rolePlaySetting.setFirstname(data.getFirstname());
        rolePlaySetting.setSurname(data.getSurname());
        rolePlaySetting.setEmpireName(data.getEmpireName());
        userService.save(user);

        return ResponseEntity.ok(true);
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

    @GetMapping(SHIP_PREFIX_ENDPOINT)
    @Operation(summary = "Get the list of ship names", operationId = "getShipPrefix",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = String.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getShipPrefix() {
        final User user = userService.find(getIdUser());
        if (user == null || user.getRolePlaySetting().getShipPrefix() == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(List.of(user.getRolePlaySetting().getShipPrefix()));
    }

    @PutMapping("/" + SHIP_PREFIX_ENDPOINT + "/{prefix}")
    @Operation(summary = "Get the list of ship names", operationId = "setShipPrefix",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> setShipPrefix(@PathVariable("prefix") @Nonnull final String prefix) {
        Preconditions.checkNotNull(prefix, "prefix must not be empty");

        final User user = userService.find(getIdUser());
        if (user == null) {
            return ResponseEntity.ok(true);
        }
        user.getRolePlaySetting().setShipPrefix(StringUtils.isNotEmpty(prefix) ? prefix : null);
        return validateAndSave(user);
    }

    @DeleteMapping("/" + SHIP_PREFIX_ENDPOINT)
    @Operation(summary = "Get the list of ship names", operationId = "removeShipPrefix",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> removeShipPrefix() {
        final User user = userService.find(getIdUser());
        if (user == null) {
            return ResponseEntity.ok(true);
        }
        user.getRolePlaySetting().setShipPrefix(null);
        return validateAndSave(user);
    }

    @Nonnull
    private ResponseEntity<Boolean> validateAndSave(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final Set<ConstraintViolation<RolePlaySetting>> constraintViolations = validator.validate(user.getRolePlaySetting());
        if (constraintViolations.isEmpty()) {
            userService.save(user);
            return ResponseEntity.ok(true);
        }
        throw new NotifyWebUserException(constraintViolations);
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

    @PutMapping("/" + SHIP_NAMES_ENDPOINT + "/{name}")
    @Operation(summary = "Get the list of ship names", operationId = "addShipNamesFor",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> addShipNamesFor(@PathVariable("name") @Nonnull final String name) {
        Preconditions.checkNotNull(name, "name must not be empty");

        userService.addShipNamesForIndividualList(getIdUser(), name);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/" + SHIP_NAMES_ENDPOINT + "/{name}")
    @Operation(summary = "Get the list of ship names", operationId = "removeShipNamesFor",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> removeShipNamesFor(@PathVariable("name") @Nonnull final String name) {
        Preconditions.checkNotNull(name, "name must not be empty");

        userService.removeShipNamesForIndividualList(getIdUser(), name);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/" + SHIP_TEMPLATE_ENDPOINT)
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

    @PutMapping("/" + SHIP_TEMPLATE_ENDPOINT + "/{starNation}")
    @Operation(summary = "Get the list of ship names", operationId = "addShipNameTemplate",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RolePlayData.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> addShipNameTemplate(@PathVariable("starNation") @Nonnull final EStarNation starNation) {
        Preconditions.checkNotNull(starNation, "starNation must not be empty");

        userService.addShipNamesTemplate(getIdUser(), starNation);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/" + SHIP_TEMPLATE_ENDPOINT + "/{starNation}")
    @Operation(summary = "Get the list of ship names", operationId = "removeShipNameTemplate",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RolePlayData.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> removeShipNameTemplate(@PathVariable("starNation") @Nonnull final EStarNation starNation) {
        Preconditions.checkNotNull(starNation, "starNation must not be empty");

        userService.removeShipNamesTemplate(getIdUser(), starNation);
        return ResponseEntity.ok(true);
    }

}
