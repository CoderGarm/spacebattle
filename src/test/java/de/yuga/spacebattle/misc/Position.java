package de.yuga.spacebattle.misc;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;

import java.math.BigDecimal;

public class Position {

    private int x;
    private int y;

    @JsonProperty
    private int z = 0;

    public Position(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Position(final double x, final double y) {
        this.x = (int) x;
        this.y = (int) y;
    }


    public void invertYAxis() {
        y = y * -1;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public void setY(final int y) {
        this.y = y;
    }

    public int getDistance(final Position that) {
        final BigDecimal xThis = new BigDecimal(this.x);
        final BigDecimal yThis = new BigDecimal(this.y);

        final BigDecimal xThat = new BigDecimal(that.x);
        final BigDecimal yThat = new BigDecimal(that.y);
        return DistanceCalculator.getDistance(xThat.subtract(xThis), yThat.subtract(yThis)).intValue();
    }

    public double getR() {
        return Math.sqrt(((double) x * (double) x) + ((double) y * (double) y));
    }

    public double getTheta() {
        return flippedAtan2(y, x);
    }

    public double bearingTo(Position target) {
        return flippedAtan2(target.getY() - y, target.getX() - x);
    }

    public static Position fromPolar(double magnitude, double angle) {
        double flippedAngle = flipAngle(angle);
        return new Position(magnitude * Math.cos(flippedAngle), magnitude * Math.sin(flippedAngle));
    }

    // flip the angle between 0 is the East + counter-clockwise and 0 is the North + clockwise
    // and vice versa
    private static double flipAngle(double angle) {
        return Math.PI / 2 - angle;
    }

    private static double flippedAtan2(double y, double x) {
        double angle = Math.atan2(y, x);
        double flippedAngle = flipAngle(angle);
        //  additionally put the angle into [0; 2*Pi) range from its [-pi; +pi] range
        return (flippedAngle >= 0) ? flippedAngle : flippedAngle + 2 * Math.PI;
    }

    @Override
    public String toString() {
        return "x " + x + ", y " + y;
    }
}
