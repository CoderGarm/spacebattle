package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import org.apache.commons.lang3.Range;
import org.springframework.data.util.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines the alignment of a weapon or counter weapon.
 */
public enum EWeaponAlignment {

    /**
     * If it is aligned to the front.
     * These aligned weapons are mainly used in a hunt.
     */
    BOW(Pair.of(315, 45)),

    /**
     * If it is aligned to the back.
     * These aligned weapons are mainly used if the ship is fleeing.
     */
    STERN(Pair.of(135, 225)),

    /**
     * If it is aligned to the broad sides.
     * These aligned weapons are mainly used in sidewall battle.
     * <p>
     * Note that this alignment indicates that a such aligned weapon must be doubled by design.
     */
    BROADSIDE(Pair.of(45, 135), Pair.of(225, 315));

    /**
     * The angle from first thing to second thing which can be swept by the given alignment.<br>
     * Everything in between can be reached.
     */
    @Nonnull
    private final Pair<Integer, Integer>[] angle;

    @SafeVarargs
    EWeaponAlignment(@Nonnull final Pair<Integer, Integer>... angle) {
        Preconditions.checkNotNull(angle, "angle shouldn't be null!");

        this.angle = angle;
    }

    /**
     * Returns the alignments which could be used for the given geometric constellation.
     *
     * @param base            the base position
     * @param baseDirection   the direction of the bases' position
     * @param targetsPosition the targets position
     * @return the alignments which can be watch to the targets position
     */
    @Nonnull
    public static Set<EWeaponAlignment> getApplicableAlignments(@Nonnull final Orbit base,
                                                                @Nonnull final Direction baseDirection,
                                                                @Nonnull final Orbit targetsPosition) {
        Preconditions.checkNotNull(base, "base shouldn't be null!");
        Preconditions.checkNotNull(baseDirection, "baseDirection shouldn't be null!");
        Preconditions.checkNotNull(targetsPosition, "targetsPosition shouldn't be null!");

        final double angle = Direction.getAngleBetween(base, baseDirection, targetsPosition);
        return Arrays.stream(EWeaponAlignment.values()).filter(a ->
                Arrays.stream(a.angle).anyMatch(p -> {
                    if (angle < -360 || angle > 360) {
                        // if any wildcard angle is returned - the direct angle between could not be determined - everything is possible
                        return true;
                    }

                    final double first = (double) p.getFirst();
                    final double second = (double) p.getSecond();

                    if (second < first) {
                        // special case for the bow angle
                        final Range<Double> toZero = Range.between(second, 0D);
                        final Range<Double> fromZero = Range.between(first, 360D);
                        return toZero.contains(angle) || fromZero.contains(angle);
                    }

                    return angle >= first && angle <= second;
                })
        ).collect(Collectors.toSet());

    }

    /**
     * Checks if a given movement type matches with a weapons alignment.
     *
     * @param movementType the movement type to check
     * @return <code>true</code> if the movement type allows to fire this alignment, <code>false</code> otherwise
     */
    public boolean isAssignableFromMovementType(@Nullable final EMovementType movementType) {
        if (movementType == null) {
            return false;
        }

        switch (movementType) {
            case OFFENSIVE_ROLL:
                return this == BROADSIDE;
            case REDUCE_DISTANCE:
                return this == BOW;
            case INCREASE_DISTANCE:
                return this == STERN;
            case HOLD_DISTANCE:
                // every weapon is allowed because of free movement
                return true;
            default:
            case IMPELLER_WEDGE_PROTECTION:
                return false;
        }
    }
}
