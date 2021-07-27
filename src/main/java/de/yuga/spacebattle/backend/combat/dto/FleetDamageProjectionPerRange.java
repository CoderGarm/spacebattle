package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetCapabilities;

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

    @Nonnull
    private final Fleet fleet;

    private final BigDecimal maximumWeaponRange;

    private BigDecimal bestDamageRange;

    /**
     * The damage output of the fleet which should be sorted by range, increasing per {@link #RANGE_STEP}.
     */
    @Nonnull
    private final List<DamageProjectionPerRange> damageProjectionPerRanges = new ArrayList<>();

    public FleetDamageProjectionPerRange(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        this.fleet = fleet;
        maximumWeaponRange = fleet.getMaximumWeaponRange();
        if (maximumWeaponRange.compareTo(BigDecimal.ZERO) == 0) {
            // noop because no weapon installed
            return;
        }
        final FleetCapabilities fleetCapabilities = new FleetCapabilities(fleet);
        final BigDecimal fleetDamage = fleetCapabilities.getEffectValueByModuleType().get(EModuleType.WEAPON);
        for (BigDecimal range = BigDecimal.ZERO; range.compareTo(maximumWeaponRange) <= 0; ) {
            final BigDecimal lowerBound = range;
            range = range.add(RANGE_STEP);
            final long damagePerRange = fleet.getDamagePerRange(lowerBound, range);
            final long relativeEffectiveDamage = 100 + damagePerRange / fleetDamage.intValue();
            damageProjectionPerRanges.add(new DamageProjectionPerRange(lowerBound, range, damagePerRange, relativeEffectiveDamage));
        }
    }

    @Nonnull
    public Fleet getFleet() {
        return fleet;
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

        if (bestDamageRange != null) {
            // todo state change if setup
            return bestDamageRange;
        }

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
            bestDamageRange = bestRangesSorted.get(bestRangesSorted.size() - 1).getRange();
            return bestDamageRange;
        }
        final List<DamageProjectionPerRange> maxRangesSelf = damageProjectionPerRanges.stream()
                .sorted(Comparator.comparingLong(DamageProjectionPerRange::getAbsoluteEffectiveDamage))
                .collect(Collectors.toList());
        // if there is no diff in damage per range - return range with the biggest damage
        bestDamageRange = maxRangesSelf.get(maxRangesSelf.size() - 1).getMaxRange();
        return bestDamageRange;
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
