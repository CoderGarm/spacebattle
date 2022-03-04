package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DamagePerRangePerAlignment {

    /**
     * The range definition.
     */
    @Nonnull
    private final RangeDefinition rangeDefinition;

    /**
     * The damage per salvo relative to the total amount of damage which can be applied over all ranges.
     */
    @Nonnull
    private final Map<EWeaponAlignment, Long> damagePerAlignment = new HashMap<>();

    @Nonnull
    private List<DamagePerRangeAndAlignment> damagesPerRangeAndAlignments = new ArrayList<>();

    public DamagePerRangePerAlignment(@Nonnull final RangeDefinition rangeDefinition,
                                      @Nonnull final List<DamagePerRangeAndAlignment> damagesPerRangeAndAlignments) {
        Preconditions.checkNotNull(rangeDefinition, "rangeDefinition shouldn't be null!");
        Preconditions.checkNotNull(damagesPerRangeAndAlignments, "damagesPerRangeAndAlignments shouldn't be null!");

        this.rangeDefinition = rangeDefinition;
        this.damagesPerRangeAndAlignments = damagesPerRangeAndAlignments;
        damagesPerRangeAndAlignments.stream()
                .filter(d -> d.getRangeDefinition().equals(rangeDefinition))
                .forEach(damagePerRangeAndAlignment -> {
                    final EWeaponAlignment alignment = damagePerRangeAndAlignment.getWeaponAlignment();
                    final long damageValue = damagePerRangeAndAlignment.getDamageValue();
                    damagePerAlignment.merge(alignment, damageValue, Long::sum);
                });
    }

    public DamagePerRangePerAlignment(@Nonnull final RangeDefinition rangeDefinition, @Nonnull final Map<EWeaponAlignment, Long> damagePerAlignment) {
        Preconditions.checkNotNull(rangeDefinition, "rangeDefinition shouldn't be null!");
        Preconditions.checkNotNull(damagePerAlignment, "damagePerAlignment shouldn't be null!");

        this.rangeDefinition = rangeDefinition;
        this.damagePerAlignment.putAll(damagePerAlignment);
    }

    /**
     * Merges the ranges of the two given parameters, the most minimum and most maximum will define the resulting range definition.<br>
     * The damage potential will be the same as in both single parameters before.
     *
     * @param one first
     * @param two second
     * @return the merged object
     */
    public static DamagePerRangePerAlignment merge(@Nonnull final DamagePerRangePerAlignment one,
                                                   @Nonnull final DamagePerRangePerAlignment two) {
        Preconditions.checkNotNull(one, "one shouldn't be null!");
        Preconditions.checkNotNull(two, "two shouldn't be null!");
        Preconditions.checkState(one.getDamagePerAlignment().equals(two.getDamagePerAlignment()), "the given damage potentials must be equals");

        final EDistanceMetric metricOne = one.getRangeDefinition().getDistanceMetric();
        final EDistanceMetric metricTwo = two.getRangeDefinition().getDistanceMetric();
        final EDistanceMetric biggerMetric = metricOne.getDigitCount() > metricTwo.getDigitCount() ? metricOne : metricTwo;

        final Distance minRangeOne = one.getRangeDefinition().getMinRange();
        final Distance minRangeTwo = two.getRangeDefinition().getMinRange();

        final Distance maxRangeOne = one.getRangeDefinition().getMaxRange();
        final Distance maxRangeTwo = two.getRangeDefinition().getMaxRange();

        final BigDecimal minRangeInMetric = minRangeOne.min(minRangeTwo).getCoordinateInMetric(biggerMetric);
        final BigDecimal maxRangeInMetric = maxRangeOne.max(maxRangeTwo).getCoordinateInMetric(biggerMetric);
        final RangeDefinition rangeDefinition = new RangeDefinition(minRangeInMetric, maxRangeInMetric, biggerMetric);

        final Map<EWeaponAlignment, Long> damagePerAlignment = new HashMap<>();
        one.getDamagePerAlignment().forEach((alignment, damageValue) -> damagePerAlignment.merge(alignment, damageValue, Long::sum));
        return new DamagePerRangePerAlignment(rangeDefinition, damagePerAlignment);
    }

    @Nonnull
    public RangeDefinition getRangeDefinition() {
        return rangeDefinition;
    }

    @Nonnull
    public Map<EWeaponAlignment, Long> getDamagePerAlignment() {
        return damagePerAlignment;
    }

    @Nonnull
    public List<DamagePerRangeAndAlignment> getDamagesPerRangeByAlignments(@Nonnull final Distance range, @Nonnull final EWeaponAlignment alignment) {
        Preconditions.checkNotNull(range, "range shouldn't be null!");
        Preconditions.checkNotNull(alignment, "alignment shouldn't be null!");

        return damagesPerRangeAndAlignments.stream().filter(d -> d.getWeaponAlignment() == alignment && d.isInRange(range)).collect(Collectors.toList());
    }

    @Nonnull
    public DamageProjectionPerRange getMaximumPotentialDamage() {
        return new DamageProjectionPerRange(rangeDefinition, damagePerAlignment.values().stream().mapToLong(Long::longValue).sum());
    }

    /**
     * States if the given distance is inside these boundaries.
     *
     * @param distance the given range
     * @return <code>true</code> if the distance is inside the boundaries, <code>false</code> otherwise
     */
    public boolean isInRange(@Nonnull final Distance distance) {
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");

        return rangeDefinition.isInRange(distance.getCoordinateInMetric(rangeDefinition.getDistanceMetric()));
    }

    /**
     * States that the given parameter chains the range of this and has the same damage profile.
     *
     * @param that the parameter to check
     * @return <code>true</code> if this and that are matching each other, <code>false</code> otherwise
     */
    public boolean isRangeChainingAndDamageEquals(@Nonnull final DamagePerRangePerAlignment that) {
        Preconditions.checkNotNull(that, "that shouldn't be null!");

        return rangeDefinition.isChainingRange(that.getRangeDefinition()) && damagePerAlignment.equals(that.getDamagePerAlignment());
    }

    /**
     * States that the ranges and the damage potential are matching the parameter.
     *
     * @param that the param
     * @return <code>true</code> if this and that are matching each other, <code>false</code> otherwise
     */
    public boolean isInRangeAndDamageEquals(@Nonnull final DamagePerRangePerAlignment that) {
        Preconditions.checkNotNull(that, "that shouldn't be null!");

        return rangeDefinition.isInRange(that.getRangeDefinition()) && damagePerAlignment.equals(that.getDamagePerAlignment());
    }
}
