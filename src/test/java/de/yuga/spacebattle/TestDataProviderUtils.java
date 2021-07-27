package de.yuga.spacebattle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.enums.*;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.TestUtils.setId;

public class TestDataProviderUtils {

    @Nonnull
    public static Cage cage() {
        final Planet pl = planet(2, 2);
        final FleetOrbit fo = new FleetOrbit(pl.getOrbit(), pl.getSystem());
        final Map.Entry<FleetOrbit, List<Fleet>> e = Map.entry(fo, List.of(minimalFleet(), minimalFleet()));
        final FleetClash fleetClash = new FleetClash(e);
        return new Cage(fleetClash);
    }

    @Nonnull
    public static WarShip warShip(final Fleet fleet, final ShipClass shipClass, final Planet planet) {
        final WarShip warShip = new WarShip("", planet, fleet, shipClass);
        setId(warShip);
        return warShip;
    }

    @Nonnull
    public static User user() {
        final User user = new User();
        setId(user);
        return user;
    }

    @Nonnull
    public static Research research() {
        final Research user = new Research();
        setId(user);
        return user;
    }

    @Nonnull
    public static Orbit orbit(final int x, final int y) {
        return new Orbit(x, y);
    }

    @Nonnull
    public static StarSystem system(final int x, final int y) {
        final StarSystem sys = new StarSystem("anotherRandom", new Orbit(x, y));
        setId(sys);
        return sys;
    }

    @Nonnull
    public static Planet planet(final int x, final int y) {
        final Orbit random = orbit(0, 0);
        final StarSystem sys = system(x, y);
        setId(sys);
        final Planet planet = new Planet(null, "name1", sys, random);
        setId(planet);
        return planet;
    }

    @Nonnull
    public static Fleet minimalFleet() {
        final Planet planet = planet(1, 2);
        return fleet(10, planet);
    }

    @Nonnull
    public static Missile missile(final int damageValue) {
        AmmunitionModule shipKillerAmmunition = createAmmunitionModule("Rocket Ammunition", "A bunch of rockets.", 5, 10, 1, new CrewRequirement(militaryCrew(), EDepositType.COSTS));
        MissileMotor shipKillerMotor = createMissileMotor("Ship Killer Motor Mk I", 180, NavigationCalculator.getMeterPerSecondSquaredFromG(46000), 20, 100);
        Warhead nuclearShipKillerWarHead = createWarhead("Nuclear ship killer war head", damageValue, BigDecimal.valueOf(50000), EWarheadType.EXPLOSION, 100);
        Missile shipKillerMissile = createMissile("Nuclear ship killer missile Mk I", 100, 100, 10, nuclearShipKillerWarHead, List.of(shipKillerMotor), shipKillerAmmunition);
        ReflectionTestUtils.setField(shipKillerAmmunition, "missile", shipKillerMissile);
        return shipKillerMissile;
    }

    @Nonnull
    public static Fleet fleet(final int effectFTLValue, final Planet planetStart) {
        final User user = user();
        final Fleet fleet = new Fleet();
        fleet.setOwner(user);
        setId(fleet);
        final ShipClass shipClass = shipClass(effectFTLValue);
        final Set<WarShip> warShips = new HashSet<>();
        final WarShip warShip = warShip(fleet, shipClass, planetStart);
        warShips.add(warShip);
        fleet.updateShips(warShips);
        fleet.setOrbit(new FleetOrbit(planetStart.getOrbit(), planetStart.getSystem()));
        return fleet;
    }

