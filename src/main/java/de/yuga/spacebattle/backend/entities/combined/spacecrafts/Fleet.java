package de.yuga.spacebattle.backend.entities.combined.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EResourceSubType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

@NamedQueries({
        @NamedQuery(name = "Fleet.getAll", query = "SELECT f FROM Fleet f"),
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
    private final Map<ShipClass, Integer> ships = new HashMap<>();

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResourceDeposit", updatable = false)
    private final ResourceDeposit resourceDeposit = new ResourceDeposit(EResourceSubType.DEPOSITS);

    @Nullable
    @Embedded
    private FleetOrbit orbit;

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
        Preconditions.checkState(ships != null, "This will not happen");

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

    /**
     * Returns the range units which can be passed per turn based on the slowest ship.
     *
     * @return the maximal FTL speed
     */
    public BigDecimal getFTLRangePerTick() {
        if (ships == null) {
            throw new NotifySBUserException("This should never happen");
        }
        List<Integer> speed = new ArrayList<>();
        for (ShipClass sc : ships.keySet()) {
            int ftlSpeed = 0;
            for (Module m : sc.getModules().keySet()) {
                if (m.getModuleType() == EModuleType.FTLPROPULSION) {
                    ftlSpeed += m.getEffectiveValue(owner.getRaceType());
                }
            }
            speed.add(ftlSpeed);
        }
        Collections.sort(speed);
        return new BigDecimal(speed.get(0));
    }
}
