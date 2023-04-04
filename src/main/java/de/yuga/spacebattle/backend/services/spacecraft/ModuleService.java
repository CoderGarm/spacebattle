package de.yuga.spacebattle.backend.services.spacecraft;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.spacecraft.Fitting;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.repositories.spacecraft.MissileRepository;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.*;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"DeprecatedIsStillUsed", "unused"})
@Service
public class ModuleService {

    @Nonnull
    private final NamedTechLevelRepository namedTechLevelRepository;

    @Nonnull
    private final ArmorRepository armorRepository;

    @Nonnull
    private final WeaponRepository weaponRepository;

    @Nonnull
    private final LauncherRepository launcherRepository;

    @Nonnull
    private final MissileRepository missileRepository;

    @Nonnull
    private final SidewallRepository sidewallRepository;

    @Nonnull
    private final PropulsionRepository propulsionRepository;

    @Nonnull
    private final ElectronicWarfareRepository electronicWarfareRepository;

    @Nonnull
    private final PassiveModuleRepository passiveModuleRepository;

    public ModuleService(@Nonnull final NamedTechLevelRepository namedTechLevelRepository,
                         @Nonnull final ArmorRepository armorRepository,
                         @Nonnull final WeaponRepository weaponRepository,
                         @Nonnull final LauncherRepository launcherRepository,
                         @Nonnull final MissileRepository missileRepository,
                         @Nonnull final SidewallRepository sidewallRepository,
                         @Nonnull final PropulsionRepository propulsionRepository,
                         @Nonnull final ElectronicWarfareRepository electronicWarfareRepository,
                         @Nonnull final PassiveModuleRepository passiveModuleRepository) {
        this.namedTechLevelRepository = Preconditions.checkNotNull(namedTechLevelRepository, "namedTechLevelRepository must not be empty");
        this.armorRepository = Preconditions.checkNotNull(armorRepository, "armorRepository must not be empty");
        this.weaponRepository = Preconditions.checkNotNull(weaponRepository, "weaponRepository must not be empty");
        this.launcherRepository = Preconditions.checkNotNull(launcherRepository, "launcherRepository must not be empty");
        this.missileRepository = Preconditions.checkNotNull(missileRepository, "missileRepository must not be empty");
        this.sidewallRepository = Preconditions.checkNotNull(sidewallRepository, "sidewallRepository must not be empty");
        this.propulsionRepository = Preconditions.checkNotNull(propulsionRepository, "propulsionRepository must not be empty");
        this.electronicWarfareRepository = Preconditions.checkNotNull(electronicWarfareRepository, "electronicWarfareRepository must not be empty");
        this.passiveModuleRepository = Preconditions.checkNotNull(passiveModuleRepository, "passiveModuleRepository must not be empty");
    }