    @Nonnull
    public static ShipClass shipClass(final int effectFTLValue) {
        final User user = user();

        Map<EEducationType, Long> militaryCrew = militaryCrew();

        Armor armor = createArmor("Armor Mk I", "An armor", 5, 3000, 1, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        //Propulsion propulsion = createPropulsion("Speed Mk I", "A drive", 5, 500, 1, false, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Propulsion propulsionFTL = createPropulsion("FTL Speed Mk I", "A FTL drive", 10, effectFTLValue, 1, true, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        ElectronicWarfare electronicWarfare = createElectronicWarfare("Scanner Mk I", "A scanner", 5, 1000, 1000000, 1, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Sidewall sidewall = createSidewall("Shield Mk I", "A shield", 5, 15000, 1, new CrewRequirement(militaryCrew, EDepositType.COSTS));

        AmmunitionModule shipKillerAmmunition = createAmmunitionModule("Rocket Ammunition", "A bunch of rockets.", 5, 10, 1, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        MissileMotor shipKillerMotor = createMissileMotor("Ship Killer Motor Mk I", 180, NavigationCalculator.getMeterPerSecondSquaredFromG(46000), 20, 100);
        Warhead nuclearShipKillerWarHead = createWarhead("Nuclear ship killer war head", 1000, BigDecimal.valueOf(50000), EWarheadType.EXPLOSION, 100);
        Missile shipKillerMissile = createMissile("Nuclear ship killer missile Mk I", 100, 100, 10, nuclearShipKillerWarHead, List.of(shipKillerMotor), shipKillerAmmunition);
        ReflectionTestUtils.setField(shipKillerAmmunition, "missile", shipKillerMissile);
        Launcher shipKillerLauncher = createLauncher("Ship killer launcher Mk I", "The launcher for ship killers", shipKillerAmmunition, 100, 1, EAlignmentType.CHASE_ALIGNMENT, new CrewRequirement(militaryCrew, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(shipKillerMissile));

        AmmunitionModule counterRocketAmmunition = createAmmunitionModule("Counter Rocket Ammunition", "Another bunch of rockets.", 5, 10, 1, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        MissileMotor counterMissileMotor = createMissileMotor("Counter Motor Mk I", 5, NavigationCalculator.getMeterPerSecondSquaredFromG(96000), 80, 10);
        Warhead counterWarHead = createWarhead("Counter war head", 1, BigDecimal.ZERO, EWarheadType.COUNTER_MISSILE, 10);
        Missile counterMissile = createMissile("Counter missile Mk I", 10, 10, 10, counterWarHead, List.of(counterMissileMotor), counterRocketAmmunition);
        ReflectionTestUtils.setField(counterRocketAmmunition, "missile", counterMissile);
        Launcher counterMissileLauncher = createLauncher("Counter missile launcher Mk I", "The launcher for counter missiles", counterRocketAmmunition, 100, 1, EAlignmentType.CHASE_ALIGNMENT, new CrewRequirement(militaryCrew, EDepositType.COSTS), EWeaponType.COUNTER_MISSILE, Set.of(counterMissile));

        Weapon laserWeapon = createWeapon("Laser Mk I", "A laser", 5, 10, 1, BigDecimal.valueOf(400000), 1, EWeaponType.BEAM, EAlignmentType.CHASE_ALIGNMENT, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Weapon pointDefense = createWeapon("Point Defense Mk I", "A point defense", 5, 1, 1, BigDecimal.valueOf(50000), 1, EWeaponType.POINT_DEFENSE, EAlignmentType.BATTLE_ALIGNMENT, new CrewRequirement(militaryCrew, EDepositType.COSTS));

        PassiveModule passiveModule = createPassiveModule("Improves armor", "Increases the amount of armor", ESupportType.ARMOR, ECalculationType.ADD, 5, 10, 1, new CrewRequirement(militaryCrew, EDepositType.COSTS));

        Hull hull3 = createHull("Cruiser vessel", 80000, 150, 45, 45, 75, "The cruiser hull", EHullType.CC, new CrewRequirement(militaryCrew, EDepositType.COSTS));

        ShipClass as3 = new ShipClass(user, "Argonauts cruiser", hull3, null);
        return createFitting(armor, propulsionFTL, electronicWarfare, sidewall, laserWeapon, pointDefense, new Launcher[]{shipKillerLauncher, counterMissileLauncher}, new PassiveModule[]{passiveModule}, new AmmunitionModule[]{shipKillerAmmunition, counterRocketAmmunition}, as3);
    }

    @Nonnull
    public static Map<EEducationType, Long> militaryCrew() {
        Map<EEducationType, Long> militaryCrew = new HashMap<>();
        militaryCrew.put(EEducationType.MILITARY_MK_I, 20L);
        militaryCrew.put(EEducationType.MILITARY_MK_II, 10L);
        return militaryCrew;
    }

    @Nonnull
    public static Hull createHull(@Nonnull final String name,
                                  final int overallConstructionCapacity,
                                  final int constructionCapacity,
                                  final int constructionCapacityBow,
                                  final int constructionCapacityStern,
                                  final int constructionCapacityBroadsides,
                                  @Nonnull final String description,
                                  @Nonnull final EHullType hullType,
                                  @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(hullType, "hullType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final Hull hull = new Hull(name, overallConstructionCapacity, constructionCapacity, constructionCapacityBow, constructionCapacityStern, constructionCapacityBroadsides, description, research(), hullType, crewRequirement);
        setId(hull);
        return hull;
    }

    private static ShipClass createFitting(Armor armor,
                                           Propulsion propulsionFTL,
                                           ElectronicWarfare electronicWarfare,
                                           Sidewall sidewall,
                                           Weapon laserWeapon,
                                           Weapon pointDefense,
                                           Launcher[] missiles,
                                           PassiveModule[] passiveModules,
                                           AmmunitionModule[] ammunitionModules,
                                           ShipClass shipClass) {
        shipClass.setArmor(armor);
        shipClass.setSidewall(sidewall);
        shipClass.setPropulsion(propulsionFTL);
        shipClass.setElectronicWarfare(electronicWarfare);
        Set<AlignedFitting> fittings = new HashSet<>();
        fittings.add(new AlignedFitting(EWeaponAlignment.BOW, laserWeapon, 1));
        Arrays.stream(missiles).forEach(missile -> fittings.add(new AlignedFitting(EWeaponAlignment.STERN, missile, 1)));
        fittings.add(new AlignedFitting(EWeaponAlignment.BROADSIDE, pointDefense, 1));
        shipClass.setFittings(fittings);

        Set<SupportFitting> supportFittings = Arrays.stream(passiveModules).map(af -> new SupportFitting(af, 1)).collect(Collectors.toSet());
        shipClass.setSupportFittings(supportFittings);

        Set<AmmunitionFitting> ammunitionFittings = Arrays.stream(ammunitionModules).map(af -> new AmmunitionFitting(af, 1)).collect(Collectors.toSet());
        shipClass.setAmmunitionFittings(ammunitionFittings);

        return shipClass;
    }

    @Nonnull
    public static Armor createArmor(@Nonnull final String name,
                                    @Nonnull final String description,
                                    final int useCapacity,
                                    final int value,
                                    final int techLevel,
                                    @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final Armor armor = new Armor(name, description, research(), useCapacity, value, techLevel, crewRequirement);
        setId(armor);
        return armor;
    }

    @Nonnull
    public static ElectronicWarfare createElectronicWarfare(@Nonnull final String name,
                                                            @Nonnull final String description,
                                                            final int useCapacity,
                                                            final int value,
                                                            final int effectiveRange,
                                                            final int techLevel,
                                                            @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final ElectronicWarfare electronicWarfare = new ElectronicWarfare(name, description, research(), useCapacity, value, effectiveRange, techLevel, crewRequirement);
        setId(electronicWarfare);
        return electronicWarfare;
    }

    @Nonnull
    public static Sidewall createSidewall(@Nonnull final String name,
                                          @Nonnull final String description,
                                          final int useCapacity,
                                          final int value,
                                          final int techLevel,
                                          @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final Sidewall sidewall = new Sidewall(name, description, research(), useCapacity, value, techLevel, crewRequirement);
        setId(sidewall);
        return sidewall;
    }

    @Nonnull
    public static Weapon createWeapon(@Nonnull final String name,
                                      @Nonnull final String description,
                                      final int useCapacity,
                                      final int value,
                                      final int techLevel,
                                      @Nonnull final BigDecimal damageProjectionRange,
                                      final int amountDamageEmitter,
                                      @Nonnull final EWeaponType weaponType,
                                      @Nonnull final EAlignmentType alignmentType,
                                      @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(alignmentType, "alignmentType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final Weapon weapon = new Weapon(name, description, research(), useCapacity, value, techLevel, damageProjectionRange, amountDamageEmitter, weaponType, alignmentType, crewRequirement);
        setId(weapon);
        return weapon;
    }

    @Nonnull
    public static Launcher createLauncher(@Nonnull final String name,
                                          @Nonnull final String description,
                                          @Nonnull final AmmunitionModule ammunitionModule,
                                          final int useCapacity,
                                          final int techLevel,
                                          @Nonnull final EAlignmentType alignmentType,
                                          @Nonnull final CrewRequirement crewRequirement,
                                          @Nonnull final EWeaponType weaponType,
                                          @Nonnull final Set<Missile> allowedMissiles) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");
        Preconditions.checkNotNull(alignmentType, "alignmentType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(allowedMissiles, "allowedMissiles shouldn't be null!");

        final Launcher launcher = new Launcher(name, description, research(), ammunitionModule, useCapacity, techLevel, alignmentType, crewRequirement, weaponType, allowedMissiles);
        setId(launcher);
        return launcher;
    }

    @Nonnull
    public static MissileMotor createMissileMotor(@Nonnull final String typeName,
                                                  final int endurance,
                                                  final int acceleration,
                                                  final int maneuverability,
                                                  final int useCapacity) {
        Preconditions.checkNotNull(typeName, "typeName shouldn't be null!");

        final MissileMotor missileMotor = new MissileMotor(typeName, endurance, acceleration, maneuverability, useCapacity);
        setId(missileMotor);
        return missileMotor;
    }

    @Nonnull
    public static Warhead createWarhead(@Nonnull final String typeName,
                                        final int effectValue,
                                        @Nonnull final BigDecimal damageProjectionRange,
                                        @Nonnull final EWarheadType warheadType,
                                        final int useCapacity) {
        Preconditions.checkNotNull(typeName, "typeName shouldn't be null!");
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(warheadType, "warheadType shouldn't be null!");

        final Warhead warhead = new Warhead(typeName, effectValue, damageProjectionRange, warheadType, useCapacity);
        setId(warhead);
        return warhead;
    }

    @Nonnull
    public static Missile createMissile(@Nonnull String typeName,
                                        final int warheadCapacity,
                                        final int motorCapacity,
                                        final int elokaResistance,
                                        @Nonnull Warhead warhead,
                                        @Nonnull List<MissileMotor> missileMotors,
                                        @Nonnull final AmmunitionModule ammunitionModule) {
        Preconditions.checkNotNull(typeName, "typeName shouldn't be null!");
        Preconditions.checkNotNull(warhead, "warhead shouldn't be null!");
        Preconditions.checkNotNull(missileMotors, "missileMotors shouldn't be null!");
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");

        final Missile missile = new Missile(typeName, warheadCapacity, motorCapacity, elokaResistance, warhead, missileMotors, research(), ammunitionModule);
        setId(missile);
        return missile;
    }

    @Nonnull
    public static Propulsion createPropulsion(@Nonnull final String name,
                                              @Nonnull final String description,
                                              final int useCapacity,
                                              final int value,
                                              final int level,
                                              final boolean ftlCapable,
                                              @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final Propulsion propulsion = new Propulsion(name, description, research(), useCapacity, value, level, ftlCapable, crewRequirement);
        setId(propulsion);
        return propulsion;
    }

    @Nonnull
    public static PassiveModule createPassiveModule(@Nonnull final String name,
                                                    @Nonnull final String description,
                                                    @Nonnull final ESupportType supportType,
                                                    @Nonnull final ECalculationType calculationType,
                                                    final int useCapacity,
                                                    final int value,
                                                    final int techLevel,
                                                    @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final PassiveModule passiveModule = new PassiveModule(name, description, research(), useCapacity, value, techLevel, supportType, calculationType, crewRequirement);
        setId(passiveModule);
        return passiveModule;
    }

    @Nonnull
    public static AmmunitionModule createAmmunitionModule(@Nonnull final String name,
                                                          @Nonnull final String description,
                                                          final int useCapacity,
                                                          final int value,
                                                          final int techLevel,
                                                          @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final AmmunitionModule ammunitionModule = new AmmunitionModule(name, description, research(), useCapacity, value, techLevel, crewRequirement);
        setId(ammunitionModule);
        return ammunitionModule;
    }
}
