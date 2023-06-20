package de.yuga.spacebattle.backend.entities.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.HasOwner;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Every loss role remembers to a defeated and destroyed war ship.
 */
@Embeddable
public class LossRole implements HasOwner {

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idOwner")
    private Owner owner;

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idFleet")
    private Fleet fleet;

    /**
     * Just the name of the lost ship.<br>
     * The war ship itself will be marked as deleted.
     */
    @Nonnull
    private String warShipName;

    private int idWarship;

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
        this.fleet = warShip.getFleet();
        this.warShipName = warShip.getName();
        this.idWarship = warShip.getId();
        this.shipClass = warShip.getShipClass();
    }


    @Nonnull
    @Override
    public Owner getOwner() {
        return owner;
    }

    @Nullable
    @Override
    public User getHumanOwner() {
        if (!(owner instanceof User)) {
            return null;
        }
        return (User) owner;
    }

    @Nullable
    @Override
    public NonPlayerCharacter getNpcOwner() {
        if (!(owner instanceof NonPlayerCharacter)) {
            return null;
        }
        return (NonPlayerCharacter) owner;
    }

    @Nonnull
    public Fleet getFleet() {
        return fleet;
    }

    @Nonnull
    public String getWarShipName() {
        return warShipName;
    }

    public int getIdWarship() {
        return idWarship;
    }

    @Nonnull
    public ShipClass getShipClass() {
        return shipClass;
    }
}
