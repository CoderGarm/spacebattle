package de.yuga.spacebattle.entities.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.entities.AbstractEntityKey;
import de.yuga.spacebattle.entities.account.User;
import de.yuga.spacebattle.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.entities.orbitals.Planet;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "Move.getAll", query = "SELECT p FROM Move p")
})
@Entity
@Table(name = "move")
@AttributeOverride(name = "id", column = @Column(name = "idMove"))
public class Move extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @OneToOne
    @JoinColumn(name = "idUser", updatable = false)
    private User owner;

    @Nonnull
    @NotNull
    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idFleet", updatable = false)
    private Fleet fleet;

    @Nonnull
    @NotNull
    @Embedded
    @AssociationOverrides({
            @AssociationOverride(name = "system", joinColumns = @JoinColumn(name = "startIdStarsystem")),
            @AssociationOverride(name = "planet", joinColumns = @JoinColumn(name = "startIdPlanet"))
    })
    private FleetOrbit startOrbit;

    @Nonnull
    @NotNull
    @Embedded
    @AssociationOverrides({
            @AssociationOverride(name = "system", joinColumns = @JoinColumn(name = "targetIdStarsystem")),
            @AssociationOverride(name = "planet", joinColumns = @JoinColumn(name = "targetIdPlanet"))
    })
    private FleetOrbit targetOrbit;

    /**
     * Principle: Countdown to zero -> job done.
     */
    private int moveDoneAtZero;

    public Move() {
    }

    public Move(@Nonnull final Fleet fleet,
                @Nonnull final Planet target,
                final int moveDoneAtZero) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        this.owner = fleet.getOwner();
        this.fleet = fleet;
        this.startOrbit = fleet.getOrbit();
        this.targetOrbit = new FleetOrbit(target.getSystem(), target);
        this.moveDoneAtZero = moveDoneAtZero;
    }

    @Nonnull
    public User getOwner() {
        return owner;
    }

    public void setOwner(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.owner = owner;
    }

    @Nonnull
    public Fleet getFleet() {
        return fleet;
    }

    public void setFleet(@Nonnull Fleet fleet) {
        this.fleet = fleet;
    }

    @Nonnull
    public FleetOrbit getStartOrbit() {
        return startOrbit;
    }

    public void setStartOrbit(@Nonnull final FleetOrbit startOrbit) {
        this.startOrbit = startOrbit;
    }

    @Nonnull
    public FleetOrbit getTargetOrbit() {
        return targetOrbit;
    }

    public void setTargetOrbit(@Nonnull final FleetOrbit targetOrbit) {
        this.targetOrbit = targetOrbit;
    }

    public int getMoveDoneAtZero() {
        return moveDoneAtZero;
    }

    public void setMoveDoneAtZero(final int moveDoneAtZero) {
        if (moveDoneAtZero >= this.moveDoneAtZero) {
            throw new NotifySBUserException("You cannot increase the traffic time until you have warp scrambler");
        }
        this.moveDoneAtZero = moveDoneAtZero;
    }
}
