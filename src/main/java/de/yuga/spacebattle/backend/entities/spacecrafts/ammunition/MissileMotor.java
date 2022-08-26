package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.AccelerationConverter;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "missileMotor")
@AttributeOverride(name = "id", column = @Column(name = "idMissileMotor"))
public class MissileMotor extends HasCosts {

    /**
     * The duration which the missile engine can fire and accelerate the missile in seconds.
     */
    @Column(nullable = false)
    private int endurance;

    /**
     * The acceleration in gravity earth which is set if using the engine.
     */
    @Nonnull
    @NotNull
    @Convert(converter = AccelerationConverter.class)
    private Acceleration acceleration;

    /**
     * Defines the capability of this weapon to penetrate the shield. todo
     * The means the maneuver capability to find a gap in the tank to fire into it, for instance.
     */
    @Column(nullable = false)
    private int maneuverability;

    @Column(nullable = false)
    private int useCapacity;

    public MissileMotor() {
    }

    public MissileMotor(@Nonnull final String name,
                        @Nonnull final String description,
                        final int endurance,
                        @Nonnull final ETechLevel techLevel,
                        @Nonnull final Acceleration acceleration,
                        final int maneuverability,
                        final int useCapacity) {
        super(new Translation("en", name), new Translation("en", description), techLevel, MissileMotor.class);
        Preconditions.checkNotNull(acceleration, "acceleration shouldn't be null!");

        this.endurance = endurance;
        this.acceleration = acceleration;
        this.maneuverability = maneuverability;
        this.useCapacity = useCapacity;
    }

    public int getEndurance() {
        return endurance;
    }

    public Acceleration getAcceleration() {
        return acceleration;
    }

    public int getManeuverability() {
        return maneuverability;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof MissileMotor)) return false;

        final MissileMotor that = (MissileMotor) o;

        return new EqualsBuilder().append(id, that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
