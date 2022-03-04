package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EDistanceMetric;

import javax.annotation.Nonnull;

/**
 * Holds the information about a range and the applicable damage for that range.
 */
public class DamageProjectionPerRange {

    /**
     * The minimal range from where the damage can be projected.
     */
    @Nonnull
    private final RangeDefinition range;

    /**
     * The damage per salvo in absolute units.
     */
    private final long damageValue;

    public DamageProjectionPerRange(@Nonnull final RangeDefinition range,
                                    final long damageValue) {
        Preconditions.checkNotNull(range, "range shouldn't be null!");

        this.range = range;
        this.damageValue = damageValue;
    }

    public DamageProjectionPerRange(@Nonnull final Distance minRange, @Nonnull final Distance maxRange, final long damageValue) {
        Preconditions.checkNotNull(minRange, "minRange shouldn't be null!");
        Preconditions.checkNotNull(maxRange, "maxRange shouldn't be null!");

        final EDistanceMetric distanceMetric = minRange.getDistanceMetric();
        this.range = new RangeDefinition(minRange.getCoordinate(), maxRange.getCoordinateInMetric(distanceMetric), distanceMetric);
        this.damageValue = damageValue;
    }

    /**
     * States if the given distance is inside these boundaries.
     *
     * @param distance the given range
     * @return <code>true</code> if the distance is inside the boundaries, <code>false</code> otherwise
     */
    public boolean isInRange(@Nonnull final Distance distance) {
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");

        return range.isInRange(distance);
    }

    @Nonnull
    public RangeDefinition getRange() {
        return range;
    }

    public long getDamageValue() {
        return damageValue;
    }
}
