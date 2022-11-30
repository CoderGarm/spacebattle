package de.yuga.spacebattle.backend.calculator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class FittingUtils {

    public static final Predicate<AlignedFitting> OFFENSIVE_FITTING = new Predicate<AlignedFitting>() {
        @Override
        public boolean test(@Nonnull final AlignedFitting fitting) {
            Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

            return fitting.getWeaponType() == EWeaponType.MISSILE || fitting.getWeaponType() == EWeaponType.BEAM;
        }
    };

    public static final Predicate<AlignedFitting> DEFENSIVE_FITTING = new Predicate<AlignedFitting>() {
        @Override
        public boolean test(@Nonnull final AlignedFitting fitting) {
            Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

            return fitting.getWeaponType() == EWeaponType.COUNTER_MISSILE || fitting.getWeaponType() == EWeaponType.POINT_DEFENSE;
        }
    };

    public static final Predicate<AlignedFitting> MISSILES = new Predicate<AlignedFitting>() {
        @Override
        public boolean test(@Nonnull final AlignedFitting fitting) {
            Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

            return fitting.getWeaponType() == EWeaponType.MISSILE || fitting.getWeaponType() == EWeaponType.COUNTER_MISSILE;
        }
    };

    public static final Predicate<AlignedFitting> ATTACK_MISSILES = new Predicate<AlignedFitting>() {
        @Override
        public boolean test(@Nonnull final AlignedFitting fitting) {
            Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

            return fitting.getWeaponType() == EWeaponType.MISSILE;
        }
    };

    public static final Predicate<AlignedFitting> COUNTER_MISSILES = new Predicate<AlignedFitting>() {
        @Override
        public boolean test(@Nonnull final AlignedFitting fitting) {
            Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

            return fitting.getWeaponType() == EWeaponType.COUNTER_MISSILE;
        }
    };
}
