package de.yuga.spacebattle.backend.entities.combined.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.CounterMissileWeaponry;
import de.yuga.spacebattle.backend.combat.dto.DamagePerRangeAndAlignment;
import de.yuga.spacebattle.backend.combat.dto.RangeDefinition;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.Deletable;
import de.yuga.spacebattle.backend.entities.misc.Operationable;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.calculator.FittingUtils.DEFENSIVE_FITTING;

@NamedQueries({
        @NamedQuery(name = "Fleet.getAll",
                query = "SELECT f FROM Fleet f WHERE f.isDeleted = false"),
        @NamedQuery(name = "Fleet.getAllWithMovement",
                query = "SELECT f FROM Fleet f WHERE f.move IS NOT NULL AND f.owner.id = :idUser AND f.isDeleted = false"),
        @NamedQuery(name = "Fleet.getAllWithoutMovement",
                query = "SELECT f FROM Fleet f WHERE f.move IS NULL AND f.isDeleted = false"),
        @NamedQuery(name = "Fleet.getAllWithoutInterstellarMovement",
                query = "SELECT f FROM Fleet f WHERE f.move.originOrbit.system = f.move.destinationOrbit.system AND f.isDeleted = false"),
        @NamedQuery(name = "Fleet.getFleetsWithInterstellarMovement",
                query = "SELECT f FROM Fleet f WHERE f.owner.id = :idUser AND  f.move IS NOT NULL AND f.move.originOrbit.system <> f.move.destinationOrbit.system AND f.isDeleted = false"),
        @NamedQuery(name = "Fleet.getAllByUser",
                query = "SELECT f FROM Fleet f WHERE f.owner = :owner AND  f.isDeleted = false"),
        @NamedQuery(name = "Fleet.getAllByUserAndSystem",
                query = "SELECT f FROM Fleet f WHERE f.owner.id = :idOwner AND f.orbit.system.id = :idStarSystem AND f.isDeleted = false"),
        @NamedQuery(name = "Fleet.checkShipInUse",
                query = "SELECT COUNT(f) FROM Fleet f LEFT JOIN f.ships s WHERE s.shipClass.id =:idShipClass AND f.isDeleted = false"),
        @NamedQuery(name = "Fleet.getAllForPlanet",
                query = "SELECT f FROM Fleet f LEFT JOIN f.move  " +
                        "WHERE f.isDeleted = false AND (f.orbit.system = :system AND f.orbit.orbit.xCoordinate = :xCoordinate  AND f.orbit.orbit.yCoordinate = :yCoordinate) " +
                        "OR ( f.move.originOrbit.system = :system AND  f.move.originOrbit.orbit.xCoordinate = :xCoordinate AND f.move.originOrbit.orbit.yCoordinate = :yCoordinate) " +
                        "OR (f.move.destinationOrbit.system = :system AND f.move.destinationOrbit.orbit.xCoordinate = :xCoordinate AND f.move.destinationOrbit.orbit.yCoordinate = :yCoordinate)"),
        @NamedQuery(name = "Fleet.getAllAnchoredForPlanet",
                query = "SELECT f FROM Fleet f LEFT JOIN f.move  " +
                        "WHERE f.isDeleted = false AND (f.orbit.system = :system AND f.orbit.orbit.xCoordinate = :xCoordinate  AND f.orbit.orbit.yCoordinate = :yCoordinate)"),
        @NamedQuery(name = "Fleet.getAllDamagedForPlanetAndOwner",
                query = "SELECT f FROM Fleet f LEFT JOIN f.move  " +
                        "WHERE f.isDeleted = false AND f.needsRepair = true " +
                        "AND (f.orbit.system = :system AND f.orbit.orbit.xCoordinate = :xCoordinate  AND f.orbit.orbit.yCoordinate = :yCoordinate) " +
                        "OR ( f.move.originOrbit.system = :system AND  f.move.originOrbit.orbit.xCoordinate = :xCoordinate AND f.move.originOrbit.orbit.yCoordinate = :yCoordinate) " +
                        "OR (f.move.destinationOrbit.system = :system AND f.move.destinationOrbit.orbit.xCoordinate = :xCoordinate AND f.move.destinationOrbit.orbit.yCoordinate = :yCoordinate) "),
})
@Entity
@Table(name = "fleet")
@AttributeOverride(name = "id", column = @Column(name = "idFleet"))
public class Fleet extends Operationable {

    @Nonnull
    @NotNull
    private String name;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idOwner", updatable = false)
    private User owner;

    @Nonnull
    @NotNull
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idFleet")
    private final Set<WarShip> ships = new HashSet<>();

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idResourceDeposit", updatable = false)
    private final ResourceDeposit resourceDeposit = new ResourceDeposit(EDepositType.DEPOSITS);

