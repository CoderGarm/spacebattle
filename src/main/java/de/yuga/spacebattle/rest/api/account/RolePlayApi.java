package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.RolePlaySetting;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EStarNation;
import de.yuga.spacebattle.backend.services.ResourceService;
import de.yuga.spacebattle.backend.services.account.RolePlayService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.misc.FileSystemStorageService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.account.RPGTextBlocks;
import de.yuga.spacebattle.rest.dto.account.RolePlayData;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.misc.FileUpload;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
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
    public static final String EMPIRE_IMAGE_ENDPOINT = "empire-emblem";
    public static final String EMPIRE_IMAGE_PREFIX = "empire-emblem-";
    public static final String RPG_TEXTS = "rpg-texts";

    @Nonnull
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ResourceService resourceService;

    @Nonnull
    private final FileSystemStorageService storageService;

    @Nonnull
    private final RolePlayService rolePlayService;

    @Autowired
    public RolePlayApi(@Nonnull final UserService userService,
                       @Nonnull final ResourceService resourceService,
                       @Nonnull final FileSystemStorageService storageService,
                       @Nonnull final RolePlayService rolePlayService) {
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
        this.storageService = Preconditions.checkNotNull(storageService, "storageService must not be empty");
        this.rolePlayService = Preconditions.checkNotNull(rolePlayService, "rolePlayService must not be empty");
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
        final RolePlaySetting rolePlaySetting = rolePlayService.findForUser(getIdUser());
        Preconditions.checkNotNull(rolePlaySetting, "Nothing there, mate!");
        return ResponseEntity.ok(new RolePlayData(rolePlaySetting));
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

        validateOrThrow(data);

        final RolePlaySetting rolePlaySetting = rolePlayService.findForUser(getIdUser());
        Preconditions.checkNotNull(rolePlaySetting, "Nothing there, mate!");
        rolePlaySetting.setTitle(data.getTitle());
        rolePlaySetting.setTitleAbbreviation(data.getTitleAbbreviation());
        rolePlaySetting.setFirstname(data.getFirstname());
        rolePlaySetting.setSurname(data.getSurname());
        rolePlaySetting.setEmpireName(data.getEmpireName());
        rolePlayService.save(rolePlaySetting);

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

    private void validateOrThrow(@Nonnull final RolePlayData rolePlayData) {
        Preconditions.checkNotNull(rolePlayData, "rolePlayData must not be empty");

        final Set<ConstraintViolation<RolePlayData>> constraintViolations = validator.validate(rolePlayData);
        if (!constraintViolations.isEmpty()) {
            throw new NotifyWebUserException(constraintViolations);
        }
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

    @GetMapping(EMPIRE_IMAGE_ENDPOINT + "/{idUser}")
    @Operation(summary = "Get the emblem of the empire of the given user.", operationId = "getEmpireEmblem",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FileUpload.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEmpireEmblem(@PathVariable final int idUser) throws IOException {

        final File file = storageService.loadAsResource(createEmpireEmblemFileName(idUser));
        if (file == null) {
            return ResponseEntity.ok().build();
        }

        final String encodeImage = Base64.getEncoder().withoutPadding().encodeToString(Files.readAllBytes(file.toPath()));
        final FileUpload fileUpload = new FileUpload(file.getName(), encodeImage);

        return ResponseEntity.ok(fileUpload);
    }

    /**
     * Due to the inability of the swagger codegen to generate a useful file upload, this is faked because it can't be suppressed for generation, too.
     */
    @PostMapping(EMPIRE_IMAGE_ENDPOINT)
    @Operation(summary = "Uploads the empires emblem.", operationId = "uploadEmpireEmblemBUTFAKE",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = File.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> uploadEmpireEmblem(@Nonnull @RequestBody final MultipartFile file) {
        Preconditions.checkNotNull(file, "file must not be empty");

        storageService.store(file, createEmpireEmblemFileName(getIdUser()));
        return ResponseEntity.ok(true);
    }

    @DeleteMapping(EMPIRE_IMAGE_ENDPOINT)
    @Operation(summary = "Deletes the empires emblem.", operationId = "deleteEmpireEmblem",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> deleteEmpireEmblem() {

        final File file = storageService.loadAsResource(createEmpireEmblemFileName(getIdUser()));
        if (file == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(file.delete());
    }

    @PostMapping(RPG_TEXTS)
    @Operation(summary = "Changes the empire texts.", operationId = "editRPGTextBlocks",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RPGTextBlocks.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RPGTextBlocks.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> editRPGTextBlocks(@Nonnull @RequestBody final RPGTextBlocks textBlocks) {
        Preconditions.checkNotNull(textBlocks, "textBlocks must not be empty");

        final Set<ConstraintViolation<RPGTextBlocks>> validate = validator.validate(textBlocks);
        if (validate.isEmpty()) {
            final RolePlaySetting forUser = rolePlayService.findForUser(getIdUser());
            if (forUser != null) {
                forUser.setLeftUpper(textBlocks.getLeftUpper());
                forUser.setRightUpper(textBlocks.getRightUpper());
                forUser.setLeftBottom(textBlocks.getLeftBottom());
                forUser.setRightBottom(textBlocks.getRightBottom());
                rolePlayService.save(forUser);
            }
            return ResponseEntity.ok(true);
        }
        throw new NotifyWebUserException("This must be changed.", validate);
    }

    @Nonnull
    private static String createEmpireEmblemFileName(final int idUser) {
        return EMPIRE_IMAGE_PREFIX + idUser;
    }
}
