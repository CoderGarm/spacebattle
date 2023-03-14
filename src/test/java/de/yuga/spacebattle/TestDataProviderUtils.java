package de.yuga.spacebattle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.BattleLogger;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.*;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.i18n.Translatable;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
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
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.TestUtils.setId;
import static de.yuga.spacebattle.backend.enums.ETechnologyType.MILITARY;

public class TestDataProviderUtils {

    private static final EHullType HULL_TYPE = EHullType.CA;

    @Nonnull
    public static Cage cage() {
        final Planet pl = planet(2, 2);
        final FleetOrbit fo = new FleetOrbit(pl.getOrbit(), pl.getSystem());
        final Map.Entry<FleetOrbit, List<Fleet>> e = Map.entry(fo, List.of(minimalFleet(), minimalFleet()));
        final FleetClash fleetClash = new FleetClash(e);
        return new Cage(fleetClash, new BattleLogger("true"));
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
    public static Orbit orbit(final int x, final int y, final EDistanceMetric metric) {
        return new Orbit(new Distance(x, metric), new Distance(y, metric));
    }

    @Nonnull
    public static Orbit orbit(final BigDecimal x, final BigDecimal y, final EDistanceMetric metric) {
        return new Orbit(new Distance(x, metric), new Distance(y, metric));
    }

    @Nonnull
    public static StarSystem system(final int x, final int y) {
        final StarSystem sys = new StarSystem("anotherRandom", new Orbit(new Distance(x, StarSystem.STAR_SYSTEM_STANDARD_METRIC), new Distance(y, StarSystem.STAR_SYSTEM_STANDARD_METRIC)));
        setId(sys);
        return sys;
    }

    @Nonnull
    public static Planet planet(final int x, final int y) {
        final Orbit random = orbit(0, 0, Planet.PLANET_STANDARD_METRIC);
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
        MissileMotor shipKillerMotor = createMissileMotor(180, acc(46000, EAccelerationMetric.G), 20);
        Warhead nuclearShipKillerWarHead = createWarhead(damageValue, dis(50000, EDistanceMetric.M), EWarheadType.EXPLOSION);
        return createMissile(100, ETechLevel.TECH_I, nuclearShipKillerWarHead, shipKillerMotor);
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
        fleet.addShips(warShips);
        fleet.setOrbit(new FleetOrbit(planetStart.getOrbit(), planetStart.getSystem()));
        return fleet;
    }

    @Nonnull
    public static Distance dis(final BigDecimal value, EDistanceMetric metric) {
        return new Distance(value, metric);
    }

    @Nonnull
    public static Distance dis(final String value) {
        return Distance.valueOf(value);
    }

    @Nonnull
    public static Distance dis(final int value, EDistanceMetric metric) {
        return new Distance(value, metric);
    }

    @Nonnull
    public static Acceleration acc(final BigDecimal value, final EAccelerationMetric metric, final EHyperBand hyperBand) {
        return new Acceleration(value, metric, hyperBand);
    }

    @Nonnull
    public static Acceleration acc(final double value, EAccelerationMetric metric) {
        return new Acceleration(value, metric);
    }

    @Nonnull
    public static Time time(final int coordinate, final ETimeMetric timeMetric) {
        return new Time(coordinate, timeMetric);
    }

    @Nonnull
    public static Velocity vel(final int coordinate, final EDistanceMetric distanceMetric, final ETimeMetric timeMetric) {
        return new Velocity(coordinate, distanceMetric, timeMetric);
    }

    @Nonnull
    public static Acceleration acc(final int value, EAccelerationMetric metric) {
        return new Acceleration(value, metric);
    }

    /**
     * Returns directions for<br>
     * <ul>
     * <li> "up" </li>
     * <li> "down" </li>
     * <li> "right" </li>
     * <li> "left" </li>
     * <li> "upper right" </li>
     * <li> "upper left" </li>
     * </ul>
     */
    @Nonnull
    public static Direction dir(@Nonnull final String direction) {
        Preconditions.checkNotNull(direction, "direction shouldn't be null!");

        final Orbit destination;
        switch (direction) {
            case "up":
                destination = new Orbit(BigDecimal.ZERO, BigDecimal.ONE, EDistanceMetric.M);
                break;
            case "down":
                destination = new Orbit(BigDecimal.ZERO, BigDecimal.ONE.negate(), EDistanceMetric.M);
                break;
            case "right":
                destination = new Orbit(BigDecimal.ONE, BigDecimal.ZERO, EDistanceMetric.M);
                break;
            case "left":
                destination = new Orbit(BigDecimal.ONE.negate(), BigDecimal.ZERO, EDistanceMetric.M);
                break;
            case "upper right":
                destination = new Orbit(BigDecimal.ONE, BigDecimal.ONE, EDistanceMetric.M);
                break;
            case "upper left":
                destination = new Orbit(BigDecimal.ONE.negate(), BigDecimal.ONE, EDistanceMetric.M);
                break;
            default:
                return Direction.ZERO;
        }

        return new Direction(Orbit.getCenterOrbit(), destination);
    }

    @Nonnull
    public static ShipClass shipClass(final int effectFTLValue) {
        final User user = user();

        Map<EEducationType, Long> militaryCrew = militaryCrew();

        Armor armor = createArmor("Armor Mk I", "An armor", 5, 3000, ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Propulsion propulsionFTL = createPropulsion("FTL Speed Mk I", "A FTL drive", 10, effectFTLValue, ETechLevel.TECH_I, EHyperBand.DELTA, MILITARY);
        ElectronicWarfare electronicWarfare = createElectronicWarfare("Scanner Mk I", "A scanner", 5, 1000, dis(1000000, EDistanceMetric.M), ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Sidewall sidewall = createSidewall("Shield Mk I", "A shield", 5, 15000, ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS));

        MissileMotor shipKillerMotor = createMissileMotor(180, acc(46000, EAccelerationMetric.G), 20);
        Warhead nuclearShipKillerWarHead = createWarhead(1000, dis(50000, EDistanceMetric.M), EWarheadType.EXPLOSION);
        Missile shipKillerMissile = createMissile(100, ETechLevel.TECH_I, nuclearShipKillerWarHead, shipKillerMotor);
        Launcher shipKillerLauncher = createLauncher("Ship killer launcher Mk I", "The launcher for ship killers", 100, ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(shipKillerMissile));

        MissileMotor counterMissileMotor = createMissileMotor(5, acc(96000, EAccelerationMetric.G), 80);
        Warhead counterWarHead = createWarhead(1, Distance.ZERO, EWarheadType.COUNTER_MISSILE);
        Missile counterMissile = createMissile(10, ETechLevel.TECH_I, counterWarHead, counterMissileMotor);
        Launcher counterMissileLauncher = createLauncher("Counter missile launcher Mk I", "The launcher for counter missiles", 100, ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS), EWeaponType.COUNTER_MISSILE, Set.of(counterMissile));

        Weapon laserWeapon = createWeapon("Laser Mk I", "A laser", 5, 10, ETechLevel.TECH_I, dis(400000, EDistanceMetric.M), 1, EWeaponType.BEAM, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Weapon pointDefense = createWeapon("Point Defense Mk I", "A point defense", 5, 1, ETechLevel.TECH_I, dis(50000, EDistanceMetric.M), 1, EWeaponType.POINT_DEFENSE, new CrewRequirement(militaryCrew, EDepositType.COSTS));

        PassiveModule passiveModule = createPassiveModule("Improves armor", "Increases the amount of armor", ESupportType.ARMOR, ECalculationType.ADD, 5, 10, ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS));

        Hull hull3 = createHull("Cruiser vessel", 80000, 150, 45, 45, 75, "The cruiser hull", EHullType.CA, new CrewRequirement(militaryCrew, EDepositType.COSTS));

        ShipClass as3 = new ShipClass(user, "Argonauts cruiser", hull3, null);
        return createFitting(armor, propulsionFTL, electronicWarfare, sidewall, laserWeapon, pointDefense, new Launcher[]{shipKillerLauncher, counterMissileLauncher}, new PassiveModule[]{passiveModule}, as3);
    }

    @Nonnull
    public static Map<EEducationType, Long> militaryCrew() {
        Map<EEducationType, Long> militaryCrew = new HashMap<>();
        militaryCrew.put(EEducationType.ENLISTED, 20L);
        militaryCrew.put(EEducationType.OFFICER, 10L);
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

        final Hull hull = new Hull(name, overallConstructionCapacity, constructionCapacity, constructionCapacityBow, constructionCapacityStern, constructionCapacityBroadsides, ETechLevel.TECH_I, description, research(), hullType, crewRequirement);
        setId(hull);
        return hull;
    }

    private static ShipClass createFitting(Armor armor,
                                           Propulsion propulsionFTL,
                                           ElectronicWarfare electronicWarfare,
                                           Sidewall sidewall,
                                           Weapon laserWeapon,
                                           Weapon pointDefense,
                                           Launcher[] launchers,
                                           PassiveModule[] passiveModules,
                                           ShipClass shipClass) {
        shipClass.setArmor(armor);
        shipClass.setSidewall(sidewall);
        shipClass.setPropulsion(propulsionFTL);
        shipClass.setElectronicWarfare(electronicWarfare);
        Set<AlignedFitting> fittings = new HashSet<>();
        fittings.add(new AlignedFitting(EWeaponAlignment.BOW, laserWeapon, 1));
        Set<AmmunitionFitting> ammunitionFittings = new HashSet<>();
        Arrays.stream(launchers).forEach(launcher -> {
            fittings.add(new AlignedFitting(EWeaponAlignment.STERN, launcher, 1));
            ammunitionFittings.add(new AmmunitionFitting(new ArrayList<>(launcher.getAllowedMissiles()).get(0), 1));
        });
        fittings.add(new AlignedFitting(EWeaponAlignment.BROADSIDE, pointDefense, 1));
        shipClass.setFittings(fittings);
        shipClass.setAmmunitionFittings(ammunitionFittings);

        Set<SupportFitting> supportFittings = Arrays.stream(passiveModules).map(af -> new SupportFitting(af, 1)).collect(Collectors.toSet());
        shipClass.setSupportFittings(supportFittings);

        return shipClass;
    }

    @Nonnull
    public static Armor createArmor(@Nonnull final String name,
                                    @Nonnull final String description,
                                    final int costsPercentage,
                                    final int effectValue,
                                    final ETechLevel techLevel,
                                    @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final NamedTechLevel namedTechLevel = new NamedTechLevel(name, description, research(), techLevel, Armor.class);
        final Armor armor = new Armor(namedTechLevel, "XxX", 1, effectValue, costsPercentage, EHullType.CA);
        setId(armor);
        return armor;
    }

    @Nonnull
    public static ElectronicWarfare createElectronicWarfare(@Nonnull final String name,
                                                            @Nonnull final String description,
                                                            final int costsPercentage,
                                                            final int value,
                                                            @Nonnull final Distance effectiveRange,
                                                            final ETechLevel techLevel,
                                                            @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final NamedTechLevel namedTechLevel = new NamedTechLevel(name, description, research(), techLevel, ElectronicWarfare.class);
        final ElectronicWarfare electronicWarfare = new ElectronicWarfare(namedTechLevel, "yYy", 1, costsPercentage, value, EHullType.CA, effectiveRange);
        setId(electronicWarfare);
        return electronicWarfare;
    }

    @Nonnull
    public static Sidewall createSidewall(@Nonnull final String name,
                                          @Nonnull final String description,
                                          final int useCapacity,
                                          final int value,
                                          final ETechLevel techLevel,
                                          @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final NamedTechLevel namedTechLevel = new NamedTechLevel(name, description, research(), techLevel, Sidewall.class);
        final Sidewall sidewall = new Sidewall(namedTechLevel, "sSs", 1, useCapacity, value, EHullType.CA);
        setId(sidewall);
        return sidewall;
    }

    @Nonnull
    public static Weapon createWeapon(@Nonnull final String name,
                                      @Nonnull final String description,
                                      final int useCapacity,
                                      final int value,
                                      final ETechLevel techLevel,
                                      @Nonnull final Distance damageProjectionRange,
                                      final int amountDamageEmitter,
                                      @Nonnull final EWeaponType weaponType,
                                      @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final NamedTechLevel namedTechLevel = new NamedTechLevel(name, description, research(), techLevel, Weapon.class);
        final Weapon weapon = new Weapon(namedTechLevel, "gGg", 1, useCapacity, value, EHullType.CA, damageProjectionRange, amountDamageEmitter, weaponType, crewRequirement);
        setId(weapon);
        return weapon;
    }

    @Nonnull
    public static Launcher createLauncher(@Nonnull final String name,
                                          @Nonnull final String description,
                                          final int useCapacity,
                                          final ETechLevel techLevel,
                                          @Nonnull final CrewRequirement crewRequirement,
                                          @Nonnull final EWeaponType weaponType,
                                          @Nonnull final Set<Missile> allowedMissiles) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(allowedMissiles, "allowedMissiles shouldn't be null!");

        final NamedTechLevel namedTechLevel = new NamedTechLevel(name, description, research(), techLevel, Launcher.class);
        final Launcher launcher = new Launcher(namedTechLevel, "dwq", 1, useCapacity, EHullType.CA, crewRequirement, weaponType, allowedMissiles);
        setId(launcher);
        return launcher;
    }

