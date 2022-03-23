package de.yuga.spacebattle.backend.services.spacecraft;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.repositories.spacecraft.MissileMotorRepository;
import de.yuga.spacebattle.backend.repositories.spacecraft.MissileRepository;
import de.yuga.spacebattle.backend.repositories.spacecraft.WarheadRepository;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.*;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@SuppressWarnings("DeprecatedIsStillUsed")
@Service
public class ModuleService {

    @Nonnull
    private final ArmorRepository armorRepository;

    @Nonnull
    private final WeaponRepository weaponRepository;

    @Nonnull
    private final LauncherRepository launcherRepository;

    @Nonnull
    private final WarheadRepository warheadRepository;

    @Nonnull
    private final MissileMotorRepository missileMotorRepository;

    @Nonnull
    private final MissileRepository missileRepository;

    @Nonnull
    private final SidewallRepository sidewallRepository;

    @Nonnull
    private final PropulsionRepository propulsionRepository;

    @Nonnull
    private final ElectronicWarfareRepository electronicWarfareRepository;

    @Nonnull
    private final AmmunitionRepository ammunitionRepository;

    @Nonnull
    private final PassiveModuleRepository passiveModuleRepository;

    public ModuleService(@Nonnull final ArmorRepository armorRepository,
                         @Nonnull final WeaponRepository weaponRepository,
                         @Nonnull final LauncherRepository launcherRepository,
                         @Nonnull final WarheadRepository warheadRepository,
                         @Nonnull final MissileMotorRepository missileMotorRepository,
                         @Nonnull final MissileRepository missileRepository,
                         @Nonnull final SidewallRepository sidewallRepository,
                         @Nonnull final PropulsionRepository propulsionRepository,
                         @Nonnull final ElectronicWarfareRepository electronicWarfareRepository,
                         @Nonnull final AmmunitionRepository ammunitionRepository,
                         @Nonnull final PassiveModuleRepository passiveModuleRepository) {
        Preconditions.checkNotNull(armorRepository, "armorRepository shouldn't be null!");
        Preconditions.checkNotNull(weaponRepository, "weaponRepository shouldn't be null!");
        Preconditions.checkNotNull(launcherRepository, "launcherRepository shouldn't be null!");
        Preconditions.checkNotNull(warheadRepository, "warheadRepository shouldn't be null!");
        Preconditions.checkNotNull(missileMotorRepository, "missileMotorRepository shouldn't be null!");
        Preconditions.checkNotNull(missileRepository, "missileRepository shouldn't be null!");
        Preconditions.checkNotNull(sidewallRepository, "sidewallRepository shouldn't be null!");
        Preconditions.checkNotNull(propulsionRepository, "propulsionRepository shouldn't be null!");
        Preconditions.checkNotNull(electronicWarfareRepository, "electronicWarfareRepository shouldn't be null!");
        Preconditions.checkNotNull(ammunitionRepository, "ammunitionRepository shouldn't be null!");
        Preconditions.checkNotNull(passiveModuleRepository, "passiveModuleRepository shouldn't be null!");

        this.armorRepository = armorRepository;
        this.weaponRepository = weaponRepository;
        this.launcherRepository = launcherRepository;
        this.warheadRepository = warheadRepository;
        this.missileMotorRepository = missileMotorRepository;
        this.missileRepository = missileRepository;
        this.sidewallRepository = sidewallRepository;
        this.propulsionRepository = propulsionRepository;
        this.electronicWarfareRepository = electronicWarfareRepository;
        this.ammunitionRepository = ammunitionRepository;
        this.passiveModuleRepository = passiveModuleRepository;
    }


