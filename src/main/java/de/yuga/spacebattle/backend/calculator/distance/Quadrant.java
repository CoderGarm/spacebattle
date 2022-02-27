package de.yuga.spacebattle.backend.calculator.distance;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

/**
 * A quadrant enum which will transform the 'computer coordinates' to human readable quadrants.
 */
public enum Quadrant {
    /**
     * mathematical quadrants based on browser coordinates:
     * Q4, Q1
     * Q3, Q2
     * todo check  angle base
     */
    Q1(1, -1, 45),
    Q2(1, 1, 135),
    Q3(-1, 1, 225),
    Q4(-1, -1, 315);

    private final int signumX;
    private final int signumY;
    private final int phi;

    Quadrant(final int signumX, final int signumY, final int phi) {
        this.signumX = signumX;
        this.signumY = signumY;
        this.phi = phi;
    }

    public int getSignumX() {
        return signumX;
    }

    public int getSignumY() {
        return signumY;
    }

    public int getPhi() {
        return phi;
    }

    /**
     * Returns a quadrant by the signum of a coordinate set.
     *
     * @param signumX the x signum
     * @param signumY the y signum
     * @return the corresponding quadrant
     */
    @Nonnull
    public static Quadrant getBySignum(final int signumX, final int signumY) {
        return Objects.requireNonNull(Arrays.stream(Quadrant.values()).filter(q -> q.getSignumX() == signumX && q.getSignumY() == signumY).findFirst().orElse(Q1));
    }

    /**
     * Returns the quadrant by the orbit.
     *
     * @param orbit the orbit to validate
     * @return the quadrant
     */
    public static Quadrant getByOrbit(@Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        final BigDecimal xCoordinate = orbit.getXCoordinate().getCoordinate();
        final BigDecimal yCoordinate = orbit.getYCoordinate().getCoordinate();

        int signumX = xCoordinate.signum();
        int signumY = yCoordinate.signum();
        return Quadrant.getBySignum(signumX, signumY);
    }
}
