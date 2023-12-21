package de.yuga.spacebattle.backend.entities.turn.navigation;

import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.turn.Move;

import javax.annotation.Nonnull;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("WAYPOINT")
public class Waypoint extends FlightPlan {

    public Waypoint() {
        super();
    }

    public Waypoint(@Nonnull final Move move, @Nonnull final FleetOrbit location, final int timeAfterStart) {
        super(location, timeAfterStart);

        super.setMove(move);
    }
}
