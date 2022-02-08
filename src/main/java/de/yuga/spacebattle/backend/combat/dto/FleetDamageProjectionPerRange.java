package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
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
    private final List<DamagePerRangeAndAlignment> damageProjectionPerRanges = new ArrayList<>();

    private BigDecimal rangeWithBestDamage;

    public FleetDamageProjectionPerRange(@Nonnull final FleetRoundState roundState) {
        Preconditions.checkNotNull(roundState, "roundState shouldn't be null!");

        maximumWeaponRange = roundState.getMaximumWeaponRange();
        if (maximumWeaponRange.compareTo(BigDecimal.ZERO) == 0) {
            // noop because no weapon installed
            return;
        }

        for (BigDecimal range = BigDecimal.ZERO; range.compareTo(maximumWeaponRange) <= 0; ) {
            final BigDecimal lowerBound = range;
            range = range.add(RANGE_STEP);
            damageProjectionPerRanges.addAll(roundState.getDamagePerRange(lowerBound, range));
        }
    }

    public BigDecimal getMaximumWeaponRange() {
        return maximumWeaponRange;
    }

    @Nonnull
    public List<DamagePerRangeAndAlignment> getDamageProjectionPerRanges() {
        return damageProjectionPerRanges;
    }

    /**
     * Calculates the possible damage for the given firing range.
     *
     * @param range the range which has the damage applied to
     * @return the damage value
     */
    public long getDamageProjectionAtRanges(@Nonnull final BigDecimal range) {
        Preconditions.checkNotNull(range, "range shouldn't be null!");

        return damageProjectionPerRanges.stream().filter(d -> d.isInRange(range)).mapToLong(DamagePerRangeAndAlignment::getDamageValue).sum();
    }

    /**
     * Returns the alignment with the maximum possible damage at the given range.
     *
     * @param range the range
     * @return the alignment - null if no damage is possible at this range
     */
    @Nullable
    public EWeaponAlignment getAlignmentWithBestDamageForRange(@Nonnull final BigDecimal range) {
        Preconditions.checkNotNull(range, "range shouldn't be null!");

        final Map<EWeaponAlignment, Long> damagePerAlignment = damageProjectionPerRanges.stream()
                .filter(d -> d.isInRange(range))
                .collect(Collectors.groupingBy(DamagePerRangeAndAlignment::getWeaponAlignment,
                        Collectors.mapping(Function.identity(), Collectors.toList()))).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream().mapToLong(DamagePerRangeAndAlignment::getDamageValue).sum()));

        EWeaponAlignment alignment = null;
        long biggestDamage = Long.MIN_VALUE;
        for (final EWeaponAlignment key : damagePerAlignment.keySet()) {
            final Long aLong = damagePerAlignment.get(key);
            if (aLong > biggestDamage) {
                biggestDamage = aLong;
                alignment = key;
            }
        }
        return alignment;
    }

    /**
     * Detects the median of the best distance to the foe in order to project damage.
     *
     * @param foe the damage projection per range of the foe
     * @return the best combat distance
     */
    public BigDecimal getDistanceWithBestDamageAgainst(@Nonnull final FleetDamageProjectionPerRange foe) {
        Preconditions.checkNotNull(foe, "foe shouldn't be null!");

        if (rangeWithBestDamage != null) {
            return rangeWithBestDamage;
        }

        final List<DamageDifferenceAtRange> bestRanges = new ArrayList<>();
        int counter = 0;
        for (BigDecimal range = BigDecimal.ZERO; range.compareTo(maximumWeaponRange) <= 0; ) {
            final DamagePerRangeAndAlignment self = damageProjectionPerRanges.get(counter);
            final DamagePerRangeAndAlignment other = foe.getDamageProjectionPerRanges().get(counter);

            final long dmgAtRangeSelf = self.getDamageValue();
            final long dmgAtRangeOther = other.getDamageValue();
            if (dmgAtRangeSelf > dmgAtRangeOther) {
                bestRanges.add(new DamageDifferenceAtRange(range, dmgAtRangeSelf - dmgAtRangeOther));
            }
            range = range.add(RANGE_STEP);
        }
        if (!bestRanges.isEmpty()) {
            // if there is a clear damage diff - return the range with the biggest
            final List<DamageDifferenceAtRange> bestRangesSorted = bestRanges.stream()
                    .sorted(Comparator.comparingLong(DamageDifferenceAtRange::getDamageDifference))
                    .collect(Collectors.toList());
            rangeWithBestDamage = bestRangesSorted.get(bestRangesSorted.size() - 1).getRange();
        } else {
            final AtomicReference<Long> maxDamage = new AtomicReference<>(0L);
            final AtomicReference<BigDecimal> rangeWithMaxDamage = new AtomicReference<>(BigDecimal.ZERO);
            damageProjectionPerRanges.forEach(damagePerRangeAndAlignment -> {
                final long knownMaxDamage = maxDamage.get();
                final long currentDamage = damagePerRangeAndAlignment.getDamageValue();
                if (knownMaxDamage < currentDamage) {
                    rangeWithMaxDamage.set(damagePerRangeAndAlignment.getRangeDefinition().getMaxRange());
                }
            });

            // if there is no diff in damage per range - return range with the biggest damage
            rangeWithBestDamage = rangeWithMaxDamage.get();
        }
        return rangeWithBestDamage;
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
