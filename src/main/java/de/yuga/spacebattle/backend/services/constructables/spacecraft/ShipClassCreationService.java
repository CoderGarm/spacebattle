package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.spacecraft.HullService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapabilities;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.ShipyardConstructionSelection;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.AmmunitionModule;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.PassiveModule;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ShipClassCreationService {

    @Nonnull
    private final ShipClassService shipClassService;

    @Nonnull
    private final ModuleService moduleService;

    @Nonnull
    private final HullService hullService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final Validator validator;

    public ShipClassCreationService(@Nonnull final ShipClassService shipClassService,
                                    @Nonnull final ModuleService moduleService,
                                    @Nonnull final HullService hullService,
                                    @Nonnull final UserService userService,
                                    @Nonnull final FleetService fleetService) {
        Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        Preconditions.checkNotNull(moduleService, "moduleService shouldn't be null!");
        Preconditions.checkNotNull(hullService, "hullService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");

        this.shipClassService = shipClassService;
        this.moduleService = moduleService;
        this.hullService = hullService;
        this.userService = userService;
        this.fleetService = fleetService;
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Creates a ship class entity from the user's input.
     *
     * @param shipClass the ship class from the web ui
     * @param idUser    the id user of the owner
     * @return the generated class
     */
    @Nonnull
    public ShipClass mapAndCreateShipClass(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass shipClass, final int idUser) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final Set<ConstraintViolation<de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass>> validate = validator.validate(shipClass);
        if (!validate.isEmpty()) {
            throw new NotifyWebUserException("The provided class is not valid.", validate);
        }
        final ShipClass entity = mapShipClassToEntity(shipClass, idUser);

        return shipClassService.save(entity);
    }

    @Nonnull
    private ShipClass mapShipClassToEntity(@Nonnull de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass shipClass, final int idUser) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final User owner = userService.find(idUser);
        if (owner == null) {
            throw new NotifyWebUserException("There should be a questioned user.");
        }

        ShipClass entity = null;
        final Integer idShipClass = shipClass.getIdShipClass();
        if (idShipClass != null) {
            final boolean shipClassInUse = fleetService.isShipClassInUse(idShipClass);
            if (!shipClassInUse) {
                // if class is not in use just modify it - otherwise create a new one
                entity = shipClassService.find(idShipClass);
                if (entity == null) {
                    throw new NotifyWebUserException("This should not happen - don't try to use an ID without knowing it.");
                }
            }
        }

        if (entity == null) {
            final Hull hullEntity = hullService.find(shipClass.getHull().getIdHull());
            if (hullEntity == null) {
                throw new NotifyWebUserException("There should be a hull present.");
            }

            final Integer idPredecessor = shipClass.getIdPredecessor();
            ShipClass predecessor = null;
            if (idPredecessor != null) {
                predecessor = shipClassService.find(idPredecessor);
            }
            entity = new ShipClass(owner, shipClass.getName(), hullEntity, predecessor);
        }

        mapSingularities(shipClass, entity);
        mapAmmunitionModules(shipClass, entity);
        mapPassiveModules(shipClass, entity);
        mapWeaponModules(shipClass, entity);
        return entity;
    }

    /**
     * Maps the singular occurring modules.
     *
     * @param shipClass the dtp
     * @param entity    the entity
     */
    private void mapSingularities(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass shipClass,
                                  @Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        if (shipClass.getArmor() != null) {
            final Armor module = moduleService.findArmorById(shipClass.getArmor().getBaseModule().getIdModule());
            entity.setArmor(module);
        }

        if (shipClass.getPropulsion() != null) {
            final Propulsion module = moduleService.findPropulsionById(shipClass.getPropulsion().getBaseModule().getIdModule());
            entity.setPropulsion(module);
        }

        if (shipClass.getElectronicWarfare() != null) {
            final ElectronicWarfare module = moduleService.findElectronicWarfareById(shipClass.getElectronicWarfare().getBaseModule().getIdModule());
            entity.setElectronicWarfare(module);
        }

        if (shipClass.getSidewall() != null) {
            final Sidewall module = moduleService.findSidewallById(shipClass.getSidewall().getBaseModule().getIdModule());
            entity.setSidewall(module);
        }
    }

    /**
     * Maps the ammunition modules.
     *
     * @param shipClass the dto
     * @param entity    the entity
     */
    private void mapAmmunitionModules(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass shipClass,
                                      @Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        final List<Integer> ammunitionModulesById = shipClass.getAmmunitionFittings().stream()
                .map(AmmunitionFitting::getAmmunitionModule)
                .map(AmmunitionModule::getBaseModule)
                .map(BaseModule::getIdModule)
                .collect(Collectors.toList());

        final Map<Integer, de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule> integerAmmunitionModuleMap =
                moduleService.findAmmunitionModulesById(ammunitionModulesById).stream()
                        .collect(Collectors.toMap(de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule::getId, Function.identity()));

        final Set<de.yuga.spacebattle.backend.entities.spacecrafts.details.AmmunitionFitting> ammunitionFittings = shipClass.getAmmunitionFittings().stream().map(ammunitionFitting -> {
            final int idModule = ammunitionFitting.getAmmunitionModule().getBaseModule().getIdModule();
            final de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule ammunitionModule = integerAmmunitionModuleMap.get(idModule);
            final int amount = ammunitionFitting.getAmount();
            return new de.yuga.spacebattle.backend.entities.spacecrafts.details.AmmunitionFitting(ammunitionModule, amount);
        }).collect(Collectors.toSet());
        entity.setAmmunitionFittings(ammunitionFittings);
    }

    /**
     * Maps the passive modules.
     *
     * @param shipClass the dto
     * @param entity    the entity
     */
    private void mapPassiveModules(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass shipClass,
                                   @Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        final List<Integer> passiveModulesById = shipClass.getSupportFittings().stream()
                .map(SupportFitting::getPassiveModule)
                .map(PassiveModule::getBaseModule)
                .map(BaseModule::getIdModule)
                .collect(Collectors.toList());

        final Map<Integer, de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule> integerPassiveModuleMap =
                moduleService.findPassiveModulesById(passiveModulesById).stream()
                        .collect(Collectors.toMap(de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule::getId, Function.identity()));

        final Set<de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting> passiveFittings = shipClass.getSupportFittings().stream().map(passiveFitting -> {
            final int idModule = passiveFitting.getPassiveModule().getBaseModule().getIdModule();
            final de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule passiveModule = integerPassiveModuleMap.get(idModule);
            final int amount = passiveFitting.getAmount();
            return new de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting(passiveModule, amount);
        }).collect(Collectors.toSet());
        entity.setSupportFittings(passiveFittings);
    }

    /**
     * Maps the weapon modules.
     *
     * @param shipClass the dto
     * @param entity    the entity
     */
    private void mapWeaponModules(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass shipClass,
                                  @Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        final List<Integer> weaponModulesById = shipClass.getFittings().stream()
                .map(AlignedFitting::getWeapon)
                .filter(Objects::nonNull)
                .map(Weapon::getBaseModule)
                .map(BaseModule::getIdModule)
                .collect(Collectors.toList());

        final Map<Integer, de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon> integerWeaponModuleMap = moduleService
                .findWeaponsById(weaponModulesById).stream()
                .collect(Collectors.toMap(de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon::getId, Function.identity()));

        final List<Integer> launcherModulesById = shipClass.getFittings().stream()
                .map(AlignedFitting::getLauncher)
                .filter(Objects::nonNull)
                .map(de.yuga.spacebattle.rest.dto.spacecrafts.modules.Launcher::getBaseModule)
                .map(BaseModule::getIdModule)
                .collect(Collectors.toList());

        final Map<Integer, de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher> integerLauncherModuleMap = moduleService
                .findLaunchersById(launcherModulesById).stream()
                .collect(Collectors.toMap(de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher::getId, Function.identity()));

        final Set<de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting> weaponFittings = shipClass.getFittings().stream().map(weaponFitting -> {
            final int amount = weaponFitting.getAmount();
            final EWeaponAlignment weaponAlignment = weaponFitting.getWeaponAlignment();
            if (weaponFitting.getWeapon() != null) {
                final int idModule = weaponFitting.getWeapon().getBaseModule().getIdModule();
                final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon weaponModule = integerWeaponModuleMap.get(idModule);
                return new de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting(weaponAlignment, weaponModule, amount);
            }
            if (weaponFitting.getLauncher() != null) {
                final int idModule = weaponFitting.getLauncher().getBaseModule().getIdModule();
                final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher launcherModule = integerLauncherModuleMap.get(idModule);
                return new de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting(weaponAlignment, launcherModule, amount);
            }
            throw new NotifyWebUserException("There should be always one weapon system in the fitting!");
        }).collect(Collectors.toSet());
        entity.setFittings(weaponFittings);
    }

    /**
     * Returns the costs of the complete shipyard job.
     *
     * @param shipyardConstructionOrder the order
     * @return the order costs
     */
    @Nonnull
    public ResourceDeposit getCosts(@Nonnull final List<ShipyardConstructionSelection> shipyardConstructionOrder) {
        Preconditions.checkNotNull(shipyardConstructionOrder, "shipyardConstructionOrder shouldn't be null!");

        final Map<Integer, Integer> amountsByIdShipClasses = shipyardConstructionOrder
                .stream()
                .collect(Collectors.toMap(ShipyardConstructionSelection::getIdShipClass, ShipyardConstructionSelection::getAmount));

        final Map<Integer, ResourceDeposit> resourceDepositMap = shipClassService.find(amountsByIdShipClasses.keySet())
                .stream()
                .collect(Collectors.toMap(ShipClass::getId, ShipClass::getCostsOverall));

        final ResourceDeposit jobCosts = new ResourceDeposit(EDepositType.COSTS);
        resourceDepositMap.forEach((idShipClass, costs) -> {
            final Integer amount = amountsByIdShipClasses.get(idShipClass);
            Arrays.stream(EEducationType.values()).forEach(eEducationType -> {
                long crewAmountByType = costs.getCrewRequirement().getCrewAmountByType(eEducationType);
                jobCosts.updateCrewRequirement(eEducationType, crewAmountByType * amount);
            });
            Arrays.stream(EResourceType.values()).forEach(eResourceType -> {
                long resourceAmountByType = costs.getResourceAmountByType(eResourceType);
                jobCosts.updateResource(eResourceType, resourceAmountByType * amount);
            });
        });
        return jobCosts;
    }

    /**
     * Returns the costs of a possible ship class.
     *
     * @param shipClass the possible class
     * @param idUser    the owner
     * @return the costs
     */
    @Nonnull
    public ResourceDeposit getCosts(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass shipClass, final int idUser) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final ShipClass entity = mapShipClassToEntity(shipClass, idUser);
        return entity.getCostsOverall();
    }

    @Nonnull
    public SpacecraftCapabilities getCaps(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass shipClass, final int idUser) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final ShipClass entity = mapShipClassToEntity(shipClass, idUser);
        return new SpacecraftCalculator().getSpaceCraftCapabilities(entity);
    }
}
