package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
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
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.spacecraft.HullService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapabilities;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapacityAreas;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.ShipyardConstructionSelection;
import de.yuga.spacebattle.rest.dto.spacecrafts.PropulsionCapacity;
import de.yuga.spacebattle.rest.dto.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.AmmunitionModule;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.PassiveModule;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
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
    private final Validator validator;

    public ShipClassCreationService(@Nonnull final ShipClassService shipClassService,
                                    @Nonnull final ModuleService moduleService,
                                    @Nonnull final HullService hullService,
                                    @Nonnull final UserService userService) {
        Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        Preconditions.checkNotNull(moduleService, "moduleService shouldn't be null!");
        Preconditions.checkNotNull(hullService, "hullService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.shipClassService = shipClassService;
        this.moduleService = moduleService;
        this.hullService = hullService;
        this.userService = userService;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nonnull
    public ShipClass createShipClass(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClassMock shipClass, final int idUser) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final User user = userService.find(idUser);
        assert shipClass.getHull() != null;
        final Hull hull = hullService.find(shipClass.getHull().getIdHull());

        assert user != null;
        assert shipClass.getName() != null;
        assert hull != null;
        final ShipClass entity = mapShipClassMockToEntity(shipClass, new ShipClass(user, shipClass.getName(), hull, null));
        final Set<ConstraintViolation<ShipClass>> validate = validator.validate(entity);
        if (!validate.isEmpty()) {
            throw new NotifyWebUserException("The provided class is not valid.", validate);
        }
        return shipClassService.save(entity);
    }

    @Nonnull
    public ShipClass updateShipClassToEntity(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass shipClass, final int idUser) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final ShipClass entity = mapUpdatedShipClass(shipClass, idUser);
        final Set<ConstraintViolation<ShipClass>> validate = validator.validate(entity);
        if (!validate.isEmpty()) {
            throw new NotifyWebUserException("The provided class is not valid.", validate);
        }
        return shipClassService.save(entity);
    }

    @Nonnull
    private ShipClass mapUpdatedShipClass(@Nonnull de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass shipClass, final int idUser) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final User owner = userService.find(idUser);
        if (owner == null) {
            throw new NotifyWebUserException("There should be a questioned user.");
        }

        final Hull hull = hullService.find(shipClass.getHull().getIdHull());
        assert hull != null;

        final Integer idPredecessor = shipClass.getIdPredecessor();
        assert idPredecessor != null;
        ShipClass predecessor = shipClassService.find(idPredecessor);
        ShipClass entity = new ShipClass(owner, shipClass.getName(), hull, predecessor);

        mapSingularities(shipClass, entity);
        mapAmmunitionModules(shipClass, entity);
        mapPassiveModules(shipClass, entity);
        mapWeaponModules(shipClass, entity);
        return entity;
    }

    @Nonnull
    private ShipClass mapShipClassMockToEntity(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClassData shipClass, @Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(entity, "entity must not be empty");

        if (shipClass.getHull() != null && entity.getHull() == null) {
            final Hull hull = hullService.find(shipClass.getHull().getIdHull());
            entity.setHull(hull);
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
    private void mapSingularities(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClassData shipClass,
                                  @Nonnull final ShipClass entity) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        if (shipClass.getArmor() != null) {
            final Armor module = moduleService.findArmorById(shipClass.getArmor().getBaseModule().getIdModule());
            entity.setArmor(module);
        }

        if (shipClass.getPropulsion() != null) {
            final Propulsion module = moduleService.findPropulsionById(shipClass.getPropulsion().getIdModule());
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
    private void mapAmmunitionModules(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClassData shipClass,
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

        final Set<de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting> ammunitionFittings = shipClass.getAmmunitionFittings().stream().map(ammunitionFitting -> {
            final int idModule = ammunitionFitting.getAmmunitionModule().getBaseModule().getIdModule();
            final de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule ammunitionModule = integerAmmunitionModuleMap.get(idModule);
            final int amount = ammunitionFitting.getAmount();
            return new de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting(ammunitionModule, amount);
        }).collect(Collectors.toSet());
        entity.setAmmunitionFittings(ammunitionFittings);
    }

    /**
     * Maps the passive modules.
     *
     * @param shipClass the dto
     * @param entity    the entity
     */
    private void mapPassiveModules(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClassData shipClass,
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

        final Set<de.yuga.spacebattle.backend.entities.spacecrafts.fittings.SupportFitting> passiveFittings = shipClass.getSupportFittings().stream().map(passiveFitting -> {
            final int idModule = passiveFitting.getPassiveModule().getBaseModule().getIdModule();
            final de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule passiveModule = integerPassiveModuleMap.get(idModule);
            final int amount = passiveFitting.getAmount();
            return new de.yuga.spacebattle.backend.entities.spacecrafts.fittings.SupportFitting(passiveModule, amount);
        }).collect(Collectors.toSet());
        entity.setSupportFittings(passiveFittings);
    }

    /**
     * Maps the weapon modules.
     *
     * @param shipClass the dto
     * @param entity    the entity
     */
    private void mapWeaponModules(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClassData shipClass,
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

        final Set<de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting> weaponFittings = shipClass.getFittings().stream().map(weaponFitting -> {
            final int amount = weaponFitting.getAmount();
            final EWeaponAlignment weaponAlignment = weaponFitting.getWeaponAlignment();
            if (weaponFitting.getWeapon() != null) {
                final int idModule = weaponFitting.getWeapon().getBaseModule().getIdModule();
                final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon weaponModule = integerWeaponModuleMap.get(idModule);
                return new de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting(weaponAlignment, weaponModule, amount);
            }
            if (weaponFitting.getLauncher() != null) {
                final int idModule = weaponFitting.getLauncher().getBaseModule().getIdModule();
                final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher launcherModule = integerLauncherModuleMap.get(idModule);
                return new de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting(weaponAlignment, launcherModule, amount);
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
                .collect(Collectors.toMap(ShipClass::getId, ShipClass::getCosts));

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

    @Nonnull
    public ResourceDeposit getCosts(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClassMock shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final ShipClass entity = mapShipClassMockToEntity(shipClass, new ShipClass());
        return entity.getCosts();
    }

    @Nonnull
    public SpacecraftCapabilities getShipClassCapabilities(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClassMock shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final ShipClass entity = mapShipClassMockToEntity(shipClass, new ShipClass());
        return new SpacecraftCalculator().getSpaceCraftCapabilities(entity);
    }

    @Nonnull
    public SpacecraftCapacityAreas getShipClassCapacities(@Nonnull final de.yuga.spacebattle.rest.dto.spacecrafts.ShipClassMock shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final ShipClass entity = mapShipClassMockToEntity(shipClass, new ShipClass());
        return new SpacecraftCalculator().getSpacecraftCapacityAreas(entity);
    }

    public List<PropulsionCapacity> getPropulsionCapacity(final int idHull, final int idPropulsion) {

        final Hull hull = hullService.find(idHull);
        Preconditions.checkNotNull(hull, "hull must not be empty");
        final Propulsion propulsion = moduleService.findPropulsionById(idPropulsion);
        Preconditions.checkNotNull(propulsion, "propulsion must not be empty");

        final ShipClass shipClass = new ShipClass();
        shipClass.setHull(hull);
        shipClass.setPropulsion(propulsion);

        final ArrayList<PropulsionCapacity> result = new ArrayList<>();
        for (final EHyperBand hyperBand : EHyperBand.values()) {
            final Acceleration acceleration = shipClass.getAcceleration(hyperBand);
            Velocity velocity = Velocity.ZERO;
            if (acceleration.getValue().compareTo(BigDecimal.ZERO) != 0) {
                velocity = new Velocity(hyperBand.getEffectiveTopSpeed(propulsion.getTechnologyType()), EDistanceMetric.M, ETimeMetric.SECOND);
            }
            result.add(new PropulsionCapacity(acceleration, velocity));
        }
        return result;
    }
}