    @Nonnull
    public static MissileMotor createMissileMotor(final int endurance,
                                                  @Nonnull final Acceleration acceleration,
                                                  final int maneuverability) {
        Preconditions.checkNotNull(acceleration, "acceleration shouldn't be null!");

        return new MissileMotor(endurance, maneuverability, acceleration);
    }

    @Nonnull
    public static Warhead createWarhead(final int effectValue,
                                        @Nonnull final Distance damageProjectionRange,
                                        @Nonnull final EWarheadType warheadType) {
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(warheadType, "warheadType shouldn't be null!");

        return new Warhead(damageProjectionRange, warheadType, effectValue);
    }

    @Nonnull
    public static Missile createMissile(final int elokaResistance,
                                        final ETechLevel techLevel,
                                        @Nonnull Warhead warhead,
                                        @Nonnull MissileMotor missileMotors) {
        Preconditions.checkNotNull(warhead, "warhead shouldn't be null!");
        Preconditions.checkNotNull(missileMotors, "missileMotors shouldn't be null!");

        final NamedTechLevel namedTechLevel = new NamedTechLevel("name", "description", research(), techLevel, Missile.class);
        final Missile missile = new Missile(namedTechLevel, "", 1, elokaResistance, 1, HULL_TYPE, warhead, missileMotors);
        setId(missile);
        return missile;
    }

