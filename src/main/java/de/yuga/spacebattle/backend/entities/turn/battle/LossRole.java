package de.yuga.spacebattle.backend.entities.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Every loss role remembers to a defeated and destroyed war ship.
 */
@Embeddable
public class LossRole {

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idOwner")
    private User owner;

    /**
     * Just the name of the lost ship.<br>
     * The war ship itself will be deleted. todo
     */
    @Nonnull
    private String warShipName;

    /**
     * The type of the loss.<br>
     * The ship class itself will never be removed but flagged as deleted.
     */
    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idShipClass")
    private ShipClass shipClass;

    public LossRole() {
    }

    public LossRole(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip shouldn't be null!");

        this.owner = warShip.getShipClass().getOwner();
        this.warShipName = warShip.getName();
        this.shipClass = warShip.getShipClass();
    }

    @Nonnull
    public User getOwner() {
        return owner;
    }

    @Nonnull
    public String getWarShipName() {
        return warShipName;
    }

    @Nonnull
    public ShipClass getShipClass() {
        return shipClass;
    }
}
