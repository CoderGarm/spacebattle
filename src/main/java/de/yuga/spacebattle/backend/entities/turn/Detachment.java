package de.yuga.spacebattle.backend.entities.turn;

import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.turn.mission.Mission;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * A warship can be part of a fleet or part of a mission or at shore leave.
 */
@Embeddable
public class Detachment {

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idFleet")
    private Fleet fleet;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idMission")
    private Mission mission;

    @Nullable
    public Fleet getFleet() {
        return fleet;
    }

    public void setFleet(@Nullable final Fleet fleet) {
        if (fleet != null) {
            this.mission = null;
        }
        this.fleet = fleet;
    }

    @Nullable
    public Mission getMission() {
        return mission;
    }

    public void setMission(@Nullable final Mission mission) {
        if (mission != null) {
            this.fleet = null;
        }
        this.mission = mission;
    }

    public void sendToPool() {
        this.setMission(null);
        this.setFleet(null);
    }

    public boolean isPoolShip() {
        return mission == null && fleet == null;
    }
}
