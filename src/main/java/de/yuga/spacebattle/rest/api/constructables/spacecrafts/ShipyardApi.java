package de.yuga.spacebattle.rest.api.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassCreationService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "ShipyardApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + ShipyardApi.ENDPOINT + "/")
public class ShipyardApi {

    @Nonnull
    public static final String ENDPOINT = "shipyard";
    private static final String SHIP_CLASS_FOR_USER_ENDPOINT = "forUser";
    private static final String E_HULL_TYPE = "hullType";
    private static final String E_MODULE_TYPE = "moduleType";

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final ShipClassService shipClassService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ShipClassCreationService shipClassCreationService;

    @Nonnull
    private final Validator validator;

    @Autowired
    public ShipyardApi(@Nonnull final JobService jobService,
                       @Nonnull final ShipClassService shipClassService,
                       @Nonnull final UserService userService,
                       @Nonnull final ShipClassCreationService shipClassCreationService) {
        Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(shipClassCreationService, "shipClassCreationService shouldn't be null!");

        this.jobService = jobService;
        this.shipClassService = shipClassService;
        this.userService = userService;
        this.shipClassCreationService = shipClassCreationService;
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @GetMapping(value = SHIP_CLASS_FOR_USER_ENDPOINT + "/{idUser}")
    @Operation(summary = "Get all active ship classes for the owner .", operationId = "getShipClassesByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = ShipClass.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getShipClassesByUser(@PathVariable("idUser") final int idUser) {
        final User owner = userService.find(idUser);
        if (owner == null) {
            throw new NotifyWebUserException("There should be a questioned user.");
        }
        final List<de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass> allLatestByOwner = shipClassService.findAllLatestByOwner(owner);
        final List<ShipClass> shipClasses = allLatestByOwner.stream().map(ShipClass::new).collect(Collectors.toList());
        return ResponseEntity.ok(shipClasses);
    }

    @PostMapping(value = SHIP_CLASS_FOR_USER_ENDPOINT + "/{idUser}")
    @Operation(summary = "Get all active ship classes for the owner .", operationId = "setShipClass",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ShipClass.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> setShipClass(@PathVariable("idUser") final int idUser, @RequestBody ShipClass shipClass) {

        if (shipClass == null) {
            throw new NotifyWebUserException("There should be a ship class provided.");
        }

        final Set<ConstraintViolation<ShipClass>> validate = validator.validate(shipClass);
        if (!validate.isEmpty()) {
            throw new NotifyWebUserException("The provided class is not valid.", validate);
        }
        return ResponseEntity.ok(new ShipClass(shipClassCreationService.mapAndCreateShipClass(shipClass, idUser)));
    }

    @DeleteMapping(value = SHIP_CLASS_FOR_USER_ENDPOINT + "/{idUser}/{idShipClass}")
    @Operation(summary = "Marks a ship classes as deleted.", operationId = "deleteShipClass",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful"),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    @ResponseStatus(HttpStatus.OK)
    public void deleteShipClass(@PathVariable("idUser") final int idUser, @PathVariable("idShipClass") final int idShipClass) {

        shipClassService.delete(idUser, idShipClass);
    }

    @GetMapping(value = E_HULL_TYPE)
    @Operation(summary = "Get all EHullType.", operationId = "getEHullTypes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.enums.EHullType.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEHullTypes() {
        return ResponseEntity.ok(Arrays.stream(EHullType.values()).map(de.yuga.spacebattle.rest.dto.enums.EHullType::new).collect(Collectors.toList()));
    }

    @GetMapping(value = E_MODULE_TYPE)
    @Operation(summary = "Get all EModuleType.", operationId = "getEModuleTypes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.enums.EModuleType.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEModuleTypes() {
        return ResponseEntity.ok(Arrays.stream(EModuleType.values()).map(de.yuga.spacebattle.rest.dto.enums.EModuleType::new).collect(Collectors.toList()));
    }

    @GetMapping(value = SHIP_CLASS_FOR_USER_ENDPOINT + "/{idUser}/{className}")
    @Operation(summary = "Checks if the name for a ship class is free for new classes only.", operationId = "checkClassName",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> checkClassName(@PathVariable("idUser") final int idUser, @PathVariable("className") final String className) {

        if (StringUtils.isBlank(className) || className.trim().length() < 3 || className.trim().length() > 30) {
            throw new NotifyWebUserException("The name of the class did not suit the requirements.");
        }

        return ResponseEntity.ok(shipClassService.checkIfClassNameIsFree(idUser, className));
    }
}
