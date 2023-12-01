package de.yuga.spacebattle.rest.api;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.services.ResourceService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.OrbitalModuleService;
import de.yuga.spacebattle.backend.services.researches.TechTreeService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.rest.api.researches.ResearchApi;
import de.yuga.spacebattle.rest.api.spacecrafts.ModuleApi;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import de.yuga.spacebattle.rest.dto.enums.EEducationType;
import de.yuga.spacebattle.rest.dto.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.researches.ResearchTree;
import de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PUBLIC_BASE_ENDPOINT;

@RestController
@Tag(name = "PublicResourcesApi")
@RequestMapping(value = "/" + PUBLIC_BASE_ENDPOINT + "/" + PublicResourcesApi.ENDPOINT + "/")
public class PublicResourcesApi extends BaseApi {

    public final static String ENDPOINT = "resources";
    private static final String RESOURCE_TYPES_ENDPOINT = "types";
    private static final String HUMAN_RESOURCE_TYPES_ENDPOINT = "educationTypes";
    private static final String E_HULL_TYPE_ENDPOINT = "shipClassType";
    private static final String E_MODULE_TYPE_ENDPOINT = "moduleType";
    private static final String E_PRODUCTION_CATEGORY = "EProductionCategory";
    private static final String E_REFINEMENT_SEQUENCE = "ERefinementSequence";

    @Nonnull
    private final ResourceService resourceService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ModuleService moduleService;

    @Nonnull
    private final BuildingService buildingService;

    @Nonnull
    private final OrbitalModuleService orbitalModuleService;

    @Nonnull
    private final TechTreeService techTreeService;

    public PublicResourcesApi(@Nonnull final ResourceService resourceService,
                              @Nonnull final UserService userService,
                              @Nonnull final ModuleService moduleService,
                              @Nonnull final BuildingService buildingService,
                              @Nonnull final OrbitalModuleService orbitalModuleService,
                              @Nonnull final TechTreeService techTreeService) {
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
        this.moduleService = Preconditions.checkNotNull(moduleService, "moduleService must not be empty");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService must not be empty");
        this.orbitalModuleService = Preconditions.checkNotNull(orbitalModuleService, "orbitalModuleService must not be empty");
        this.techTreeService = Preconditions.checkNotNull(techTreeService, "techTreeService must not be empty");
    }

    @GetMapping(value = ResearchApi.TREE_ENDPOINT)
    @Operation(summary = "Get all researches with their unlocking research.", operationId = "getOpenTechTree",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResearchTree.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getOpenTechTree() {
        return ResponseEntity.ok(techTreeService.getResearchTree(getPreferredLanguage()));
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
        return ResponseEntity.ok(moduleService.findAllArmors().stream().map(a -> new Armor(a, getPreferredLanguage()).withCosts(a.getCosts())).collect(Collectors.toList()));
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
        return ResponseEntity.ok(moduleService.findAllWeapons().stream()
                .map(w -> new Weapon(w, getPreferredLanguage()).withCosts(w.getCosts()))
                .collect(Collectors.toList()));
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
        return ResponseEntity.ok(moduleService.findAllLaunchers().stream().map(l -> new Launcher(l, getPreferredLanguage()).withCosts(l.getCosts())).collect(Collectors.toList()));
    }

    @GetMapping(value = ModuleApi.MISSILE_ENDPOINT + "/all")
    @Operation(summary = "Get all unlocked weapons for the owner .", operationId = "getMissiles",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Missile.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getMissiles() {
        return ResponseEntity.ok(moduleService.findAllMissiles().stream().map(l -> new Missile(l, getPreferredLanguage()).withCosts(l.getCosts())).collect(Collectors.toList()));
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
        return ResponseEntity.ok(moduleService.findAllSidewalls().stream().map(s -> new Sidewall(s, getPreferredLanguage()).withCosts(s.getCosts())).collect(Collectors.toList()));
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
        return ResponseEntity.ok(moduleService.findAllElectronicWarfare().stream().map(e -> new ElectronicWarfare(e, getPreferredLanguage()).withCosts(e.getCosts())).collect(Collectors.toList()));
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
        return ResponseEntity.ok(moduleService.findAllPassiveModules().stream().map(p -> new PassiveModule(p, getPreferredLanguage()).withCosts(p.getCosts())).collect(Collectors.toList()));
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
        return ResponseEntity.ok(buildingService.findAll().stream().map(p -> new Building(p, getPreferredLanguage()).withCosts(p.getCosts())).collect(Collectors.toList()));
    }

    @GetMapping("orbitals/all")
    @Operation(summary = "Get all orbital modules.", operationId = "getOrbitalModules",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.spacecrafts.OrbitalModule.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getOrbitalModules() {
        final List<OrbitalModule> modules = orbitalModuleService.findAll();
        return ResponseEntity.ok(modules.stream().map(m -> new de.yuga.spacebattle.rest.dto.spacecrafts.OrbitalModule(m, getPreferredLanguage()).withCosts(m.getCosts())).collect(Collectors.toList()));
    }

    @GetMapping(value = RESOURCE_TYPES_ENDPOINT)
    @Operation(summary = "Get all EResourceTypes.", operationId = "getEResourceTypes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = EResourceType.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEResourceTypes() {
        return ResponseEntity.ok(Arrays.stream(de.yuga.spacebattle.backend.enums.EResourceType.values())
                .map(EResourceType::new)
                .collect(Collectors.toList()));
    }

    @GetMapping(value = HUMAN_RESOURCE_TYPES_ENDPOINT)
    @Operation(summary = "Get all EEducationTypes.", operationId = "getEEducationTypes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = EEducationType.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEEducationTypes() {
        return ResponseEntity.ok(Arrays.stream(de.yuga.spacebattle.backend.enums.EEducationType.values())
                .map(EEducationType::new)
                .collect(Collectors.toList()));
    }

    @GetMapping(value = E_HULL_TYPE_ENDPOINT)
    @Operation(summary = "Get all EShipClassTypes.", operationId = "getEShipClassTypes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.enums.EShipClassType.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEShipClassTypes() {
        return ResponseEntity.ok(Arrays.stream(EShipClassType.values()).map(de.yuga.spacebattle.rest.dto.enums.EShipClassType::new).collect(Collectors.toList()));
    }

    @GetMapping(value = E_MODULE_TYPE_ENDPOINT)
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

    @GetMapping(value = "/" + E_PRODUCTION_CATEGORY)
    @Operation(summary = "Get all EProductionCategories.", operationId = "getEProductionCategories",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = EProductionCategory.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEProductionCategories() {
        return ResponseEntity.ok(Arrays.stream(EProductionCategory.values()).collect(Collectors.toList()));
    }

    @GetMapping(value = "/" + E_REFINEMENT_SEQUENCE)
    @Operation(summary = "Get all ERefinementSequences.", operationId = "getERefinementSequences",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = de.yuga.spacebattle.rest.dto.enums.ERefinementSequence.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getERefinementSequences() {
        return ResponseEntity.ok(Arrays.stream(ERefinementSequence.values()).map(de.yuga.spacebattle.rest.dto.enums.ERefinementSequence::new).collect(Collectors.toList()));
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