    @Nonnull
    public static Propulsion createPropulsion(@Nonnull final String name,
                                              @Nonnull final String description,
                                              final int useCapacity,
                                              final int value,
                                              final ETechLevel techLevel,
                                              @Nonnull final EHyperBand hyperBand,
                                              @Nonnull final ETechnologyType technologyType) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(hyperBand, "hyperBand shouldn't be null!");
        Preconditions.checkNotNull(technologyType, "technologyType must not be empty");

        final NamedTechLevel namedTechLevel = new NamedTechLevel(name, description, research(), techLevel, Propulsion.class);
        final Propulsion propulsion = new Propulsion(namedTechLevel, "xXx", 1, useCapacity, value, hyperBand, technologyType);
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
                                                    final ETechLevel techLevel,
                                                    @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        final PassiveModule passiveModule = new PassiveModule(name, description, research(), useCapacity, value, HULL_TYPE, techLevel, supportType, calculationType, crewRequirement);
        setId(passiveModule);
        return passiveModule;
    }

    @Nonnull
    public static BigDecimal bd(final double x) {
        return BigDecimal.valueOf(x);
    }

    @Nonnull
    public static BigDecimal bd(final String x) {
        return new BigDecimal(x);
    }

    public static Building building() {
        final Building b = new Building();
        TestUtils.setId(b);
        final ProductionType productionType = new ProductionType();
        TestUtils.setFieldValue(productionType, "productionTarget", EResourceType.RESEARCH);
        TestUtils.setFieldValue(productionType, "productionCategory", EProductionCategory.PRODUCE);
        TestUtils.setFieldValue(b, "productionType", productionType);

        final ResourceDeposit resourceDeposit = createResourceDeposit();
        TestUtils.setFieldValue(b, "costs", resourceDeposit);
        TestUtils.setFieldValue(b, "techLevel", ETechLevel.TECH_I);

        final Translatable name = new Translatable();
        name.add(new Translation("en", "Construction Yard"));
        TestUtils.setFieldValue(b, "name", name);
        return b;
    }

    public static ResourceDeposit createResourceDeposit() {
        final EDepositType subType = EDepositType.COSTS;
        final ResourceDeposit resourceDeposit = new ResourceDeposit(subType);
        for (final EResourceType type : EResourceType.valuesWithoutPopulation()) {
            long rand = ThreadLocalRandom.current().nextLong(10, 51);
            resourceDeposit.setAbsoluteResourceValue(type, rand);
        }
        for (final EEducationType eEducationType : EEducationType.values()) {
            long rand = ThreadLocalRandom.current().nextLong(10, 51);
            resourceDeposit.setAbsoluteCrewRequirement(eEducationType, rand);
        }
        return resourceDeposit;
    }
}
