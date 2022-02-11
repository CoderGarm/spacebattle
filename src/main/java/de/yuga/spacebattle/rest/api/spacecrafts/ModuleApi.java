package de.yuga.spacebattle.rest.api.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.spacecraft.HullService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.spacecrafts.HullList;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.*;
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

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Api(tags = ModuleApi.API)
@RolesAllowed("ROLE_USER")
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + ModuleApi.ENDPOINT + "/")
public class ModuleApi {

    public static final String API = "ModuleApi";
    public static final String ENDPOINT = "modules";
    private static final String ARMOR_ENDPOINT = "armor";
    private static final String WEAPON_ENDPOINT = "weapon";
    private static final String LAUNCHER_ENDPOINT = "launcher";
    private static final String SIDEWALL_ENDPOINT = "sidewall";
    private static final String PROPULSION_ENDPOINT = "propulsion";
    private static final String HULL_ENDPOINT = "hull";
    private static final String ELOKA_ENDPOINT = "eloka";
    private static final String PASSIVE_ENDPOINT = "passive";
    private static final String AMMUNITION_ENDPOINT = "ammunition";

    @Nonnull
    private final ModuleService moduleService;

    @Nonnull
    private final HullService hullService;

    @Nonnull
    private final UserService userService;

    @Autowired
    public ModuleApi(@Nonnull final ModuleService moduleService,
                     @Nonnull final HullService hullService,
                     @Nonnull final UserService userService) {
        Preconditions.checkNotNull(moduleService, "moduleService shouldn't be null!");
        Preconditions.checkNotNull(hullService, "hullService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.moduleService = moduleService;
        this.hullService = hullService;
        this.userService = userService;
    }

    @GetMapping(value = ARMOR_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all unlocked armors for the owner .", nickname = "getArmorsByUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ArmorList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getArmorsByUser(@PathVariable("idUser") final int idUser) {
        final User owner = userService.findWithResearches(idUser);
        if (owner == null) {
            throw new NotifyWebUserException("There should be a questioned user.");
        }

        return ResponseEntity.ok(new ArmorList(moduleService.findAllArmorByUser(owner)));
    }

    @GetMapping(value = WEAPON_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all unlocked weapons for the owner .", nickname = "getWeaponsByUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = WeaponList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getWeaponsByUser(@PathVariable("idUser") final int idUser) {
        final User owner = userService.findWithResearches(idUser);
        return ResponseEntity.ok(new WeaponList(moduleService.findAllWeaponByUser(owner)));
    }

    @GetMapping(value = LAUNCHER_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all unlocked weapons for the owner .", nickname = "getLaunchersByUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LauncherList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getLaunchersByUser(@PathVariable("idUser") final int idUser) {
        final User owner = userService.findWithResearches(idUser);
        return ResponseEntity.ok(new LauncherList(moduleService.findAllLauncherByUser(owner)));
    }

    @GetMapping(value = SIDEWALL_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all unlocked sidewalls for the owner .", nickname = "getSidewallsByUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SidewallList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getSidewallsByUser(@PathVariable("idUser") final int idUser) {
        final User owner = userService.findWithResearches(idUser);
        if (owner == null) {
            throw new NotifyWebUserException("There should be a questioned user.");
        }

        return ResponseEntity.ok(new SidewallList(moduleService.findAllSidewallByUser(owner)));
    }

    @GetMapping(value = PROPULSION_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all unlocked propulsions for the owner .", nickname = "getPropulsionsByUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PropulsionList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPropulsionsByUser(@PathVariable("idUser") final int idUser) {
        final User owner = userService.findWithResearches(idUser);
        if (owner == null) {
            throw new NotifyWebUserException("There should be a questioned user.");
        }

        return ResponseEntity.ok(new PropulsionList(moduleService.findAllPropulsionByUser(owner)));
    }

    @GetMapping(value = HULL_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all unlocked hulls for the owner .", nickname = "getHullsByUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HullList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getHullsByUser(@PathVariable("idUser") final int idUser) {
        final User owner = userService.findWithResearches(idUser);
        if (owner == null) {
            throw new NotifyWebUserException("There should be a questioned user.");
        }

        return ResponseEntity.ok(new HullList(hullService.findAllByUser(owner)));
    }

    @GetMapping(value = ELOKA_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all unlocked electronic warfare for the owner .", nickname = "getElectronicWarfareByUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ElectronicWarfareList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getElectronicWarfaresByUser(@PathVariable("idUser") final int idUser) {
        final User owner = userService.findWithResearches(idUser);
        if (owner == null) {
            throw new NotifyWebUserException("There should be a questioned user.");
        }

        return ResponseEntity.ok(new ElectronicWarfareList(moduleService.findAllElectronicWarfareByUser(owner)));
    }

    @GetMapping(value = PASSIVE_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all unlocked passive modules for the owner .", nickname = "getPassiveModulesByUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PassiveModuleList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPassiveModulesByUser(@PathVariable("idUser") final int idUser) {
        final User owner = userService.findWithResearches(idUser);
        if (owner == null) {
            throw new NotifyWebUserException("There should be a questioned user.");
        }

        return ResponseEntity.ok(new PassiveModuleList(moduleService.findAllPassiveModuleByUser(owner)));
    }

    @GetMapping(value = AMMUNITION_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all unlocked ammunition modules for the owner .", nickname = "getAmmunitionModulesByUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AmmunitionModuleList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAmmunitionModulesByUser(@PathVariable("idUser") final int idUser) {
        final User owner = userService.findWithResearches(idUser);
        if (owner == null) {
            throw new NotifyWebUserException("There should be a questioned user.");
        }

        return ResponseEntity.ok(new AmmunitionModuleList(moduleService.findAllAmmunitionModulesByUser(owner)));
    }
}
