package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

import static de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator.MC_HU;

@Embeddable
public class Orbit implements Comparable<Orbit>, Cloneable {

    /**
     * Just to position the system.
     */
    @Convert(converter = DistanceConverter.class)
    private Distance xCoordinate;

    /**
     * Just to position the system.
     */
    @Convert(converter = DistanceConverter.class)
    private Distance yCoordinate;

    public Orbit() {
    }

    public Orbit(final Distance xCoordinate, final Distance yCoordinate) {
        this.xCoordinate = xCoordinate.clone();
        this.yCoordinate = yCoordinate.clone();
    }

    public Orbit(@Nonnull final de.yuga.spacebattle.rest.dto.orbitals.Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.xCoordinate = orbit.getXCoordinate();
        this.yCoordinate = orbit.getYCoordinate();
    }

    public Orbit(@Nonnull final BigDecimal xCoordinate, @Nonnull final BigDecimal yCoordinate, @Nonnull final EDistanceMetric distanceMetric) {
        Preconditions.checkNotNull(xCoordinate, "xCoordinate shouldn't be null!");
        Preconditions.checkNotNull(yCoordinate, "yCoordinate shouldn't be null!");
        Preconditions.checkNotNull(distanceMetric, "distanceMetric shouldn't be null!");

        this.xCoordinate = new Distance(xCoordinate, distanceMetric);
        this.yCoordinate = new Distance(yCoordinate, distanceMetric);
    }

    public Distance getXCoordinate() {
        return xCoordinate;
    }

    public Distance getYCoordinate() {
        return yCoordinate;
    }

    /**
     * Returns the distance in range units between the params orbit and this.
     *
     * @param targetOrbit the targets orbit
     * @return the distance in range units
     */
    @Nonnull
    public Distance getDistance(@Nonnull final Orbit targetOrbit) {
        Preconditions.checkNotNull(targetOrbit, "targetOrbit shouldn't be null!");

        return DistanceCalculator.getOrbitalDistance(this, targetOrbit);
    }

    /**
     * Moves this position to the direction for the given distance by the given movement plan.
     *
     * @param movementType if the movement is towards or away from the direction
     * @param distance     the distance which this orbit will be moved
     * @param direction    the direction
     * @return the resulting position
     */
    public Orbit move(@Nonnull final EMovementType movementType,
                      @Nonnull final Distance distance,
                      @Nonnull final Orbit direction) {
        Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");
        Preconditions.checkNotNull(direction, "direction shouldn't be null!");

        BigDecimal i = BigDecimal.ONE;
        switch (movementType) {
            case REDUCE_DISTANCE:
            case EVASION_MOVEMENT:
                i = i.negate();
                break;
            case INCREASE_DISTANCE:
                // noop
                break;
            default:
            case HOLD_DISTANCE:
                // todo LOGGER.info("This should be implemented if we are in the space without any acting force by Newton III.");
                return this.clone();
        }

        final EDistanceMetric distanceMetric = getXCoordinate().getDistanceMetric();
        final BigDecimal distanceScalar = i.multiply(distance.getCoordinateInMetric(distanceMetric));
        final Distance xC = xCoordinate.subtract(direction.getXCoordinate());
        final Distance yC = yCoordinate.subtract(direction.getYCoordinate());
        final BigDecimal uC = DistanceCalculator.getDistance(xC.getCoordinate(), yC.getCoordinate());
        final BigDecimal eXC = xC.getCoordinate().divide(uC, MC_HU);
        final BigDecimal eYC = yC.getCoordinate().divide(uC, MC_HU);
        final Distance x1 = xCoordinate.add(new Distance(distanceScalar.multiply(eXC), distanceMetric));
        final Distance y1 = yCoordinate.add(new Distance(distanceScalar.multiply(eYC), distanceMetric));
        return new Orbit(x1, y1);
    }

