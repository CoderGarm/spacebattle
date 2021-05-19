package de.yuga.spacebattle.backend.entities.combined.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EResourceSubType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

@NamedQueries({
        @NamedQuery(name = "Fleet.getAll", query = "SELECT f FROM Fleet f"),
        @NamedQuery(name = "Fleet.getAllByUser", query = "SELECT f FROM Fleet f WHERE f.owner = :owner"),
        @NamedQuery(name = "Fleet.checkShipInUse", query = "SELECT COUNT(f) FROM Fleet f LEFT JOIN f.ships s WHERE KEY(s) =:idShipClass")
})
@Entity
@Table(name = "fleet")
@AttributeOverride(name = "id", column = @Column(name = "idFleet"))
public class Fleet extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "Fleet must have a name")
    private String name;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idOwner", updatable = false)
    private User owner;

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "idShipClass", referencedColumnName = "idShipClass")
    @Column(name = "amount")
    @CollectionTable(name = "fleetcomposition", joinColumns = @JoinColumn(name = "idFleet"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idFleet")
    private final Map<ShipClass, Integer> ships = new HashMap<>();

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idResourceDeposit", updatable = false)
    private final ResourceDeposit resourceDeposit = new ResourceDeposit(EResourceSubType.DEPOSITS);

    /**
     * The current location of this fleet.
     * <p>
     * If null, then this is in hyper space.
     */
    @Nullable
    @Embedded
    private FleetOrbit orbit;

    /**
     * The move includes the origin and the destination if the start is different from the current {@link #orbit}.
     */
    @Nullable
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idMove", unique = true)
    private Move move;

    public Fleet() {
    }

    public Fleet(@Nonnull final String name, @Nonnull final User owner, @Nonnull final FleetOrbit orbit) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.owner = owner;
        this.name = name;
        this.orbit = orbit;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull final String name) {
        this.name = name;
    }

    public void setOwner(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.owner = owner;
    }

    @Nonnull
    public User getOwner() {
        return owner;
    }

    @Nonnull
    public Map<ShipClass, Integer> getShips() {
        return ships;
    }

    public void updateShips(@Nonnull final ShipClass shipClass, final int amount) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        if (ships.containsKey(shipClass)) {
            Integer oldAmount = this.ships.get(shipClass);
            this.ships.put(shipClass, oldAmount + amount);
        } else {
            this.ships.put(shipClass, amount);
        }
    }

    @Nonnull
    public ResourceDeposit getResourceDeposit() {
        return resourceDeposit;
    }

    @Nullable
    public FleetOrbit getOrbit() {
        return orbit;
    }

    public void setOrbit(@Nullable final FleetOrbit orbit) {
        this.orbit = orbit;
    }

    @Nullable
    public Move getMove() {
        return move;
    }

    /**
     * Sets this fleet in motion and modifies the fleet's orbit accordingly.
     *
     * @param move the planned move
     */
    public void setMove(@Nullable Move move) {
        if (move != null && orbit != null) {
            if (move.isSystemTravel()) {
                orbit = null;
            } else {
                orbit.leavePlanet();
            }
        }
        this.move = move;
    }

    /**
     * Checks that this fleet is in a planetary orbit.
     *
     * @return <code>true</code> if this fleet is in a planetary orbit, <code>false</code> otherwise
     */
    public boolean isInPlanetaryOrbit() {
        return orbit != null && orbit.getPlanet() != null;
    }

    /**
     * Returns the range units which can be passed per turn based on the slowest ship.
     *
     * @return the maximal FTL speed
     */
    public BigDecimal getRangePerTick(@Nonnull final EModuleType eModuleType) {
        Preconditions.checkNotNull(eModuleType, "eModuleType shouldn't be null!");
        Preconditions.checkArgument((eModuleType == EModuleType.FTLPROPULSION || eModuleType == EModuleType.PROPULSION),
                "EModuleType must be kind of propulsion.");

        List<Integer> speeds = new ArrayList<>();
        for (ShipClass sc : ships.keySet()) {
            final Propulsion propulsion = sc.getPropulsion();
            if (propulsion == null || (EModuleType.FTLPROPULSION == eModuleType && !propulsion.isFtlCapable())) {
                // if no propulsion present or ftl is used and no ftl is present
                return BigDecimal.ZERO;
            }
            speeds.add(propulsion.getEffectValue());
        }
        Collections.sort(speeds);
        return new BigDecimal(speeds.get(0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fleet)) return false;

        Fleet fleet = (Fleet) o;

        return id == fleet.id;
    }

    @Override
    public int hashCode() {
        return id * 31;
    }
}
