package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.util.Arrays;

public enum ETranslationTarget {

    BUILDING(Building.class),
    HULL(Hull.class),
    RESEARCH(Research.class),
    MISSILE(Missile.class),
    WARHEAD(Warhead.class),
    MISSILE_MOTOR(MissileMotor.class),
    LAUNCHER(Launcher.class),
    ARMOR(Armor.class),
    ELECTRONIC_WARFARE(ElectronicWarfare.class),
    PROPULSION(Propulsion.class),
    WEAPON(Weapon.class),
    PASSIVE_MODULE(PassiveModule.class),
    SIDEWALL(Sidewall.class),
    AMMUNITION_MODULE(AmmunitionModule.class);

    @Nonnull
    private final Class<?> clazz;

    ETranslationTarget(@Nonnull final Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "clazz must not be empty");

        this.clazz = clazz;
    }

    @Nonnull
    public Class<?> getClazz() {
        return clazz;
    }

    public static ETranslationTarget getByClazz(@Nonnull final Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "clazz must not be empty");

        return Arrays.stream(ETranslationTarget.values())
                .filter(value -> value.getClazz().isAssignableFrom(clazz))
                .findFirst()
                .orElseThrow(() -> new NotifyWebUserException("You shouldn't use this if it is not mapped."));
    }
}
