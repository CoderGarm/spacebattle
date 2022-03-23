package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class FleetDamageProjectionPerRange {

    @Nonnull
    private final Distance maximumWeaponRange;

    @Nullable
    private DamagePerRangePerAlignment rangeWithBestDamage;

    /**
     * The damage output of the fleet pr range and alignment.
     */
    @Nonnull
    private final List<DamagePerRangePerAlignment> damagePotential = new ArrayList<>();

    public FleetDamageProjectionPerRange(@Nonnull final FleetRoundState roundState) {
        Preconditions.checkNotNull(roundState, "roundState shouldn't be null!");

        maximumWeaponRange = roundState.getMaximumWeaponRange();
        if (maximumWeaponRange.compareTo(Distance.ZERO) == 0) {
            // noop because no weapon installed
            return;
        }

        final List<DamagePerRangeAndAlignment> damageProjectionPerRanges = roundState.getDamagePerRangeAndAlignments();
        getChainedRangeDefinitions(damageProjectionPerRanges).forEach(rangeDefinition -> {
            final List<DamagePerRangeAndAlignment> inRange = damageProjectionPerRanges.stream()
                    .filter(d -> d.getRangeDefinition().isInRange(rangeDefinition))
                    .collect(Collectors.toList());
            damagePotential.add(new DamagePerRangePerAlignment(rangeDefinition, inRange));
        });
    }

    @Nonnull
    private List<RangeDefinition> getChainedRangeDefinitions(@Nonnull final List<DamagePerRangeAndAlignment> damageProjectionPerRanges) {
        Preconditions.checkNotNull(damageProjectionPerRanges, "damageProjectionPerRanges shouldn't be null!");

        final Set<RangeDefinition> rangeDefinitions = damageProjectionPerRanges.stream().map(DamagePerRangeAndAlignment::getRangeDefinition).collect(Collectors.toSet());
        final List<RangeDefinition> sortedRanges = rangeDefinitions.stream().sorted(new RangeDefinition.MaxRangeComparator()).collect(Collectors.toList());
        final List<RangeDefinition> result = new ArrayList<>();

        for (RangeDefinition r : sortedRanges) {
            if (result.isEmpty()) {
                result.add(r);
                continue;
            }
            final RangeDefinition last = result.get(result.size() - 1);
            final BigDecimal lastCoord = last.getMaxRange().getCoordinateInMetric(r.getDistanceMetric());
            final BigDecimal thisCoord = r.getMaxRange().getCoordinate();
            final RangeDefinition next = new RangeDefinition(lastCoord, thisCoord, r.getDistanceMetric());
            result.add(next);
        }

        return result;
    }

    @Nonnull
    public Distance getMaximumWeaponRange() {
        return maximumWeaponRange;
    }

    /**
     * Calculates the possible damage output for the given firing range and the alignment.
     *
     * @param range     the range which has the damage applied to
     * @param alignment the alignment
     * @return the damage potential
     */
    @Nullable
    public DamageProjectionPerRange getDamageProjectionAtRangeAndAlignment(@Nonnull final Distance range, @Nonnull final EWeaponAlignment alignment) {
        Preconditions.checkNotNull(range, "range shouldn't be null!");
        Preconditions.checkNotNull(alignment, "alignment shouldn't be null!");

        final List<DamagePerRangeAndAlignment> damages = damagePotential.stream()
                .map(d -> d.getDamagesPerRangeByAlignments(range, alignment))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        final Optional<RangeDefinition> min = damages.stream().min(Comparator.comparing(o -> o.getRangeDefinition().getMinRange())).map(DamagePerRangeAndAlignment::getRangeDefinition);
        final Optional<RangeDefinition> max = damages.stream().max(Comparator.comparing(o -> o.getRangeDefinition().getMaxRange())).map(DamagePerRangeAndAlignment::getRangeDefinition);
        if (min.isPresent()) {
            final Distance minRange = min.get().getMinRange();
            final Distance maxRange = max.get().getMaxRange();
            final long sum = damages.stream().mapToLong(DamagePerRangeAndAlignment::getDamageValue).sum();
            return new DamageProjectionPerRange(minRange, maxRange, sum);
        }
        return null;
    }

    /**
     * Returns the alignment with the maximum possible damage at the given range.
     *
     * @param range the range
     * @return the alignment - null if no damage is possible at this range
     */
    @Nullable
    public EWeaponAlignment getAlignmentWithBestDamageForRange(@Nonnull final Distance range) {
        Preconditions.checkNotNull(range, "range shouldn't be null!");

        final List<DamagePerRangePerAlignment> collect = damagePotential.stream().filter(d -> d.isInRange(range)).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return null;
        }
        final DamagePerRangePerAlignment max = Collections.max(collect, Comparator.comparingLong(d -> d.getMaximumPotentialDamage().getDamageValue()));
        final Map<EWeaponAlignment, Long> damagePerAlignment = max.getDamagePerAlignment();
        return Collections.max(damagePerAlignment.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * Checks if the fleet can attack at the given range by the given alignment.
     *
     * @param range     the range
     * @param alignment the alignment
     * @return <code>true</code> if an attack is possible, <code>false</code> otherwise
     */
    public boolean canAttackAtRangeOnSide(@Nonnull final Distance range, @Nonnull final EWeaponAlignment alignment) {
        Preconditions.checkNotNull(range, "range shouldn't be null!");
        Preconditions.checkNotNull(alignment, "alignment shouldn't be null!");

        return damagePotential.stream().anyMatch(d -> d.getDamagePerAlignment().containsKey(alignment) && d.isInRange(range));
    }

    /**
     * Detects the median of the best distance to the foe in order to project damage.
     *
     * @param foe the damage projection per range of the foe
     * @return the best combat distance
     */
    @Nullable
    public DamagePerRangePerAlignment getDistanceWithBestDamageAgainst(@Nonnull final FleetDamageProjectionPerRange foe) {
        Preconditions.checkNotNull(foe, "foe shouldn't be null!");

        if (rangeWithBestDamage != null) {
            return rangeWithBestDamage;
        }

        final List<DamagePerRangePerAlignment> damagesPerRangeSelf = getDamagesPerRange();
        final List<DamagePerRangePerAlignment> damagesPerRangeFoe = foe.getDamagesPerRange();
        // todo compare dmg difference and show result
        final List<DamagePerRangePerAlignment> bestRanges = damagesPerRangeSelf.stream()
                .filter(self -> damagesPerRangeFoe.stream()
                        .filter(dmgFoe -> self.getMaximumPotentialDamage().getDamageValue() > dmgFoe.getMaximumPotentialDamage().getDamageValue())
                        .findFirst()
                        .orElse(null) != null)
                .collect(Collectors.toList());

        if (!bestRanges.isEmpty()) {
            // if there is a clear damage diff - return the range with the biggest
            rangeWithBestDamage = Collections.max(bestRanges, Comparator.comparingLong(d -> d.getMaximumPotentialDamage().getDamageValue()));
        } else {
            // if there is no diff in damage per range - return range with the biggest damage
            if (!damagesPerRangeSelf.isEmpty()) {
                rangeWithBestDamage = Collections.max(damagesPerRangeSelf, Comparator.comparingLong(d -> d.getMaximumPotentialDamage().getDamageValue()));
            }
        }
        return rangeWithBestDamage;
    }

    /**
     * Calculates the damage potential per range and alignment.
     *
     * @return the damage potentials
     */
    @Nonnull
    public List<DamagePerRangePerAlignment> getDamagesPerRange() {
        return damagePotential;
    }
}
