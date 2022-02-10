package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.BigInteger;

@Embeddable
public class Orbit implements Comparable<Orbit>, Cloneable {

    /**
     * Just to position the system.
     */
    @Column(columnDefinition = "decimal(19, 0)")
    private BigInteger xCoordinate;

    /**
     * Just to position the system.
     */
    @Column(columnDefinition = "decimal(19, 0)")
    private BigInteger yCoordinate;

    public Orbit() {
    }

    public Orbit(final BigInteger xCoordinate, final BigInteger yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public Orbit(final int xCoordinate, final int yCoordinate) {
        this.xCoordinate = BigInteger.valueOf(xCoordinate);
        this.yCoordinate = BigInteger.valueOf(yCoordinate);
    }

    public Orbit(@Nonnull final de.yuga.spacebattle.rest.dto.orbitals.Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.xCoordinate = orbit.getxCoordinate();
        this.yCoordinate = orbit.getyCoordinate();
    }

    public Orbit(@Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.xCoordinate = orbit.getXCoordinate();
        this.yCoordinate = orbit.getYCoordinate();
    }

    /**
     * For convenience. BigDecimal will be scaled down.
     */
    public Orbit(@Nonnull final BigDecimal xCoordinate, @Nonnull final BigDecimal yCoordinate) {
        Preconditions.checkNotNull(xCoordinate, "xCoordinate shouldn't be null!");
        Preconditions.checkNotNull(yCoordinate, "yCoordinate shouldn't be null!");

        this.xCoordinate = xCoordinate.toBigInteger();
        this.yCoordinate = yCoordinate.toBigInteger();
    }

    public BigInteger getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(BigInteger xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public BigInteger getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(BigInteger yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    /**
     * Returns the distance in range units between the params orbit and this.
     *
     * @param targetOrbit the targets orbit
     * @return the distance in range units
     */
    @Nonnull
    public BigDecimal getDistance(@Nonnull final Orbit targetOrbit) {
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
                      @Nonnull final BigDecimal distance,
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
        final BigDecimal xDecimal = new BigDecimal(xCoordinate);
        final BigDecimal yDecimal = new BigDecimal(yCoordinate);

        final BigDecimal distanceScalar = i.multiply(distance);
        final BigDecimal xDirection = xDecimal.subtract(new BigDecimal(direction.getXCoordinate()));
        final BigDecimal yDirection = yDecimal.subtract(new BigDecimal(direction.getYCoordinate()));
        final BigDecimal unitDirection = DistanceCalculator.getDistance(xDirection, yDirection);
        final BigDecimal einheitsX = xDirection.divide(unitDirection, DistanceCalculator.MATH_CONTEXT_MORE_PRECISION);
        final BigDecimal einheitsY = yDirection.divide(unitDirection, DistanceCalculator.MATH_CONTEXT_MORE_PRECISION);

        final BigDecimal x = xDecimal.add(distanceScalar.multiply(einheitsX));
        final BigDecimal y = yDecimal.add(distanceScalar.multiply(einheitsY));
        return new Orbit(x, y);
    }

    /**
     * Sets the coordinates to the new ones, for convenience.
     *
     * @param xCoordinate the new x
     * @param yCoordinate the new y
     */
    public void moveTo(final BigInteger xCoordinate, final BigInteger yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
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
        final BigInteger o1X = this.getXCoordinate();
        final BigInteger o2X = o.getXCoordinate();

        final BigInteger o1Y = this.getYCoordinate();
        final BigInteger o2Y = o.getYCoordinate();

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
        return new Orbit(BigInteger.ZERO, BigInteger.ZERO);
    }

    @Override
    public String toString() {
        return " x: " + DistanceCalculator.getDistanceAsStringWithUnit(xCoordinate) + ", y: " + DistanceCalculator.getDistanceAsStringWithUnit(yCoordinate);
    }

    @Override
    public Orbit clone() {
        try {
            final Orbit clone = (Orbit) super.clone();
            clone.xCoordinate = new BigInteger(xCoordinate.toString());
            clone.yCoordinate = new BigInteger(yCoordinate.toString());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
