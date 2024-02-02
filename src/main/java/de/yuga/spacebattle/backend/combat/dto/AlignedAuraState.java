package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class AlignedAuraState {

    @Nonnull
    @Enumerated(EnumType.STRING)
    private EWeaponAlignment alignment;

    @Nonnull
    @Convert(converter = DistanceConverter.class)
    private Distance antiShipMissileRange = Distance.ZERO.clone();

    @Nonnull
    @Convert(converter = DistanceConverter.class)
    private Distance antiMissileMissileRange = Distance.ZERO.clone();

    @Nonnull
    @Convert(converter = DistanceConverter.class)
    private Distance weaponRange = Distance.ZERO.clone();

    public AlignedAuraState() {
    }

    public AlignedAuraState(@Nonnull final EWeaponAlignment alignment) {
        this.alignment = Preconditions.checkNotNull(alignment, "alignment must not be empty");
    }

    @Nonnull
    public EWeaponAlignment getAlignment() {
        return alignment;
    }

    @Nonnull
    public Distance getAntiShipMissileRange() {
        return antiShipMissileRange;
    }

    public void setAntiShipMissileRange(@Nullable final Distance distance) {
        this.antiShipMissileRange = distance != null && distance.compareTo(this.antiShipMissileRange) > 0 ? distance : this.antiShipMissileRange;
    }

    @Nonnull
    public Distance getAntiMissileMissileRange() {
        return antiMissileMissileRange;
    }

    public void setAntiMissileMissileRange(@Nullable final Distance distance) {
        this.antiMissileMissileRange = distance != null && distance.compareTo(this.antiMissileMissileRange) > 0 ? distance : this.antiMissileMissileRange;
    }

    @Nonnull
    public Distance getWeaponRange() {
        return weaponRange;
    }

    public void setWeaponRange(@Nullable final Distance distance) {
        this.weaponRange = distance != null && distance.compareTo(this.weaponRange) > 0 ? distance : this.weaponRange;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final AlignedAuraState that = (AlignedAuraState) o;

        return new EqualsBuilder().append(alignment, that.alignment).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(alignment).toHashCode();
    }
}
