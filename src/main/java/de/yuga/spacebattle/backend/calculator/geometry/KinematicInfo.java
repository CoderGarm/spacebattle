package de.yuga.spacebattle.backend.calculator.geometry;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.EModuleType;

import javax.annotation.Nonnull;

public class KinematicInfo implements Cloneable {

    @Nonnull
    private final Acceleration acceleration;

    @Nonnull
    private final Velocity velocity;

    @Nonnull
    private final Direction direction;

    @Nonnull
    private final Orbit position;

    public KinematicInfo(@Nonnull final Acceleration acceleration,
                         @Nonnull final Velocity velocity,
                         @Nonnull final Direction direction,
                         @Nonnull final Orbit position) {
        this.acceleration = Preconditions.checkNotNull(acceleration, "acceleration must not be empty");
        this.velocity = Preconditions.checkNotNull(velocity, "velocity must not be empty");
        this.direction = Preconditions.checkNotNull(direction, "direction must not be empty");
        this.position = Preconditions.checkNotNull(position, "position must not be empty");
    }

    public static KinematicInfo getFrom(@Nonnull final FleetRoundState roundState) {
        Preconditions.checkNotNull(roundState, "roundState must not be empty");

        final Acceleration acceleration = roundState.getAccelerationFor(EModuleType.PROPULSION);
        final Velocity velocity = roundState.getVelocity();
        final Direction direction = roundState.getDirection();
        final Orbit position = roundState.getPosition();
        return new KinematicInfo(acceleration, velocity, direction, position);
    }

    @Nonnull
    public Acceleration getAcceleration() {
        return acceleration.clone();
    }

    @Nonnull
    public Velocity getVelocity() {
        return velocity.clone();
    }

    @Nonnull
    public Direction getDirection() {
        return direction.clone();
    }

    @Nonnull
    public Orbit getPosition() {
        return position.clone();
    }

    @Override
    public KinematicInfo clone() {
        try {
            //noinspection UnnecessaryLocalVariable
            final KinematicInfo clone = (KinematicInfo) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
