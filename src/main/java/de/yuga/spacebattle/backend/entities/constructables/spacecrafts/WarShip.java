package de.yuga.spacebattle.backend.entities.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.Operationable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.turn.mission.Mission;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "WarShip.getAll", query = "SELECT a FROM WarShip a WHERE a.isDeleted = false")
})
@Entity
@Table(name = "warShip")
@AttributeOverride(name = "id", column = @Column(name = "idWarShip"))
@Check(constraints = "idFleet is not null OR idMission IS NOT NULL")
public class WarShip extends Operationable {

    @Nonnull
    @NotNull
    private String name;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idShipyard", updatable = false)
    private Planet shipyard;

    /**
     * A warship can be part of a fleet or part of a mission or at shore leave.
     */
    @Nullable
    @ManyToOne
    @JoinColumn(name = "idFleet")
    private Fleet fleet;

    /**
     * A warship can be part of a fleet or part of a mission or at shore leave.
     */
    @Nullable
    @ManyToOne
    @JoinColumn(name = "idMission")
    private Mission mission;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idShipClass", updatable = false)
    private ShipClass shipClass;

    @Nonnull
    @NotNull
    @OneToOne(mappedBy = "warShip", cascade = CascadeType.ALL)
    @JoinColumn(name = "idWarshipHealthState", updatable = false)
    private WarshipHealthState warshipHealthState = new WarshipHealthState();

    public WarShip() {
    }

    public WarShip(@Nonnull final String name,
                   @Nonnull final Planet shipyard,
                   @Nonnull final Fleet fleet,
                   @Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(shipyard, "shipyard shouldn't be null!");
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        this.name = name;
        this.shipyard = shipyard;
        this.fleet = fleet;
        this.shipClass = shipClass;
        this.warshipHealthState = new WarshipHealthState(this);
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Planet getShipyard() {
        return shipyard;
    }

    @Nonnull
    public Fleet getFleet() {
        return fleet;
    }

    public void setFleet(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        this.fleet = fleet;
    }

    @Nonnull
    public ShipClass getShipClass() {
        return shipClass;
    }

    @Nonnull
    public WarshipHealthState getWarshipHealthState() {
        return warshipHealthState;
    }

    public boolean isActive() {
        return warshipHealthState.isFightingCapable();
    }

    public boolean isInactive() {
        return !warshipHealthState.isFightingCapable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WarShip)) return false;

        WarShip that = (WarShip) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id * 33;
    }

    public void createWarshipHealthState() {
        this.warshipHealthState = new WarshipHealthState(this);
    }
}
