package de.yuga.spacebattle.backend.calculator.geometry;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;

import javax.annotation.Nonnull;

public class DynamicInfo implements Cloneable {

    @Nonnull
    private Acceleration acceleration;

    @Nonnull
    private Velocity velocity;

    @Nonnull
    private Distance distance;

    public DynamicInfo(@Nonnull final Acceleration acceleration,
                       @Nonnull final Velocity velocity,
                       @Nonnull final Distance distance) {
        this.acceleration = Preconditions.checkNotNull(acceleration, "acceleration must not be empty").clone();
        this.velocity = Preconditions.checkNotNull(velocity, "velocity must not be empty").clone();
        this.distance = Preconditions.checkNotNull(distance, "distance must not be empty").clone();
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
    public Distance getDistance() {
        return distance.clone();
    }

    @Override
    public DynamicInfo clone() {
        try {
            //noinspection UnnecessaryLocalVariable
            final DynamicInfo clone = (DynamicInfo) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Nonnull
    public DynamicInfo with(@Nonnull final Acceleration acceleration) {
        this.acceleration = Preconditions.checkNotNull(acceleration, "acceleration must not be empty").clone();

        return this;
    }

    @Nonnull
    public DynamicInfo with(@Nonnull final Velocity velocity) {
        this.velocity = Preconditions.checkNotNull(velocity, "velocity must not be empty").clone();

        return this;
    }

    @Nonnull
    public DynamicInfo with(@Nonnull final Distance distance) {
        this.distance = Preconditions.checkNotNull(distance, "distance must not be empty").clone();

        return this;
    }

    @Override
    public String toString() {
        return acceleration + " " + velocity;
    }
}
