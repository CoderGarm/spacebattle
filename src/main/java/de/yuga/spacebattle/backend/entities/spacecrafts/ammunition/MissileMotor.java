package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import de.yuga.spacebattle.backend.converter.AccelerationConverter;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class MissileMotor {

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

    public MissileMotor() {
    }

    public MissileMotor(final int endurance,
                        final int maneuverability,
                        @Nonnull final Acceleration acceleration) {

        this.endurance = endurance;
        this.acceleration = acceleration;
        this.maneuverability = maneuverability;
    }

    public int getEndurance() {
        return endurance;
    }

    @Nonnull
    public Acceleration getAcceleration() {
        return acceleration;
    }

    public int getManeuverability() {
        return maneuverability;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final MissileMotor that = (MissileMotor) o;

        return new EqualsBuilder().append(endurance, that.endurance).append(maneuverability, that.maneuverability).append(acceleration, that.acceleration).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(endurance).append(acceleration).append(maneuverability).toHashCode();
    }
}
