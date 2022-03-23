package de.yuga.spacebattle.backend.dto.physics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator.MC;

/**
 * The direction represents the normed vectorial direction between two points.
 */
public class Direction implements Cloneable {

    public static final Direction ZERO = new Direction();

    /**
     * The normed y coordinate of the direction vector.
     */
    @Nonnull
    private BigDecimal xCoordinate;

    /**
     * The normed y coordinate of the direction vector.
     */
    @Nonnull
    private BigDecimal yCoordinate;

    public Direction(@Nonnull final Orbit origin, @Nonnull final Orbit destination) {
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        final Orbit result = destination.subtract(origin);
        final BigDecimal x = result.getXCoordinate().getCoordinateInMetric(EDistanceMetric.M);
        final BigDecimal y = result.getYCoordinate().getCoordinateInMetric(EDistanceMetric.M);
        final BigDecimal normResult = calculateNormDivisor(x, y);
        this.xCoordinate = normCoordinate(x, normResult);
        this.yCoordinate = normCoordinate(y, normResult);
    }

    public Direction(@Nonnull final BigDecimal xCoordinate, @Nonnull final BigDecimal yCoordinate) {
        Preconditions.checkNotNull(xCoordinate, "xCoordinate shouldn't be null!");
        Preconditions.checkNotNull(yCoordinate, "yCoordinate shouldn't be null!");

        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    @Nonnull
    private BigDecimal normCoordinate(@Nonnull final BigDecimal val, @Nonnull final BigDecimal other) {
        Preconditions.checkNotNull(val, "val shouldn't be null!");
        Preconditions.checkNotNull(other, "other shouldn't be null!");

        BigDecimal xO = BigDecimal.ZERO;
        if (isDifferentFromZero(val) && isDifferentFromZero(other)) {
            xO = val.divide(other, MC);
        }
        return xO;
    }

    private boolean isDifferentFromZero(@Nonnull final BigDecimal val) {
        Preconditions.checkNotNull(val, "val shouldn't be null!");

        if (val.compareTo(BigDecimal.ZERO) != 0) {
            return true;
        }
        return false;
    }

    private Direction() {
        this.xCoordinate = BigDecimal.ZERO;
        this.yCoordinate = BigDecimal.ZERO;
    }

    @Nonnull
    public BigDecimal getXCoordinate() {
        return xCoordinate;
    }

    @Nonnull
    public BigDecimal getYCoordinate() {
        return yCoordinate;
    }

    @Override
    public String toString() {
        return " x: " + xCoordinate + ", y: " + yCoordinate;
    }

    /**
     * Calculates the divisor which must be used to norm the vectorial coordinates.
     *
     * @param x one coordinate
     * @param y the other coordinate
     * @return the divisor to norm the coordinates
     */
    @Nonnull
    private BigDecimal calculateNormDivisor(@Nonnull final BigDecimal x, @Nonnull final BigDecimal y) {
        Preconditions.checkNotNull(x, "x shouldn't be null!");
        Preconditions.checkNotNull(y, "y shouldn't be null!");

        return x.pow(2).add(y.pow(2)).sqrt(MC);
    }

    /**
     * Calculates the alignment of two directions via vector calculation.<br>
     * The cosines of the angle between both directions will be returned.
     *
     * @param that the other direction
     * @return something between -1 and 1 where -1 means the opposite direction and 1 means a full alignment of both directions
     */
    @Nonnull
    public BigDecimal getAlignmentFactor(@Nonnull final Direction that) {
        Preconditions.checkNotNull(that, "that shouldn't be null!");

        if (isNullDirection() || that.isNullDirection()) {
            return BigDecimal.ONE;
        }

        // calculate angle between both direction vectors
        final double angle = Math.abs(getAngleBetween(that)) / 2;
        // returning 1 for nearly zero difference and -1 for the full difference at 180°
        if (isBiggerButSmaller(angle, 0D, 45D)) {
            return BigDecimal.ONE;
        }
        if (isBiggerButSmaller(angle, 45D, 90D)) {
            return BigDecimal.valueOf(0.75);
        }
        if (isBiggerButSmaller(angle, 90D, 135D)) {
            return BigDecimal.valueOf(-0.75);
        }
        return BigDecimal.ONE.negate();
    }

    private boolean isBiggerButSmaller(@Nonnull final Double a, @Nonnull final Double b, @Nonnull final Double c) {
        Preconditions.checkNotNull(a, "a shouldn't be null!");
        Preconditions.checkNotNull(b, "b shouldn't be null!");
        Preconditions.checkNotNull(c, "c shouldn't be null!");

        return a.compareTo(b) >= 0 && a.compareTo(c) <= 0;
    }

    /**
     * Calculates the difference between this and that.<br>
     * Will return an angle between 0° and 360°.<br>
     * <br>
     * <a href="https://www.mathebibel.de/winkel-zwischen-zwei-vektoren#formel">Source</a>
     *
     * @param that that
     * @return the angular difference
     */
    public double getAngleBetween(@Nonnull final Direction that) {
        Preconditions.checkNotNull(that, "that shouldn't be null!");

        if (isNullDirection() || that.isNullDirection()) {
            // if one or both are "not defined" they have no clear choice, it is handled always as wildcard
            return Double.NEGATIVE_INFINITY;
        }
        // calculate scalar product
        final BigDecimal scalar = xCoordinate.multiply(that.getXCoordinate()).add(yCoordinate.multiply(that.getYCoordinate()));
        // calculate quantities of the vectors
        final BigDecimal thisQuantity = xCoordinate.pow(2).add(yCoordinate.pow(2)).sqrt(MC);
        final BigDecimal thatQuantity = that.getXCoordinate().pow(2).add(that.getYCoordinate().pow(2)).sqrt(MC);
        // calculate cosines of angle
        final BigDecimal multiply = thisQuantity.multiply(thatQuantity);
        final BigDecimal cosinesOfPhi = scalar.divide(multiply, MC);
        final double acos = Math.acos(cosinesOfPhi.doubleValue());
        return Math.toDegrees(acos);
    }

    /**
     * States whether this direction has a direction or not.
     *
     * @return <code>true</code> if this direction is the 'null direction', <code>false</code> otherwise
     */
    public boolean isNullDirection() {
        return xCoordinate.compareTo(BigDecimal.ZERO) == 0 && yCoordinate.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public Direction clone() {
        try {
            final Direction clone = (Direction) super.clone();
            this.xCoordinate = new BigDecimal(xCoordinate.toString());
            this.yCoordinate = new BigDecimal(yCoordinate.toString());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
