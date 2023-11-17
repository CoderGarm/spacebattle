package de.yuga.spacebattle.rest.api;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.ResourceService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.rest.api.spacecrafts.ModuleApi;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PUBLIC_BASE_ENDPOINT;

@RestController
@Tag(name = "PublicResourcesApi")
@RequestMapping(value = "/" + PUBLIC_BASE_ENDPOINT + "/" + PublicResourcesApi.ENDPOINT + "/")
public class PublicResourcesApi extends BaseApi {

    public final static String ENDPOINT = "resources";

    @Nonnull
    private final ResourceService resourceService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ModuleService moduleService;

    @Nonnull
    private final BuildingService buildingService;

    public PublicResourcesApi(@Nonnull final ResourceService resourceService,
                              @Nonnull final UserService userService,
                              @Nonnull final ModuleService moduleService,
                              @Nonnull final BuildingService buildingService) {
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
        this.moduleService = Preconditions.checkNotNull(moduleService, "moduleService must not be empty");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService must not be empty");
    }

    @GetMapping("user-names")
    @Operation(summary = "Get all usernames.", operationId = "getUsernames",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = String.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getUsernames() {
        final Set<String> names = userService.findAll().stream().map(User::getUsername).collect(Collectors.toSet());
        return ResponseEntity.ok(names);
    }


    @GetMapping(value = ModuleApi.ARMOR_ENDPOINT + "/all")
    @Operation(summary = "Get all unlocked armors for the owner .", operationId = "getArmors",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Armor.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getArmors() {
        return ResponseEntity.ok(moduleService.findAllArmors().stream().map(a -> new Armor(a, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = ModuleApi.WEAPON_ENDPOINT + "/all")
    @Operation(summary = "Get all unlocked weapons for the owner .", operationId = "getWeapons",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Weapon.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getWeapons() {
        final List<de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon> allWeapon = moduleService.findAllWeapons();
        final List<de.yuga.spacebattle.rest.dto.spacecrafts.modules.Weapon> weaponList = allWeapon.stream()
                .map(w -> new de.yuga.spacebattle.rest.dto.spacecrafts.modules.Weapon(w, getPreferredLanguage()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(weaponList);
    }

    @GetMapping(value = ModuleApi.LAUNCHER_ENDPOINT + "/all")
    @Operation(summary = "Get all unlocked weapons for the owner .", operationId = "getLaunchers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Launcher.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getLaunchers() {
        return ResponseEntity.ok(moduleService.findAllLaunchers().stream().map(l -> new Launcher(l, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = ModuleApi.SIDEWALL_ENDPOINT + "/all")
    @Operation(summary = "Get all unlocked sidewalls for the owner .", operationId = "getSidewalls",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Sidewall.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getSidewalls() {
        return ResponseEntity.ok(moduleService.findAllSidewalls().stream().map(s -> new Sidewall(s, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = ModuleApi.PROPULSION_ENDPOINT + "/all")
    @Operation(summary = "Get all unlocked propulsions for the owner .", operationId = "getPropulsions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Propulsion.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPropulsions() {
        return ResponseEntity.ok(moduleService.findAllPropulsions().stream().map(p -> new Propulsion(p, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = ModuleApi.ELOKA_ENDPOINT + "/all")
    @Operation(summary = "Get all unlocked electronic warfare for the owner .", operationId = "getElectronicWarfare",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = ElectronicWarfare.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getElectronicWarfares() {
        return ResponseEntity.ok(moduleService.findAllElectronicWarfare().stream().map(e -> new ElectronicWarfare(e, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = ModuleApi.PASSIVE_ENDPOINT + "/all")
    @Operation(summary = "Get all unlocked passive modules for the owner .", operationId = "getPassiveModules",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = PassiveModule.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getPassiveModules() {
        return ResponseEntity.ok(moduleService.findAllPassiveModules().stream().map(p -> new PassiveModule(p, getPreferredLanguage())).collect(Collectors.toList()));
    }

    @GetMapping(value = "buildings/all")
    @Operation(summary = "Get all unlocked passive modules for the owner .", operationId = "getAllBuildings",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Building.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllBuildings() {
        return ResponseEntity.ok(buildingService.findAll().stream().map(p -> new Building(p, getPreferredLanguage())).collect(Collectors.toList()));
    }

    /*
    @GetMapping("system-coordinates")
    @Operation(summary = "Get star systems by coordinates.", operationId = "getAllSystemCoordinates",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CoordsBlob.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllSystemCoordinates() {
        final List<Coords> coords = resourceService.readStarSystems();
        return ResponseEntity.ok(new CoordsBlob(coords));
    }

    @GetMapping("wormhole-junctions")
    @Operation(summary = "Get star systems by coordinates.", operationId = "getAllWormholeJunctions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Junction.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllWormholeJunctions() {
        return ResponseEntity.ok(resourceService.readWormholes());
    }

    @GetMapping("distances")
    @Operation(summary = "Get the known distances.", operationId = "getAllDistances",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = DistanceElement.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllDistances() {
        final List<DistanceElement> coords = resourceService.getAllDistances();
        return ResponseEntity.ok(coords);
    }
    */
}
