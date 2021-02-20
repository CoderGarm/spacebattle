package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.MathContext;

@Embeddable
public class Orbit {

    private final static Logger LOGGER = LoggerFactory.getLogger(Orbit.class);

    /**
     * Just to position the system.
     */
    private int xCoordinate;

    /**
     * Just to position the system.
     */
    private int yCoordinate;

    public Orbit() {
    }

    public Orbit(final int xCoordinate, final int yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    /**
     * Creates an orbit at the base of the star system.
     *
     * @param xCoordinate the X of the sun's orbital
     * @param yCoordinate the Y of the sun's orbital
     */
    public Orbit(@Nonnull final StarSystem system, final int xCoordinate, final int yCoordinate) {
        Preconditions.checkNotNull(system, "system shouldn't be null!");

        Orbit orbit = system.getOrbit();
        this.xCoordinate = orbit.getXCoordinate() + xCoordinate;
        this.yCoordinate = orbit.getYCoordinate() + yCoordinate;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    /**
     * Returns the distance in range units between the params orbit and this.
     *
     * @param targetOrbit the targets orbit
     * @return the distance in range units
     */
    public BigDecimal getDistance(@Nonnull final Orbit targetOrbit) {
        Preconditions.checkNotNull(targetOrbit, "targetOrbit shouldn't be null!");

        try {
            int diffX = targetOrbit.getXCoordinate() - this.xCoordinate;
            int diffY = targetOrbit.getYCoordinate() - this.yCoordinate;

            BigDecimal diffXb = new BigDecimal(diffX);
            BigDecimal diffYb = new BigDecimal(diffY);

            BigDecimal diffXbSQRT = diffXb.multiply(diffXb);
            BigDecimal diffYbSQRT = diffYb.multiply(diffYb);
            return diffXbSQRT.add(diffYbSQRT).sqrt(MathContext.DECIMAL128);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());
            throw new NotifySBUserException("no distance calculatable");
        }
    }
}
