package de.yuga.spacebattle.backend.entities.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.Completable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "transportJob")
@AttributeOverride(name = "id", column = @Column(name = "idTransportJob"))
public class TransportJob extends Completable {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idOwner", updatable = false)
    private Owner owner;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idOrigin", updatable = false)
    private Planet origin;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idDestination", updatable = false)
    private Planet destination;

    @Nonnull
    @NotNull
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "idTransportJob")
    private final Set<WarShip> ships = new HashSet<>();

    @Nonnull
    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "transferredShips",
            joinColumns = @JoinColumn(name = "idTransportJob"),
            inverseJoinColumns = @JoinColumn(name = "idWarship"),
            uniqueConstraints = @UniqueConstraint(name = "transferredShips_UC", columnNames = {"idWarship", "idTransportJob"}))
    private final Set<WarShip> transferredShips = new HashSet<>();


    public TransportJob() {
    }

    public TransportJob(@Nonnull final Planet destination, @Nonnull final WarShip warship) {
        this.destination = Preconditions.checkNotNull(destination, "destination must not be empty");
        final Planet reserveStation = Preconditions.checkNotNull(warship, "warship must not be empty").getMothball();
        this.origin = Objects.requireNonNull(reserveStation);
        this.owner = warship.getShipClass().getOwner();
        this.ticksLeft = 1;
    }

    @Nonnull
    public Owner getOwner() {
        return owner;
    }

    @Nonnull
    public Planet getOrigin() {
        return origin;
    }

    @Nonnull
    public Planet getDestination() {
        return destination;
    }

    public void setDestination(@Nonnull final Planet destination) {
        this.destination = destination;
    }

    @Nonnull
    public Set<WarShip> getShips() {
        return ships;
    }

    @Nonnull
    public Set<WarShip> getTransferredShips() {
        return transferredShips;
    }

    @Override
    public void setFinished(@Nonnull final Tick finishedAt) {
        Preconditions.checkNotNull(finishedAt, "finishedAt must not be empty");

        transferredShips.addAll(ships);
        super.setFinished(finishedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransportJob)) return false;

        TransportJob job = (TransportJob) o;

        return id == job.id;
    }

    @Override
    public int hashCode() {
        return id * 31;
    }
}