    @Nonnull
    public NamedTechLevel createBaseModule(@Nonnull final String name,
                                           @Nonnull final String description,
                                           @Nonnull final Research unlockedThrough,
                                           @Nonnull final ETechLevel techLevel,
                                           @Nonnull final Class<?> clazz) {
        Preconditions.checkNotNull(name, "name must not be empty");
        Preconditions.checkNotNull(description, "description must not be empty");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough must not be empty");
        Preconditions.checkNotNull(techLevel, "techLevel must not be empty");
        Preconditions.checkNotNull(clazz, "clazz must not be empty");

        return namedTechLevelRepository.save(new NamedTechLevel(name, description, unlockedThrough, techLevel, clazz));
    }

    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public Weapon createWeapon(@Nonnull final NamedTechLevel namedTechLevel,
                               final int unlockedThroughLevel,
                               final int tonnage,
                               final int effectValue,
                               @Nonnull final EShipClassType shipClassType,
                               @Nonnull final Distance damageProjectionRange,
                               final int amountDamageEmitter,
                               @Nonnull final EWeaponType weaponType,
                               @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(namedTechLevel, "namedTechLevel must not be empty");
        Preconditions.checkNotNull(shipClassType, "shipClassType must not be empty");
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final String technicalTypeName = "D-" + shipClassType.name() + namedTechLevel.getTechLevel().name().split("_")[1] + weaponType.name().charAt(0);
        return weaponRepository.save(new Weapon(namedTechLevel, technicalTypeName, unlockedThroughLevel, tonnage, effectValue, shipClassType, damageProjectionRange, amountDamageEmitter, weaponType, crewRequirement));
    }

    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public Launcher createLauncher(@Nonnull final NamedTechLevel namedTechLevel,
                                   final int unlockedThroughLevel,
                                   final int tonnage,
                                   @Nonnull final EShipClassType shipClassType,
                                   @Nonnull final CrewRequirement crewRequirement,
                                   @Nonnull final EWeaponType weaponType,
                                   @Nonnull final Set<Missile> allowedMissiles) {
        Preconditions.checkNotNull(namedTechLevel, "namedTechLevel must not be empty");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(allowedMissiles, "allowedMissiles shouldn't be null!");

        final String technicalTypeName = "L-" + shipClassType.name() + namedTechLevel.getTechLevel().name().split("_")[1] + weaponType.name().charAt(0);
        return launcherRepository.save(new Launcher(namedTechLevel, technicalTypeName, unlockedThroughLevel, tonnage, shipClassType, crewRequirement, weaponType, allowedMissiles));
    }

    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public Missile createMissile(@Nonnull final NamedTechLevel namedTechLevel,
                                 final int unlockedThroughLevel,
                                 final int elokaResistance,
                                 final int tonnage,
                                 @Nonnull final EShipClassType shipClassType,
                                 @Nonnull Warhead warhead,
                                 @Nonnull MissileMotor missileMotor) {
        Preconditions.checkNotNull(namedTechLevel, "namedTechLevel must not be empty");
        Preconditions.checkNotNull(warhead, "warhead shouldn't be null!");
        Preconditions.checkNotNull(missileMotor, "missileMotor shouldn't be null!");

        final String technicalTypeName = "M-" + shipClassType.name() + missileMotor.getEndurance() + "-" + missileMotor.getManeuverability() + "-"
                + namedTechLevel.getTechLevel().name().split("_")[1] + "-" + warhead.getWarheadType().name().charAt(0);
        return missileRepository.save(new Missile(namedTechLevel, technicalTypeName, unlockedThroughLevel, elokaResistance, tonnage, shipClassType, warhead, missileMotor));
    }

    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public Propulsion createPropulsion(@Nonnull final NamedTechLevel namedTechLevel,
                                       final int unlockedThroughLevel,
                                       final int effectValue,
                                       final int costsPercentage,
                                       @Nonnull final EHyperBand hyperBand,
                                       @Nonnull final ETechnologyType technologyType) {
        Preconditions.checkNotNull(namedTechLevel, "genericBaseModule shouldn't be null!");
        Preconditions.checkNotNull(hyperBand, "hyperBand shouldn't be null!");
        Preconditions.checkNotNull(technologyType, "technologyType must not be empty");

        final String technicalTypeName = "P-" + hyperBand.name().charAt(0) + namedTechLevel.getTechLevel().name().split("_")[1] + "-" + technologyType.name().charAt(0);
        return propulsionRepository.save(new Propulsion(namedTechLevel, technicalTypeName, unlockedThroughLevel, effectValue, costsPercentage, hyperBand, technologyType));
    }


    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public Armor createArmor(@Nonnull final NamedTechLevel namedTechLevel,
                             final int unlockedThroughLevel,
                             final int effectValue,
                             final int tonnage,
                             @Nonnull final EShipClassType shipClassType,
                             @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(namedTechLevel, "namedTechLevel must not be empty");

        final String technicalTypeName = "A-" + namedTechLevel.getTechLevel().name().split("_")[1] + "-" + shipClassType.name();
        return armorRepository.save(new Armor(namedTechLevel, technicalTypeName, unlockedThroughLevel, effectValue, tonnage, shipClassType, crewRequirement));
    }

    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public ElectronicWarfare createElectronicWarfare(@Nonnull final NamedTechLevel namedTechLevel,
                                                     final int unlockedThroughLevel,
                                                     final int effectValue,
                                                     final int tonnage,
                                                     @Nonnull final EShipClassType shipClassType,
                                                     @Nonnull final CrewRequirement crewRequirement,
                                                     @Nonnull final Distance effectiveRange) {
        Preconditions.checkNotNull(namedTechLevel, "namedTechLevel must not be empty");
        Preconditions.checkNotNull(shipClassType, "shipClassType must not be empty");
        Preconditions.checkNotNull(effectiveRange, "effectiveRange shouldn't be null!");

        final String technicalTypeName = "E-" + namedTechLevel.getTechLevel().name().split("_")[1] + "-" + shipClassType.name();
        return electronicWarfareRepository.save(new ElectronicWarfare(namedTechLevel, technicalTypeName, unlockedThroughLevel, effectValue, tonnage, shipClassType, crewRequirement, effectiveRange));
    }

    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public Sidewall createSidewall(@Nonnull final NamedTechLevel namedTechLevel,
                                   final int unlockedThroughLevel,
                                   final int effectValue,
                                   final int tonnage,
                                   @Nonnull final EShipClassType shipClassType,
                                   @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(namedTechLevel, "namedTechLevel must not be empty");
        Preconditions.checkNotNull(shipClassType, "shipClassType must not be empty");

        final String technicalTypeName = "S-" + namedTechLevel.getTechLevel().name().split("_")[1] + "-" + shipClassType.name();
        return sidewallRepository.save(new Sidewall(namedTechLevel, technicalTypeName, unlockedThroughLevel, effectValue, tonnage, shipClassType, crewRequirement));
    }

    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public PassiveModule createPassiveModule(@Nonnull final String name,
                                             @Nonnull final String description,
                                             @Nonnull final Research unlockedThrough,
                                             @Nonnull final ESupportType supportType,
                                             @Nonnull final ECalculationType calculationType,
                                             final int tonnage,
                                             final int value,
                                             @Nonnull final EShipClassType shipClassType,
                                             @Nonnull final ETechLevel techLevel,
                                             @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        return passiveModuleRepository.save(new PassiveModule(name, description, unlockedThrough, tonnage, value, shipClassType, techLevel, supportType, calculationType, crewRequirement));
    }

