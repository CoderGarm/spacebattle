package de.yuga.spacebattle.backend.entities.combined.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EModuleType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "Fleet.getAll", query = "SELECT f FROM Fleet f"),
        @NamedQuery(name = "Fleet.getAllByUser", query = "SELECT f FROM Fleet f WHERE f.owner = :owner"),
        @NamedQuery(name = "Fleet.checkShipInUse", query = "SELECT COUNT(f) FROM Fleet f LEFT JOIN f.ships s WHERE s.shipClass.id =:idShipClass")
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
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinTable(name = "fleetComposition",
            joinColumns = @JoinColumn(name = "idFleet"),
            inverseJoinColumns = @JoinColumn(name = "idWarShip"),
            uniqueConstraints = @UniqueConstraint(name = "fleetComposition_UC", columnNames = {"idFleet", "idWarShip"}))
    private final Set<WarShip> ships = new HashSet<>();

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idResourceDeposit", updatable = false)
    private final ResourceDeposit resourceDeposit = new ResourceDeposit(EDepositType.DEPOSITS);

    /**
     * The current location of this fleet. <br>
     * <br>
     * If null, then this is in hyper space.<br>
     * The planet could be null if the fleet is on a local movement.
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
    public Map<ShipClass, Integer> getShipsByClass() {
        return ships.stream().collect(Collectors.groupingBy(WarShip::getShipClass, Collectors.summingInt(x -> 1)));
    }

    @Nonnull
    public Set<WarShip> getShips() {
        return ships;
    }

    /**
     * Creates a new set of war ships by the given parameter.
     *
     * @param splitFleet the amount of ships by class as return
     * @return the separated war ships
     */
    public Set<WarShip> separateShips(@Nonnull final Map<ShipClass, Integer> splitFleet) {
        Preconditions.checkNotNull(splitFleet, "splitFleet shouldn't be null!");

        final Map<ShipClass, Integer> shipsByClass = getShipsByClass();
        // check if enough ships are present
        splitFleet.forEach((shipClass, amountToSeparate) -> {
            final Integer availableAmount = shipsByClass.get(shipClass);
            if (availableAmount < amountToSeparate) {
                throw new NotifySBUserException("There are not enough ships in the fleet do split them in that way.");
            }
        });
        // separate ships
        final Set<WarShip> toMove = new HashSet<>();
        splitFleet.forEach((shipClass, amountToSeparate) -> {
            final Set<WarShip> warShips = ships.stream()
                    .filter(w -> w.getShipClass().equals(shipClass)).limit(amountToSeparate)
                    .collect(Collectors.toSet());
            toMove.addAll(warShips);
        });
        ships.removeAll(toMove);
        return toMove;
    }

    public void updateShips(@Nonnull final Set<WarShip> warShips) {
        Preconditions.checkNotNull(warShips, "warShips shouldn't be null!");

        ships.addAll(warShips);
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

        final List<Integer> speeds = new ArrayList<>();
        final Set<ShipClass> shipClasses = ships.stream().map(WarShip::getShipClass).collect(Collectors.toSet());
        for (ShipClass sc : shipClasses) {
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
