package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FleetDamageProjectionPerRange {

    /**
     * The range boundary width in meter.<br>
     * Currently, it should be set to 1.000 km.
     */
    private static final BigDecimal RANGE_STEP = BigDecimal.valueOf(1000000);

    @Nonnull
    private final BigDecimal maximumWeaponRange;

    /**
     * The damage output of the fleet which should be sorted by range, increasing per {@link #RANGE_STEP}.
     */
    @Nonnull
    private final List<DamagePerRangeAndAlignment> damageProjectionPerRanges = new ArrayList<>();

    @Nullable
    private RangeDefinition rangeWithBestDamage;

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

    @Nonnull
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

        final Map<EWeaponAlignment, Long> damagePerAlignment = getDamageAtRangeByAlignment(range);
        if (damagePerAlignment.isEmpty()) {
            return null;
        }
        return Collections.max(damagePerAlignment.entrySet(), Comparator.comparingLong(Map.Entry::getValue)).getKey();
    }

    /**
     * Checks if the fleet can attack at the given range by the given alignment.
     *
     * @param range     the range
     * @param alignment the alignment
     * @return <code>true</code> if an attack is possible, <code>false</code> otherwise
     */
    public boolean canAttackAtRangeOnSide(@Nonnull final BigDecimal range, @Nonnull final EWeaponAlignment alignment) {
        Preconditions.checkNotNull(range, "range shouldn't be null!");
        Preconditions.checkNotNull(alignment, "alignment shouldn't be null!");

        return damageProjectionPerRanges.stream().anyMatch(d -> d.getWeaponAlignment() == alignment && d.isInRange(range));
    }

    /**
     * Checks if the fleet can attack at the given range by the given weaponType.
     *
     * @param range      the range
     * @param weaponType the weaponType
     * @return the alignment which is suitable for best damage application
     */
    @Nullable
    public EWeaponAlignment canAttackAtRangeAndWeaponType(@Nonnull final BigDecimal range, @Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(range, "range shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        final List<DamagePerRangeAndAlignment> damages = damageProjectionPerRanges.stream()
                .filter(d -> d.getWeaponType() == weaponType && d.isInRange(range))
                .collect(Collectors.toList());
        if (damages.isEmpty()) {
            return null;
        }
        return Collections.max(damages, Comparator.comparingLong(DamagePerRangeAndAlignment::getDamageValue)).getWeaponAlignment();
    }

    /**
     * Returns the damage per alignment at the given range.
     *
     * @param range the range
     * @return the damage per alignment
     */
    @Nonnull
    public Map<EWeaponAlignment, Long> getDamageAtRangeByAlignment(@Nonnull final BigDecimal range) {
        Preconditions.checkNotNull(range, "range shouldn't be null!");

        return damageProjectionPerRanges.stream()
                .filter(d -> d.isInRange(range))
                .collect(Collectors.groupingBy(DamagePerRangeAndAlignment::getWeaponAlignment,
                        Collectors.mapping(Function.identity(), Collectors.toList()))).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream().mapToLong(DamagePerRangeAndAlignment::getDamageValue).sum()));
    }

    /**
     * Detects the median of the best distance to the foe in order to project damage.
     *
     * @param foe the damage projection per range of the foe
     * @return the best combat distance
     */
    @Nullable
    public RangeDefinition getDistanceWithBestDamageAgainst(@Nonnull final FleetDamageProjectionPerRange foe) {
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
                bestRanges.add(new DamageDifferenceAtRange(range, range.add(RANGE_STEP), dmgAtRangeSelf - dmgAtRangeOther));
            }
            range = range.add(RANGE_STEP);
        }
        if (!bestRanges.isEmpty()) {
            // if there is a clear damage diff - return the range with the biggest
            final List<DamageDifferenceAtRange> bestRangesSorted = bestRanges.stream()
                    .sorted(Comparator.comparingLong(DamageDifferenceAtRange::getDamageDifference))
                    .collect(Collectors.toList());
            // todo properly do the same like at the pure damage calculation
            rangeWithBestDamage = !bestRangesSorted.isEmpty() ? bestRangesSorted.get(bestRangesSorted.size() - 1).getRangeDefinition() : null;
        } else {
            final DamagePerRangePerAlignment max = getRangeWithMaxDamage();
            // if there is no diff in damage per range - return range with the biggest damage
            rangeWithBestDamage = max != null ? max.getRangeDefinition() : null;
        }
        return rangeWithBestDamage;
    }

    /**
     * Calculates the range with the biggest damage potential.
     *
     * @return the highest damage potential
     */
    private DamagePerRangePerAlignment getRangeWithMaxDamage() {
        final Map<RangeDefinition, List<DamagePerRangeAndAlignment>> dmgPerRangeDefinition = damageProjectionPerRanges.stream()
                .collect(Collectors.groupingBy(DamagePerRangeAndAlignment::getRangeDefinition,
                        Collectors.mapping(Function.identity(), Collectors.toList())));
        final List<DamagePerRangePerAlignment> dmgPerRangePerAlignment = dmgPerRangeDefinition.entrySet().stream()
                .map(e -> new DamagePerRangePerAlignment(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DamagePerRangePerAlignment::getRangeDefinition))
                .collect(Collectors.toList());

        final List<DamagePerRangePerAlignment> mergedDamages = new ArrayList<>();
        for (DamagePerRangePerAlignment dmgO : dmgPerRangePerAlignment) {
            DamagePerRangePerAlignment merged = dmgO;
            for (DamagePerRangePerAlignment dmgI : dmgPerRangePerAlignment) {
                if (merged != dmgI && merged.isRangeChainingAndDamageEquals(dmgI)) {
                    merged = DamagePerRangePerAlignment.merge(merged, dmgI);
                }
            }
            final DamagePerRangePerAlignment checker = merged;
            final boolean isPresent = mergedDamages.stream().anyMatch(dmg -> dmg.isInRangeAndDamageEquals(checker));
            if (!isPresent) {
                mergedDamages.add(merged);
            }
        }

        return Collections.max(mergedDamages, Comparator.comparingLong(DamagePerRangePerAlignment::getMaximumPotentialDamage));
    }

    private static class DamageDifferenceAtRange implements Comparable<DamageDifferenceAtRange> {

        @Nonnull
        private final RangeDefinition rangeDefinition;

        private final long damageDifference;

        private DamageDifferenceAtRange(@Nonnull final BigDecimal minRange, @Nonnull final BigDecimal maxRange, long damageDifference) {
            Preconditions.checkNotNull(minRange, "minRange shouldn't be null!");
            Preconditions.checkNotNull(maxRange, "maxRange shouldn't be null!");

            this.rangeDefinition = new RangeDefinition(minRange, maxRange);
            this.damageDifference = damageDifference;
        }

        @Nonnull
        public RangeDefinition getRangeDefinition() {
            return rangeDefinition;
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
