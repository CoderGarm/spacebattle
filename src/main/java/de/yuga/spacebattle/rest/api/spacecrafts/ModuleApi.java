package de.yuga.spacebattle.rest.api.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.spacecraft.HullService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.spacecrafts.Hull;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.*;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = ModuleApi.API)
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + ModuleApi.ENDPOINT + "/")
public class ModuleApi extends BaseApi {

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

    @GetMapping(value = ARMOR_ENDPOINT)
    @Operation(summary = "Get all unlocked armors for the owner .", operationId = "getArmorsByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Armor.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getArmorsByUser() {
        return ResponseEntity.ok(moduleService.findAllArmorByUser(getIdUser()).stream().map(a -> new Armor(a, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = WEAPON_ENDPOINT)
    @Operation(summary = "Get all unlocked weapons for the owner .", operationId = "getWeaponsByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Weapon.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getWeaponsByUser() {
        final List<de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon> allWeaponByUser = moduleService.findAllWeaponByUser(getIdUser());
        final List<de.yuga.spacebattle.rest.dto.spacecrafts.modules.Weapon> weaponList = allWeaponByUser.stream()
                .map(w -> new de.yuga.spacebattle.rest.dto.spacecrafts.modules.Weapon(w, getPreferredLanguage()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(weaponList);
    }

    @GetMapping(value = LAUNCHER_ENDPOINT)
    @Operation(summary = "Get all unlocked weapons for the owner .", operationId = "getLaunchersByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Launcher.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getLaunchersByUser() {
        final User owner = userService.findWithResearches(getIdUser());
        return ResponseEntity.ok(moduleService.findAllLauncherByUser(owner).stream().map(l -> new Launcher(l, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = SIDEWALL_ENDPOINT)
    @Operation(summary = "Get all unlocked sidewalls for the owner .", operationId = "getSidewallsByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Sidewall.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getSidewallsByUser() {
        return ResponseEntity.ok(moduleService.findAllSidewallByUser(getIdUser()).stream().map(s -> new Sidewall(s, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = PROPULSION_ENDPOINT)
    @Operation(summary = "Get all unlocked propulsions for the owner .", operationId = "getPropulsionsByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Propulsion.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPropulsionsByUser() {
        return ResponseEntity.ok(moduleService.findAllPropulsionByUser(getIdUser()).stream().map(p -> new Propulsion(p, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = HULL_ENDPOINT)
    @Operation(summary = "Get all unlocked hulls for the owner .", operationId = "getHullsByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Hull.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getHullsByUser() {
        final User owner = userService.findWithResearches(getIdUser());

        return ResponseEntity.ok(hullService.findAllByUser(owner).stream().map(h -> new Hull(h, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = ELOKA_ENDPOINT)
    @Operation(summary = "Get all unlocked electronic warfare for the owner .", operationId = "getElectronicWarfareByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = ElectronicWarfare.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getElectronicWarfaresByUser() {
        return ResponseEntity.ok(moduleService.findAllElectronicWarfareByUser(getIdUser()).stream().map(e -> new ElectronicWarfare(e, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = PASSIVE_ENDPOINT)
    @Operation(summary = "Get all unlocked passive modules for the owner .", operationId = "getPassiveModulesByUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = PassiveModule.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPassiveModulesByUser() {
        final User owner = userService.findWithResearches(getIdUser());

        return ResponseEntity.ok(moduleService.findAllPassiveModuleByUser(owner).stream().map(p -> new PassiveModule(p, getPreferredLanguage())).collect(Collectors.toList()));
    }
}
