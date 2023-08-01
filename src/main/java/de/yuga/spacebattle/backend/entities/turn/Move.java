package de.yuga.spacebattle.backend.entities.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.misc.HasOwner;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "Move.getAll", query = "SELECT p FROM Move p")
})
@Entity
@Table(name = "move")
@Check(constraints = "xCoordinateOrigin != xCoordinateDestination AND yCoordinateOrigin != yCoordinateDestination")
@AttributeOverride(name = "id", column = @Column(name = "idMove"))
public class Move extends AbstractEntityKey implements HasOwner {

    @Nonnull
    @NotNull
    @OneToOne
    @JoinColumn(name = "idUser", updatable = false)
    private Owner owner;

    @Nonnull
    @NotNull
    @OneToOne
    @JoinColumn(name = "idFleet", updatable = false)
    private Fleet fleet;

    @Nonnull
    @NotNull
    @Embedded
    @AttributeOverride(name = "orbit.xCoordinate", column = @Column(name = "xCoordinateOrigin"))
    @AttributeOverride(name = "orbit.yCoordinate", column = @Column(name = "yCoordinateOrigin"))
    @AssociationOverride(name = "system", joinColumns = @JoinColumn(name = "idStarSystemOrigin"))
    private FleetOrbit originOrbit;

    @Nonnull
    @NotNull
    @Embedded
    @AttributeOverride(name = "orbit.xCoordinate", column = @Column(name = "xCoordinateDestination"))
    @AttributeOverride(name = "orbit.yCoordinate", column = @Column(name = "yCoordinateDestination"))
    @AssociationOverride(name = "system", joinColumns = @JoinColumn(name = "idStarSystemDestination"))
    private FleetOrbit destinationOrbit;

    /**
     * Principle: Countdown to zero -> job done.
     */
    private int moveDoneAtZero;

    /**
     * The original duration without modification;
     */
    @Column(updatable = false)
    private int originalDuration;

    public Move() {
    }

    public Move(@Nonnull final Fleet fleet,
                @Nonnull final FleetOrbit destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkState(fleet.getOrbit() != null, "The fleet must have an orbit currently");

        this.owner = fleet.getOwner();
        this.fleet = fleet;
        final FleetOrbit orbit = fleet.getOrbit();
        this.originOrbit = new FleetOrbit(orbit);
        this.destinationOrbit = destination;

        final boolean ftlCapable = fleet.isFTLCapable();
        final StarSystem originSystem = orbit.getSystem();
        final StarSystem destinationSystem = destination.getSystem();

        if (!StarSystem.equalsAtMap(originSystem, destinationSystem)) {
            // can not move
            this.moveDoneAtZero = -1;
        } else {
            this.moveDoneAtZero = DistanceCalculator.calculateTimeToTravel(fleet, destination);
        }
        this.originalDuration = this.moveDoneAtZero;
    }

    public Move(@Nonnull final Fleet fleet,
                @Nonnull final FleetOrbit destination,
                final int calculateTimeToTravel,
                final int originalDuration) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkState(fleet.getOrbit() != null, "The fleet must have an orbit currently");

        this.owner = fleet.getOwner();
        this.fleet = fleet;
        this.originOrbit = new FleetOrbit(fleet.getOrbit());
        this.destinationOrbit = destination;
        this.moveDoneAtZero = calculateTimeToTravel;
        this.originalDuration = originalDuration;
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

    public void setOwner(@Nonnull final Owner owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.owner = owner;
    }

    @Nonnull
    public Fleet getFleet() {
        return fleet;
    }

    public void setFleet(@Nonnull final Fleet fleet) {
        this.fleet = fleet;
    }

    @Nonnull
    public FleetOrbit getOriginOrbit() {
        return originOrbit;
    }

    @Nonnull
    public FleetOrbit getDestinationOrbit() {
        return destinationOrbit;
    }

    public int getMoveDoneAtZero() {
        return moveDoneAtZero;
    }

    public void setMoveDoneAtZero(final int moveDoneAtZero) {
        if (moveDoneAtZero >= this.moveDoneAtZero) {
            throw new NotifyWebUserException("You cannot increase the traffic time until you have warp scrambler");
        }
        this.moveDoneAtZero = moveDoneAtZero;
    }

    public int getOriginalDuration() {
        return originalDuration;
    }

    /**
     * Checks if this move is between the stars.
     *
     * @return <code>true</code> if this move is between stars, <code>false</code> otherwise
     */
    public boolean isInterstellarTravel() {
        return originOrbit.getSystem() == null || destinationOrbit.getSystem() == null || !originOrbit.getSystem().equals(destinationOrbit.getSystem());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;

        Move move = (Move) o;

        if (!fleet.equals(move.fleet)) return false;
        if (!originOrbit.equals(move.originOrbit)) return false;
        return destinationOrbit.equals(move.destinationOrbit);
    }

    @Override
    public int hashCode() {
        int result = fleet.hashCode();
        result = 31 * result + originOrbit.hashCode();
        result = 31 * result + destinationOrbit.hashCode();
        return result;
    }
}