    /**
     * The current location of this fleet. <br>
     * <br>
     * If null, then this is in hyper space.<br>
     * The <code>orbit.orbit could be null if the fleet is on a local movement.
     */
    @Nullable
    @Embedded
    @AttributeOverride(name = "orbit.xCoordinate", column = @Column(name = "xCoordinateLocation"))
    @AttributeOverride(name = "orbit.yCoordinate", column = @Column(name = "yCoordinateLocation"))
    @AssociationOverride(name = "system", joinColumns = @JoinColumn(name = "idStarSystemLocation"))
    private FleetOrbit orbit;

    /**
     * The move includes the origin and the destination if the start is different from the current {@link #orbit}.
     */
    @Nullable
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idMove", unique = true)
    private Move move;

    @Column(columnDefinition = "bit not null default false")
    private boolean needsRepair = false;

    @Nonnull
    @OneToMany(mappedBy = "constructable.fleet", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Job> jobs = new HashSet<>();

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

    public boolean isActive() {
        return jobs.stream().noneMatch(Deletable::isAlive) && isAlive() && isOperational();
    }

    public boolean isOperational() {
        return super.isOperational() && getAliveShips().stream().allMatch(WarShip::isOperational);
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
        return getAliveShips().stream()
                .collect(Collectors.groupingBy(WarShip::getShipClass, Collectors.summingInt(x -> 1)));
    }

    @Nonnull
    public Set<WarShip> getAliveShips() {
        return ships.stream()
                .filter(Deletable::isAlive)
                .collect(Collectors.toSet());
    }

    @Nonnull
    public Set<WarShip> getAllShips() {
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

        // todo separate ships in ui
        final Map<ShipClass, Integer> shipsByClass = getShipsByClass();
        // check if enough ships are present
        splitFleet.forEach((shipClass, amountToSeparate) -> {
            final Integer availableAmount = shipsByClass.get(shipClass);
            if (availableAmount < amountToSeparate) {
                throw new NotifyWebUserException("There are not enough ships in the fleet do split them in that way.");
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

    public void addShips(@Nonnull final Set<WarShip> warShips) {
        Preconditions.checkNotNull(warShips, "warShips shouldn't be null!");

        ships.addAll(warShips);
    }

    public void removeShips(@Nonnull final Set<WarShip> warShips) {
        Preconditions.checkNotNull(warShips, "warShips shouldn't be null!");

        ships.removeAll(warShips);
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
    public void setMove(@Nullable final Move move) {
        if (move != null && orbit != null) {
            if (move.isInterstellarTravel()) {
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
        return orbit != null && orbit.getSystem() != null && orbit.getOrbit() != null;
    }

    /**
     * Checks if all ships of the fleet are capable of driving faster than light.
     *
     * @return <code>true</code> if the fleet can drive faster than light, <code>false</code> otherwise
     */
    public boolean isFTLCapable() {
        return ships.stream().map(WarShip::getShipClass).allMatch(ShipClass::isFTLCapable);
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

    /**
     * Returns the range units which can be passed per turn based on the slowest ship.
     *
     * @return the maximal distance which could be passed in a tick
     */
    public Acceleration getAccelerationFor(@Nonnull final EModuleType eModuleType) {
        Preconditions.checkNotNull(eModuleType, "eModuleType shouldn't be null!");
        Preconditions.checkArgument((eModuleType == EModuleType.FTLPROPULSION || eModuleType == EModuleType.PROPULSION),
                "EModuleType must be kind of propulsion.");

        final List<Integer> speeds = new ArrayList<>();
        final Set<ShipClass> shipClasses = ships.stream().map(WarShip::getShipClass).collect(Collectors.toSet());
        EHyperBand hyperBand = EHyperBand.NONE;
        for (ShipClass sc : shipClasses) {
            final double propulsionSupportFactor = sc.getSupportFittings().stream()
                    .filter(s -> eModuleType == s.getPassiveModule().getSupportType().getModifiedProperty())
                    .findAny().stream()
                    .map(SupportFitting::getAbsoluteValueAsFactor).reduce(0D, Double::sum);

            final Propulsion propulsion = sc.getPropulsion();
            if (propulsion == null || (EModuleType.FTLPROPULSION == eModuleType && !propulsion.isFtlCapable())) {
                // if no propulsion present or ftl is used and no ftl is present
                return Acceleration.ZERO;
            }
            // calculate effect of support modules
            final BigDecimal factor = BigDecimal.ONE.add(new BigDecimal(propulsionSupportFactor));
            final BigDecimal effectValue = new BigDecimal(propulsion.getEffectValue());
            // if the prop's hyper band is lower than use this
            hyperBand = propulsion.getHyperBand().ordinal() >= hyperBand.ordinal() ? propulsion.getHyperBand() : hyperBand;
            speeds.add(effectValue.multiply(factor, ResourceDeposit.MATH_CONTEXT_INTEGER).intValue());
        }
        Collections.sort(speeds);
        final EAccelerationMetric accelerationMetric = eModuleType == EModuleType.PROPULSION ? EAccelerationMetric.G : EAccelerationMetric.C;
        hyperBand = eModuleType == EModuleType.FTLPROPULSION ? hyperBand : EHyperBand.NONE;
        return new Acceleration(new BigDecimal(speeds.get(0)), accelerationMetric, hyperBand);
    }

    /**
     * Returns the maximum weapon range of the fleet.
     *
     * @return the maximum weapon range
     */
    public Distance getMaximumWeaponRange() {
        return this.getShipsByClass().keySet().stream()
                .map(ShipClass::getMaximumWeaponRange)
                .max(Distance::compareTo)
                .orElse(Distance.ZERO);
    }

    /**
     * Returns the maximum weapon range of the fleet.
     *
     * @param weaponType the weapon type as filter
     * @return the maximum weapon range
     */
    public Distance getMaximumWeaponRangePerType(@Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        return this.getShipsByClass().keySet().stream()
                .map(shipClass -> shipClass.getMaximumWeaponRangePerType(weaponType))
                .max(Distance::compareTo)
                .orElse(Distance.ZERO);
    }

    public List<DamagePerRangeAndAlignment> getDamagePerRangePerType(@Nonnull final RangeDefinition boundaries,
                                                                     @Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(boundaries, "boundaries shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        return getShipsByClass().entrySet().stream().map(entry -> {
            final ShipClass shipClass = entry.getKey();
            final Integer amount = entry.getValue();
            final List<DamagePerRangeAndAlignment> damagePerRangePerWeaponType = shipClass.getDamagePerRangePerWeaponType(boundaries, weaponType);
            return damagePerRangePerWeaponType.stream().map(d -> d.multiplyDamage(amount)).collect(Collectors.toList());
        }).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Returns the range of the fleets electronic countermeasures.
     *
     * @return the eloka range
     */
    public Distance getElokaRange() {
        return getShipsByClass().keySet().stream()
                .filter(shipClass -> shipClass.getElectronicWarfare() != null)
                .map(shipClass -> {
                    final ElectronicWarfare eloka = shipClass.getElectronicWarfare();
                    assert eloka != null;
                    return eloka.getEffectiveRange();
                }).max(Distance::compareTo).orElse(Distance.ZERO);

    }

    /**
     * Returns the effect value of the fleets electronic countermeasures.
     *
     * @return the eloka range
     */
    public int getElokaEffectValue() {
        return getShipsByClass().keySet().stream()
                .filter(shipClass -> shipClass.getElectronicWarfare() != null)
                .map(shipClass -> {
                    final ElectronicWarfare eloka = shipClass.getElectronicWarfare();
                    assert eloka != null;
                    return eloka.getEffectValue();
                }).mapToInt(Integer::intValue).sum();
    }

    /**
     * Returns all anti missile weapons of this fleet.
     *
     * @return the anti missile weapons
     */
    public CounterMissileWeaponry getCounterMissileWeaponry() {
        final List<AlignedFitting> alignedFittings = getShipsByClass().keySet().stream()
                .map(ShipClass::getFittings)
                .filter(fittings -> !fittings.isEmpty())
                .map(fittings -> fittings.stream().filter(DEFENSIVE_FITTING).findAny().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new CounterMissileWeaponry(alignedFittings);
    }

    @Nonnull
    public Velocity getMaxVelocity(@Nonnull final EModuleType propulsion) {
        Preconditions.checkNotNull(propulsion, "propulsion shouldn't be null!");
        Preconditions.checkArgument(propulsion == EModuleType.PROPULSION || propulsion == EModuleType.FTLPROPULSION, "propulsion must be propulsion type!");

        final Acceleration acceleration = getAccelerationFor(propulsion);
        final EHyperBand hyperBand = acceleration.getHyperBand();
        final BigDecimal vesselTopSpeed = hyperBand.getEffectiveTopSpeed();
        return new Velocity(vesselTopSpeed, EDistanceMetric.M, ETimeMetric.SECOND);
    }

    public boolean isNeedsRepair() {
        return needsRepair;
    }

    public void setNeedsRepair(final boolean needsRepair) {
        this.needsRepair = needsRepair;
    }
}