    /**
     * Returns the resulting position by the base orbit, the direction and the distance.
     *
     * @param distance  the distance
     * @param direction the direction
     * @return the resulting position
     */
    @Nonnull
    public Orbit getDestinationBy(@Nonnull final Distance distance, @Nonnull final Direction direction) {
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");
        Preconditions.checkNotNull(direction, "direction shouldn't be null!");

        if (direction.isNullDirection()) {
            return this.clone();
        }

        final BigDecimal distanceScalar = distance.getCoordinate();
        final EDistanceMetric distanceMetric = distance.getDistanceMetric();
        final BigDecimal xDirection = direction.getXCoordinate();
        final BigDecimal yDirection = direction.getYCoordinate();

        final BigDecimal x = xDirection.multiply(distanceScalar, MC_HU);
        final BigDecimal y = yDirection.multiply(distanceScalar, MC_HU);

        final BigDecimal newX = this.xCoordinate.getCoordinateInMetric(distanceMetric).add(x);
        final BigDecimal newY = this.yCoordinate.getCoordinateInMetric(distanceMetric).add(y);

        return new Orbit(newX, newY, distanceMetric);
    }

    /**
     * Sets the coordinates to the new ones, for convenience.
     *
     * @param xCoordinate the new x
     * @param yCoordinate the new y
     */
    public void moveTo(final Distance xCoordinate, final Distance yCoordinate) {
        this.xCoordinate = xCoordinate.clone();
        this.yCoordinate = yCoordinate.clone();
    }

    /**
     * Sets the coordinates to the new ones, for convenience.
     *
     * @param destination the destination
     */
    public void moveTo(@Nonnull final Orbit destination) {
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        moveTo(destination.getXCoordinate(), destination.getYCoordinate());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof Orbit)) return false;

        final Orbit that = (Orbit) o;

        return new EqualsBuilder().append(xCoordinate, that.xCoordinate).append(yCoordinate, that.yCoordinate).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(xCoordinate).append(yCoordinate).toHashCode();
    }

    @Override
    public int compareTo(@Nonnull final Orbit o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        if (this.equals(o)) {
            // should not happen while orbits must be unique
            return 0;
        }
        /*
        a negative integer, zero, or a positive integer as
        the first argument is less than, equal to, or greater than the second.
        */
        final Distance o1X = this.getXCoordinate();
        final Distance o2X = o.getXCoordinate();

        final Distance o1Y = this.getYCoordinate();
        final Distance o2Y = o.getYCoordinate();

        if (o1X.compareTo(o2X) < 0) {
            return -1;
        } else if (o1X.compareTo(o2X) > 0) {
            return 1;
        }

        if (o1Y.compareTo(o2Y) < 0) {
            return -1;
        }
        return 1;
    }

    public static Orbit getCenterOrbit() {
        return new Orbit(Distance.ZERO, Distance.ZERO);
    }

    @Override
    public String toString() {
        return " x: " + xCoordinate.toString() + ", y: " + yCoordinate.toString();
    }

    @Override
    public Orbit clone() {
        try {
            final Orbit clone = (Orbit) super.clone();
            clone.xCoordinate = xCoordinate.clone();
            clone.yCoordinate = yCoordinate.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Nonnull
    public Orbit subtract(@Nonnull final Orbit subtrahend) {
        Preconditions.checkNotNull(subtrahend, "subtrahend shouldn't be null!");

        final Distance newX = xCoordinate.subtract(subtrahend.getXCoordinate());
        final Distance newY = yCoordinate.subtract(subtrahend.getYCoordinate());
        return new Orbit(newX, newY);
    }

    @Nonnull
    public Orbit divide(final int dividend) {
        final Distance newX = xCoordinate.divide(new Distance(dividend, xCoordinate.getDistanceMetric()));
        final Distance newY = yCoordinate.divide(new Distance(dividend, yCoordinate.getDistanceMetric()));
        return new Orbit(newX, newY);
    }
}
