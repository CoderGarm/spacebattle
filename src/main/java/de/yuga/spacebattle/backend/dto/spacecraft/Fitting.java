package de.yuga.spacebattle.backend.dto.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByOwn;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Fitting {

    @Nonnull
    private final List<Propulsion> propulsions;

    @Nonnull
    private final List<Armor> armors;

    @Nonnull
    private final List<ElectronicWarfare> eloka;

    @Nonnull
    private final List<Sidewall> sidewalls;

    @Nonnull
    private final List<Weapon> weapons;

    @Nonnull
    private final Map<Launcher, Missile> missiles;

    @Nonnull
    private final List<PassiveModule> passiveModules;

    public Fitting(@Nonnull final List<Propulsion> propulsions,
                   @Nonnull final List<Armor> armors,
                   @Nonnull final List<ElectronicWarfare> eloka,
                   @Nonnull final List<Sidewall> sidewalls,
                   @Nonnull final List<Weapon> weapons,
                   @Nonnull final Map<Launcher, Missile> missiles,
                   @Nonnull final List<PassiveModule> passiveModules) {
        this.propulsions = Preconditions.checkNotNull(propulsions, "propulsions must not be empty");
        this.armors = Preconditions.checkNotNull(armors, "armors must not be empty");
        this.eloka = Preconditions.checkNotNull(eloka, "eloka must not be empty");
        this.sidewalls = Preconditions.checkNotNull(sidewalls, "sidewalls must not be empty");
        this.weapons = Preconditions.checkNotNull(weapons, "weapons must not be empty");
        this.missiles = Preconditions.checkNotNull(missiles, "missiles must not be empty");
        this.passiveModules = Preconditions.checkNotNull(passiveModules, "passiveModules must not be empty");
    }

    public Fitting(@Nonnull final List<Propulsion> propulsions,
                   @Nonnull final List<Armor> armors,
                   @Nonnull final List<ElectronicWarfare> electronicWarfares,
                   @Nonnull final List<Sidewall> sidewalls,
                   @Nonnull final List<Weapon> weapons,
                   @Nonnull final List<Launcher> launchers,
                   @Nonnull final List<PassiveModule> passiveModules) {
        this(propulsions, armors, electronicWarfares, sidewalls, weapons,
                launchers.stream().collect(Collectors.toMap(Function.identity(), Launcher::getHeaviestMissile)),
                passiveModules);
    }

    @Nonnull
    public List<Propulsion> getPropulsions() {
        return propulsions;
    }

    @Nonnull
    public List<Armor> getArmors() {
        return armors;
    }

    @Nonnull
    public List<ElectronicWarfare> getEloka() {
        return eloka;
    }

    @Nonnull
    public List<Sidewall> getSidewalls() {
        return sidewalls;
    }

    @Nonnull
    public List<Weapon> getWeapons() {
        return weapons;
    }

    @Nonnull
    public Map<Launcher, Missile> getMissiles() {
        return missiles;
    }

    @Nonnull
    public List<PassiveModule> getPassiveModules() {
        return passiveModules;
    }

    @Nullable
    public <MODULE extends HasCostsByOwn> MODULE getByType(@Nonnull final EShipClassType shipClassType, @Nonnull final Collection<MODULE> elements) {
        Preconditions.checkNotNull(shipClassType, "shipClassType must not be empty");
        Preconditions.checkNotNull(elements, "elements must not be empty");

        return elements.stream()
                .sorted(Comparator.comparingInt(HasCostsByOwn::getEffectValue))
                .filter(a -> a.getShipClassType().suitsShipClassType(shipClassType))
                .findFirst().orElse(null);
    }

    @Nullable
    public Weapon getWeapon(@Nonnull final EShipClassType shipClassType, @Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(shipClassType, "shipClassType must not be empty");
        Preconditions.checkNotNull(weaponType, "weaponType must not be empty");

        return weapons.stream()
                .sorted(Comparator.comparingInt(HasCostsByOwn::getEffectValue))
                .filter(a -> shipClassType.suitsShipClassType(a.getShipClassType()))
                .filter(a -> a.getWeaponType() == weaponType)
                .findFirst().orElse(null);
    }

    @Nullable
    public Map.Entry<Launcher, Missile> getLauncher(@Nonnull final EShipClassType shipClassType, @Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(shipClassType, "shipClassType must not be empty");
        Preconditions.checkNotNull(weaponType, "weaponType must not be empty");

        return missiles.entrySet().stream()
                .filter(a -> shipClassType.suitsShipClassType(a.getKey().getShipClassType()))
                .filter(a -> a.getKey().getWeaponType() == weaponType)
                .findFirst().orElse(null);
    }

    @Nonnull
    public Propulsion getProp(@Nonnull final ETechnologyType technologyType) {
        Preconditions.checkNotNull(technologyType, "technologyType must not be empty");

        return propulsions.stream()
                .sorted(Comparator.comparing(Propulsion::getHyperBand))
                .filter(p -> p.getTechnologyType() == technologyType)
                .reduce((o1, o2) -> o2).orElseThrow(NullPointerException::new);
    }
}
