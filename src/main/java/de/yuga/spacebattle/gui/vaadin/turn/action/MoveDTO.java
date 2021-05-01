package de.yuga.spacebattle.gui.vaadin.turn.action;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;

/**
 * Holds a fleet and it's target.
 */
public class MoveDTO {

    @Nonnull
    private final Fleet fleet;

    @Nonnull
    private final Planet target;

    /**
     * If this dto represents a moving fleet.
     */
    private final boolean inMotion;

    public MoveDTO(@Nonnull final Fleet fleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        this.fleet = fleet;
        this.target = target;
        this.inMotion = fleet.getMove() != null;
    }

    /**
     * Returns the string representation of the duration of this move.
     *
     * @return the string representation
     */
    @Nonnull
    public String getTimeToTravel() {
        final int timeToTravel = DistanceCalculator.calculateTimeToTravel(fleet, target);
        return "Time to travel: " + timeToTravel + " Tick";
    }

    /**
     * Returns the target of the move.
     *
     * @return the target
     */
    @Nonnull
    public Planet getTarget() {
        return target;
    }

    public boolean isInMotion() {
        return inMotion;
    }
}
