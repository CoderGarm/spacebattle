package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EDistanceMetric;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

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

        this.xCoordinate = orbit.getxCoordinate();
        this.yCoordinate = orbit.getyCoordinate();
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
        final BigDecimal eXC = xC.getCoordinate().divide(uC, DistanceCalculator.MATH_CONTEXT_MORE_PRECISION);
        final BigDecimal eYC = yC.getCoordinate().divide(uC, DistanceCalculator.MATH_CONTEXT_MORE_PRECISION);
        final Distance x1 = xCoordinate.add(new Distance(distanceScalar.multiply(eXC), distanceMetric));
        final Distance y1 = yCoordinate.add(new Distance(distanceScalar.multiply(eYC), distanceMetric));
        return new Orbit(x1, y1);
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

        final Orbit orbit = (Orbit) o;

        return new EqualsBuilder().append(xCoordinate, orbit.xCoordinate).append(yCoordinate, orbit.yCoordinate).isEquals();
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
}
