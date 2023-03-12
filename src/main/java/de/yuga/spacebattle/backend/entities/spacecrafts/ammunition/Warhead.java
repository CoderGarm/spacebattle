package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EWarheadType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Embeddable
public class Warhead {

    @NotNull
    @Column(nullable = false)
    private long damageValue;

    /**
     * Defines the range of this weapon in meter.
     */
    @Convert(converter = DistanceConverter.class)
    private Distance damageProjectionRange;

    /**
     * The way of damage projection.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EWarheadType warheadType;

    public Warhead() {
    }

    public Warhead(@Nonnull final Distance damageProjectionRange,
                   @Nonnull final EWarheadType warheadType,
                   final int damageValue) {
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(warheadType, "warheadType shouldn't be null!");

        this.damageValue = damageValue;
        this.damageProjectionRange = damageProjectionRange;
        this.warheadType = warheadType;
    }

    public long getDamageValue() {
        return damageValue;
    }

    public Distance getDamageProjectionRange() {
        return damageProjectionRange;
    }

    @Nonnull
    public EWarheadType getWarheadType() {
        return warheadType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Warhead warhead = (Warhead) o;

        return new EqualsBuilder().append(damageValue, warhead.damageValue).append(damageProjectionRange, warhead.damageProjectionRange).append(warheadType, warhead.warheadType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(damageValue).append(damageProjectionRange).append(warheadType).toHashCode();
    }
}
