package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.EWarheadType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "warhead")
@AttributeOverride(name = "id", column = @Column(name = "idWarhead"))
public class Warhead extends HasCosts {

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

    @Column(nullable = false)
    private int useCapacity;

    public Warhead() {
    }

    public Warhead(@Nonnull final String name,
                   @Nonnull final String description,
                   final int damageValue,
                   @Nonnull final ETechLevel techLevel,
                   @Nonnull final Distance damageProjectionRange,
                   @Nonnull final EWarheadType warheadType,
                   final int useCapacity) {
        super(new Translation(Translation.DEFAULT_LANGUAGE, name), new Translation(Translation.DEFAULT_LANGUAGE, description), techLevel, Warhead.class);
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(warheadType, "warheadType shouldn't be null!");

        this.damageValue = damageValue;
        this.damageProjectionRange = damageProjectionRange;
        this.warheadType = warheadType;
        this.useCapacity = useCapacity;
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

    public int getUseCapacity() {
        return useCapacity;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof Warhead)) return false;

        final Warhead warhead = (Warhead) o;

        return new EqualsBuilder().append(id, warhead.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
