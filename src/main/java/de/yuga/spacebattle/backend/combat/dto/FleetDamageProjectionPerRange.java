package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FleetDamageProjectionPerRange {

    /**
     * The range boundary width in meter.<br>
     * Currently, it should be set to 1.000 km.
     */
    private static final BigDecimal RANGE_STEP = BigDecimal.valueOf(1000000);

    private final BigDecimal maximumWeaponRange;

    /**
     * The damage output of the fleet which should be sorted by range, increasing per {@link #RANGE_STEP}.
     */
    @Nonnull
    private final List<DamageProjectionPerRange> damageProjectionPerRanges = new ArrayList<>();

    public FleetDamageProjectionPerRange(@Nonnull final FleetRoundState roundState) {
        Preconditions.checkNotNull(roundState, "roundState shouldn't be null!");

        maximumWeaponRange = roundState.getMaximumWeaponRange();
        if (maximumWeaponRange.compareTo(BigDecimal.ZERO) == 0) {
            // noop because no weapon installed
            return;
        }

        final long fleetDamage = roundState.getMaximumDamage();
        for (BigDecimal range = BigDecimal.ZERO; range.compareTo(maximumWeaponRange) <= 0; ) {
            final BigDecimal lowerBound = range;
            range = range.add(RANGE_STEP);
            final long damagePerRange = roundState.getDamagePerRange(lowerBound, range);
            final long relativeEffectiveDamage = 100 + damagePerRange / fleetDamage;
            damageProjectionPerRanges.add(new DamageProjectionPerRange(lowerBound, range, damagePerRange, relativeEffectiveDamage));
        }
    }

    public BigDecimal getMaximumWeaponRange() {
        return maximumWeaponRange;
    }

    @Nonnull
    public List<DamageProjectionPerRange> getDamageProjectionPerRanges() {
        return damageProjectionPerRanges;
    }

    /**
     * Calculates the possible damage for the given firing range.
     *
     * @param range the range which has the damage applied to
     * @return the damage value
     */
    public long getDamageProjectionAtRanges(final BigDecimal range) {
        return damageProjectionPerRanges.stream().filter(d -> d.isInRange(range)).mapToLong(DamageProjectionPerRange::getAbsoluteEffectiveDamage).sum();
    }

    /**
     * Detects the median of the best distance to the foe in order to project damage.
     *
     * @param foe the damage projection per range of the foe
     * @return the best combat distance
     */
    public BigDecimal getDistanceWithBestDamageAgainst(@Nonnull final FleetDamageProjectionPerRange foe) {
        Preconditions.checkNotNull(foe, "foe shouldn't be null!");

        final List<DamageDifferenceAtRange> bestRanges = new ArrayList<>();
        int counter = 0;
        for (BigDecimal range = BigDecimal.ZERO; range.compareTo(maximumWeaponRange) <= 0; ) {
            final DamageProjectionPerRange self = damageProjectionPerRanges.get(counter);
            final DamageProjectionPerRange other = foe.getDamageProjectionPerRanges().get(counter);

            final long dmgAtRangeSelf = self.getAbsoluteEffectiveDamage();
            final long dmgAtRangeOther = other.getAbsoluteEffectiveDamage();
            if (dmgAtRangeSelf > dmgAtRangeOther) {
                bestRanges.add(new DamageDifferenceAtRange(range, dmgAtRangeSelf - dmgAtRangeOther));
            }
            range = range.add(RANGE_STEP);
        }
        if (!bestRanges.isEmpty()) {
            // if there is a clear damage diff - return the range with the biggest
            final List<DamageDifferenceAtRange> bestRangesSorted = bestRanges.stream().sorted().collect(Collectors.toList());
            return bestRangesSorted.get(bestRangesSorted.size() - 1).getRange();
        }
        final List<DamageProjectionPerRange> maxRangesSelf = damageProjectionPerRanges.stream()
                .sorted(Comparator.comparingLong(DamageProjectionPerRange::getAbsoluteEffectiveDamage))
                .collect(Collectors.toList());
        // if there is no diff in damage per range - return range with the biggest damage
        return maxRangesSelf.get(maxRangesSelf.size() - 1).getMaxRange();
    }

    private static class DamageDifferenceAtRange implements Comparable<DamageDifferenceAtRange> {

        private final BigDecimal range;

        private final long damageDifference;

        private DamageDifferenceAtRange(@Nonnull final BigDecimal range, long damageDifference) {
            Preconditions.checkNotNull(range, "range shouldn't be null!");

            this.range = range;
            this.damageDifference = damageDifference;
        }

        public BigDecimal getRange() {
            return range;
        }

        public long getDamageDifference() {
            return damageDifference;
        }

        @Override
        public int compareTo(DamageDifferenceAtRange o) {
            return Long.compare(o.getDamageDifference(), getDamageDifference());
        }
    }
}