    /**
     * Creates a new {@link Armor}.
     *
     * @param name            the name of the research
     * @param description     the description
     * @param useCapacity     the amount of construction capacity used
     * @param value           the base effect value, e.g. damage
     * @param techLevel       the techLevel of this module
     * @param unlockedThrough the research to unlock this module
     * @return the new module
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public Armor createArmor(@Nonnull final String name,
                             @Nonnull final String description,
                             @Nonnull final Research unlockedThrough,
                             final int useCapacity,
                             final int value,
                             final int techLevel,
                             @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        return armorRepository.save(new Armor(name, description, unlockedThrough, useCapacity, value, techLevel, crewRequirement));
    }

    /**
     * Creates a new {@link ElectronicWarfare}.
     *
     * @param name            the name of the research
     * @param description     the description
     * @param useCapacity     the amount of construction capacity used
     * @param value           the base effect value, e.g. damage
     * @param techLevel       the techLevel of this module
     * @param unlockedThrough the research to unlock this module
     * @return the new module
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public ElectronicWarfare createElectronicWarfare(@Nonnull final String name,
                                                     @Nonnull final String description,
                                                     @Nonnull final Research unlockedThrough,
                                                     final int useCapacity,
                                                     final int value,
                                                     @Nonnull final Distance effectiveRange,
                                                     final int techLevel,
                                                     @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(effectiveRange, "effectiveRange shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        return electronicWarfareRepository.save(new ElectronicWarfare(name, description, unlockedThrough, useCapacity, value, effectiveRange, techLevel, crewRequirement));
    }

    /**
     * Creates a new {@link Sidewall}.
     *
     * @param name            the name of the research
     * @param description     the description
     * @param useCapacity     the amount of construction capacity used
     * @param value           the base effect value, e.g. damage
     * @param techLevel       the techLevel of this module
     * @param unlockedThrough the research to unlock this module
     * @return the new module
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public Sidewall createSidewall(@Nonnull final String name,
                                   @Nonnull final String description,
                                   @Nonnull final Research unlockedThrough,
                                   final int useCapacity,
                                   final int value,
                                   final int techLevel,
                                   @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        return sidewallRepository.save(new Sidewall(name, description, unlockedThrough, useCapacity, value, techLevel, crewRequirement));
    }

    /**
     * Creates a new {@link Weapon}.
     *
     * @param name            the name of the research
     * @param description     the description
     * @param useCapacity     the amount of construction capacity used
     * @param value           the base effect value, e.g. damage
     * @param techLevel       the techLevel of this module
     * @param unlockedThrough the research to unlock this module
     * @return the new module
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public Weapon createWeapon(@Nonnull final String name,
                               @Nonnull final String description,
                               @Nonnull final Research unlockedThrough,
                               final int useCapacity,
                               final int value,
                               final int techLevel,
                               @Nonnull final Distance damageProjectionRange,
                               final int amountDamageEmitter,
                               @Nonnull final EWeaponType weaponType,
                               @Nonnull final EAlignmentType alignmentType,
                               @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(alignmentType, "alignmentType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        return weaponRepository.save(new Weapon(name, description, unlockedThrough, useCapacity, value, techLevel, damageProjectionRange, amountDamageEmitter, weaponType, alignmentType, crewRequirement));
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    public Launcher createLauncher(@Nonnull final String name,
                                   @Nonnull final String description,
                                   @Nonnull final Research unlockedThrough,
                                   @Nonnull final AmmunitionModule ammunitionModule,
                                   final int useCapacity,
                                   final int techLevel,
                                   @Nonnull final EAlignmentType alignmentType,
                                   @Nonnull final CrewRequirement crewRequirement,
                                   @Nonnull final EWeaponType weaponType,
                                   @Nonnull final Set<Missile> allowedMissiles) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");
        Preconditions.checkNotNull(alignmentType, "alignmentType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(allowedMissiles, "allowedMissiles shouldn't be null!");

        return launcherRepository.save(new Launcher(name, description, unlockedThrough, ammunitionModule, useCapacity, techLevel, alignmentType, crewRequirement, weaponType, allowedMissiles));
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    public MissileMotor createMissileMotor(@Nonnull final String typeName,
                                           final int endurance,
                                           @Nonnull final Acceleration acceleration,
                                           final int maneuverability,
                                           final int useCapacity) {
        Preconditions.checkNotNull(typeName, "typeName shouldn't be null!");
        Preconditions.checkNotNull(acceleration, "acceleration shouldn't be null!");

        return missileMotorRepository.save(new MissileMotor(typeName, endurance, acceleration, maneuverability, useCapacity));
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    public Warhead createWarhead(@Nonnull final String typeName,
                                 final int effectValue,
                                 @Nonnull final Distance damageProjectionRange,
                                 @Nonnull final EWarheadType warheadType,
                                 final int useCapacity) {
        Preconditions.checkNotNull(typeName, "typeName shouldn't be null!");
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(warheadType, "warheadType shouldn't be null!");

        return warheadRepository.save(new Warhead(typeName, effectValue, damageProjectionRange, warheadType, useCapacity));
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    public Missile createMissile(@Nonnull String typeName,
                                 final int warheadCapacity,
                                 final int motorCapacity,
                                 final int elokaResistance,
                                 @Nonnull Warhead warhead,
                                 @Nonnull List<MissileMotor> missileMotors,
                                 @Nonnull Research unlockedThrough,
                                 @Nonnull final AmmunitionModule ammunitionModule) {
        Preconditions.checkNotNull(typeName, "typeName shouldn't be null!");
        Preconditions.checkNotNull(warhead, "warhead shouldn't be null!");
        Preconditions.checkNotNull(missileMotors, "missileMotors shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");

        return missileRepository.save(new Missile(typeName, warheadCapacity, motorCapacity, elokaResistance, warhead, missileMotors, unlockedThrough, ammunitionModule));
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    public Propulsion createPropulsion(@Nonnull final String name,
                                       @Nonnull final String description,
                                       @Nonnull final Research unlockedThrough,
                                       final int useCapacity,
                                       final int value,
                                       final int level,
                                       @Nonnull final EHyperBand hyperBand,
                                       @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(hyperBand, "hyperBand shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        return propulsionRepository.save(new Propulsion(name, description, unlockedThrough, useCapacity, value, level, hyperBand, crewRequirement));
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    public PassiveModule createPassiveModule(@Nonnull final String name,
                                             @Nonnull final String description,
                                             @Nonnull final Research unlockedThrough,
                                             @Nonnull final ESupportType supportType,
                                             @Nonnull final ECalculationType calculationType,
                                             final int useCapacity,
                                             final int value,
                                             final int techLevel,
                                             @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        return passiveModuleRepository.save(new PassiveModule(name, description, unlockedThrough, useCapacity, value, techLevel, supportType, calculationType, crewRequirement));
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    public AmmunitionModule createAmmunitionModule(@Nonnull final String name,
                                                   @Nonnull final String description,
                                                   @Nonnull final Research unlockedThrough,
                                                   final int useCapacity,
                                                   final int value,
                                                   final int techLevel,
                                                   @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        return ammunitionRepository.save(new AmmunitionModule(name, description, unlockedThrough, useCapacity, value, techLevel, crewRequirement));
    }

    @Nonnull
    public List<Armor> findAllArmorByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return armorRepository.findAllByUser(user);
    }

    @Nonnull
    public List<Propulsion> findAllPropulsionByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return propulsionRepository.findAllByUser(user);
    }

    @Nonnull
    public List<ElectronicWarfare> findAllElectronicWarfareByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return electronicWarfareRepository.findAllByUser(user);
    }

    @Nonnull
    public List<Sidewall> findAllSidewallByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return sidewallRepository.findAllByUser(user);
    }

    @Nonnull
    public List<Weapon> findAllWeaponByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return weaponRepository.findAllByUser(user);
    }

    @Nonnull
    public List<Launcher> findAllLauncherByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return launcherRepository.findAllByUser(user);
    }

    @Nonnull
    public List<AmmunitionModule> findAllAmmunitionModulesByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return ammunitionRepository.findAllByUser(user);
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

    @Nullable
    public Propulsion findPropulsionById(final int idModule) {
        return propulsionRepository.findById(idModule).orElse(null);
    }

    @Nonnull
    public List<Propulsion> findPropulsionsById(@Nonnull final List<Integer> moduleIDs) {
        Preconditions.checkNotNull(moduleIDs, "moduleIDs shouldn't be null!");

        return ImmutableList.copyOf(propulsionRepository.findAllById(moduleIDs));
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

    @Nullable
    public Sidewall findSidewallById(final int idModule) {
        return sidewallRepository.findById(idModule).orElse(null);
    }

    @Nonnull
    public List<Sidewall> findSidewallsById(@Nonnull final List<Integer> moduleIDs) {
        Preconditions.checkNotNull(moduleIDs, "moduleIDs shouldn't be null!");

        return ImmutableList.copyOf(sidewallRepository.findAllById(moduleIDs));
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

    @Nullable
    public AmmunitionModule findAmmunitionModuleById(final int idModule) {
        return ammunitionRepository.findById(idModule).orElse(null);
    }

    @Nonnull
    public List<AmmunitionModule> findAmmunitionModulesById(@Nonnull final List<Integer> moduleIDs) {
        Preconditions.checkNotNull(moduleIDs, "moduleIDs shouldn't be null!");

        return ImmutableList.copyOf(ammunitionRepository.findAllById(moduleIDs));
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

    @Nullable
    public MissileMotor findMissileMotorById(final int idMissileMotor) {
        return missileMotorRepository.findById(idMissileMotor).orElse(null);
    }

    @Nonnull
    public List<MissileMotor> findMissileMotorsById(@Nonnull final List<Integer> motorIDs) {
        Preconditions.checkNotNull(motorIDs, "motorIDs shouldn't be null!");

        return ImmutableList.copyOf(missileMotorRepository.findAllById(motorIDs));
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

    @Nullable
    public Warhead findWarheadById(final int idWarhead) {
        return warheadRepository.findById(idWarhead).orElse(null);
    }

    @Nonnull
    public List<Warhead> findWarheadsById(@Nonnull final List<Integer> warheadIDs) {
        Preconditions.checkNotNull(warheadIDs, "warheadIDs shouldn't be null!");

        return ImmutableList.copyOf(warheadRepository.findAllById(warheadIDs));
    }

    @Nullable
    public Launcher findLauncherById(final int idWarhead) {
        return launcherRepository.findById(idWarhead).orElse(null);
    }

    @Nonnull
    public List<Launcher> findLaunchersById(@Nonnull final List<Integer> launcherIDs) {
        Preconditions.checkNotNull(launcherIDs, "launcherIDs shouldn't be null!");

        return ImmutableList.copyOf(launcherRepository.findAllById(launcherIDs));
    }
}