    @Nonnull
    public List<Armor> findAllArmorByUser(final int idUser) {
        return armorRepository.findAllByUser(idUser);
    }

    @Nonnull
    public List<Propulsion> findAllPropulsionByUser(final int idUser) {
        return propulsionRepository.findAllByUser(idUser);
    }

    @Nonnull
    public List<ElectronicWarfare> findAllElectronicWarfareByUser(final int idUser) {
        return electronicWarfareRepository.findAllByUser(idUser);
    }

    @Nonnull
    public List<Sidewall> findAllSidewallByUser(final int idUser) {
        return sidewallRepository.findAllByUser(idUser);
    }

    @Nonnull
    public List<Weapon> findAllWeaponByUser(final int idUser) {
        return weaponRepository.findAllByUser(idUser);
    }

    @Nonnull
    public List<Launcher> findAllLauncherByUser(final int idUser) {
        return launcherRepository.findAllByUser(idUser);
    }

    @Nonnull
    public List<PassiveModule> findAllPassiveModuleByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return passiveModuleRepository.findAllByUser(user);
    }

    @Nullable
    public Armor findArmorById(final int idModule) {
        return armorRepository.findById(idModule).orElse(null);
    }

    @Nonnull
    public List<Armor> findArmorsById(@Nonnull final List<Integer> moduleIDs) {
        Preconditions.checkNotNull(moduleIDs, "moduleIDs shouldn't be null!");

        return ImmutableList.copyOf(armorRepository.findAllById(moduleIDs));
    }

    @Nonnull
    public List<Armor> findAllArmors() {
        return ImmutableList.copyOf(armorRepository.findAll());
    }

    @Nullable
    public Propulsion findPropulsionById(final int idModule) {
        return propulsionRepository.findById(idModule).orElse(null);
    }

    @Nonnull
    public List<Propulsion> findPropulsionsById(@Nonnull final List<Integer> moduleIDs) {
        Preconditions.checkNotNull(moduleIDs, "moduleIDs shouldn't be null!");

        return ImmutableList.copyOf(propulsionRepository.findAllById(moduleIDs));
    }

    @Nonnull
    public List<Propulsion> findAllPropulsions() {
        return ImmutableList.copyOf(propulsionRepository.findAll());
    }

    @Nullable
    public ElectronicWarfare findElectronicWarfareById(final int idModule) {
        return electronicWarfareRepository.findById(idModule).orElse(null);
    }

    @Nonnull
    public List<ElectronicWarfare> findElectronicWarfareById(@Nonnull final List<Integer> moduleIDs) {
        Preconditions.checkNotNull(moduleIDs, "moduleIDs shouldn't be null!");

        return ImmutableList.copyOf(electronicWarfareRepository.findAllById(moduleIDs));
    }

    @Nonnull
    public List<ElectronicWarfare> findAllElectronicWarfare() {
        return ImmutableList.copyOf(electronicWarfareRepository.findAll());
    }

    @Nullable
    public Sidewall findSidewallById(final int idModule) {
        return sidewallRepository.findById(idModule).orElse(null);
    }

    @Nonnull
    public List<Sidewall> findSidewallsById(@Nonnull final List<Integer> moduleIDs) {
        Preconditions.checkNotNull(moduleIDs, "moduleIDs shouldn't be null!");

        return ImmutableList.copyOf(sidewallRepository.findAllById(moduleIDs));
    }

    @Nonnull
    public List<Sidewall> findAllSidewalls() {
        return ImmutableList.copyOf(sidewallRepository.findAll());
    }

    @Nullable
    public Weapon findWeaponById(final int idModule) {
        return weaponRepository.findById(idModule).orElse(null);
    }

    @Nonnull
    public List<Weapon> findWeaponsById(@Nonnull final List<Integer> moduleIDs) {
        Preconditions.checkNotNull(moduleIDs, "moduleIDs shouldn't be null!");

        return ImmutableList.copyOf(weaponRepository.findAllById(moduleIDs));
    }

    @Nonnull
    public List<Weapon> findAllWeapons() {
        return ImmutableList.copyOf(weaponRepository.findAll());
    }

    @Nullable
    public PassiveModule findPassiveModuleById(final int idModule) {
        return passiveModuleRepository.findById(idModule).orElse(null);
    }

    @Nonnull
    public List<PassiveModule> findPassiveModulesById(@Nonnull final List<Integer> moduleIDs) {
        Preconditions.checkNotNull(moduleIDs, "moduleIDs shouldn't be null!");

        return ImmutableList.copyOf(passiveModuleRepository.findAllById(moduleIDs));
    }

    @Nonnull
    public List<PassiveModule> findAllPassiveModules() {
        return ImmutableList.copyOf(passiveModuleRepository.findAll());
    }

    @Nullable
    public Missile findMissileById(final int idMissile) {
        return missileRepository.findById(idMissile).orElse(null);
    }

    @Nonnull
    public List<Missile> findMissilesById(@Nonnull final List<Integer> missileIDs) {
        Preconditions.checkNotNull(missileIDs, "missileIDs shouldn't be null!");

        return ImmutableList.copyOf(missileRepository.findAllById(missileIDs));
    }

    @Nonnull
    public List<Missile> findAllMissiles() {
        return ImmutableList.copyOf(missileRepository.findAll());
    }

    @Nullable
    public Launcher findLauncherById(final int idLauncher) {
        return launcherRepository.findById(idLauncher).orElse(null);
    }

    @Nonnull
    public List<Launcher> findLaunchersById(@Nonnull final List<Integer> launcherIDs) {
        Preconditions.checkNotNull(launcherIDs, "launcherIDs shouldn't be null!");

        return ImmutableList.copyOf(launcherRepository.findAllById(launcherIDs));
    }

    @Nonnull
    public List<Launcher> findAllLaunchers() {
        return ImmutableList.copyOf(launcherRepository.findAll());
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void saveAll(@Nonnull final Collection<Armor> list) {
        Preconditions.checkNotNull(list, "list must not be empty");

        armorRepository.saveAll(list);
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void save(@Nonnull final Armor armor) {
        Preconditions.checkNotNull(armor, "armor must not be empty");

        armorRepository.save(armor);
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void save(@Nonnull final Propulsion propulsion) {
        Preconditions.checkNotNull(propulsion, "propulsion must not be empty");

        propulsionRepository.save(propulsion);
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void delete(@Nonnull final Propulsion prop) {
        Preconditions.checkNotNull(prop, "prop must not be empty");

        propulsionRepository.delete(prop);
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void save(@Nonnull final ElectronicWarfare eloka) {
        Preconditions.checkNotNull(eloka, "eloka must not be empty");

        electronicWarfareRepository.save(eloka);
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void save(@Nonnull final Sidewall sidewall) {
        Preconditions.checkNotNull(sidewall, "sidewall must not be empty");

        sidewallRepository.save(sidewall);
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void save(@Nonnull final Launcher launcher) {
        Preconditions.checkNotNull(launcher, "launcher must not be empty");

        launcherRepository.save(launcher);
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void save(@Nonnull final Missile missile) {
        Preconditions.checkNotNull(missile, "missile must not be empty");

        missileRepository.save(missile);
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void save(@Nonnull final Weapon weapon) {
        Preconditions.checkNotNull(weapon, "weapon must not be empty");

        weaponRepository.save(weapon);
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public PassiveModule save(@Nonnull final PassiveModule passiveModule) {
        Preconditions.checkNotNull(passiveModule, "passiveModule must not be empty");

        return passiveModuleRepository.save(passiveModule);
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void save(final NamedTechLevel baseModule) {
        Preconditions.checkNotNull(baseModule, "baseModule must not be empty");

        namedTechLevelRepository.save(baseModule);
    }

    @Nonnull
    public Fitting getFitting() {
        final List<Propulsion> propulsions = propulsionRepository.findAll();
        final List<Armor> armors = armorRepository.findAll();
        final List<ElectronicWarfare> electronicWarfares = electronicWarfareRepository.findAll();
        final List<Sidewall> sidewalls = sidewallRepository.findAll();
        final List<Weapon> weapons = weaponRepository.findAll();
        final List<Launcher> launchers = launcherRepository.findAll();
        final List<PassiveModule> passiveModules = passiveModuleRepository.findAll();
        return new Fitting(propulsions, armors, electronicWarfares, sidewalls, weapons, launchers, passiveModules);
    }
}
