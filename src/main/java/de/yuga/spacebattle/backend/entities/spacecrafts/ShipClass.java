package de.yuga.spacebattle.backend.entities.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.DamagePerRangeAndAlignment;
import de.yuga.spacebattle.backend.combat.dto.RangeDefinition;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.Deletable;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.validators.ShipValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "ShipClass.getAll", query = "SELECT a FROM ShipClass a WHERE a.isDeleted = false"),
        @NamedQuery(name = "ShipClass.getAllByOwner", query = "SELECT a FROM ShipClass a WHERE a.owner = :owner AND a.isDeleted = false"),
        @NamedQuery(name = "ShipClass.getAllLatestByOwner", query = "SELECT a FROM ShipClass a WHERE a.owner = :owner AND a.successor IS NULL AND a.isDeleted = false"),
        @NamedQuery(name = "ShipClass.checkIfNameIsFree", query = "SELECT COUNT(a) FROM ShipClass a WHERE a.owner.id = :idOwner AND UPPER(a.name) = UPPER(:className)"),
})
@Entity
@ShipValidator
@Table(name = "shipClass")
@AttributeOverride(name = "id", column = @Column(name = "idShipClass"))
public class ShipClass extends Deletable {

    private final static MathContext MATH_CONTEXT = new MathContext(8, RoundingMode.DOWN);

    @Nonnull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idOwner")
    private User owner;

    /**
     * Must not unique or unique for user because at least the ships in an ancestry row could have the same names.
     */
    @Nonnull
    @NotNull
    @Size(min = 3, max = 30, message = "name should be between 3 and 30 characters long")
    private String name;

    @Valid
    @Nullable
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idHull", updatable = false)
    private Hull hull;

    @Valid
    @Nullable
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPropulsion")
    private Propulsion propulsion;

    @Valid
    @Nullable
    @ManyToOne
    @JoinColumn(name = "idArmor")
    private Armor armor;

    @Valid
    @Nullable
    @ManyToOne
    @JoinColumn(name = "idSidewall")
    private Sidewall sidewall;

    @Valid
    @Nullable
    @ManyToOne
    @JoinColumn(name = "idElectronicWarfare")
    private ElectronicWarfare electronicWarfare;

    @Valid
    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alignedFitting", joinColumns = @JoinColumn(name = "idShipClass"))
    private final Set<AlignedFitting> fittings = new HashSet<>();

    @Valid
    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ammunitionFitting", joinColumns = @JoinColumn(name = "idShipClass"))
    private final Set<AmmunitionFitting> ammunitionFittings = new HashSet<>();

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "supportFitting", joinColumns = @JoinColumn(name = "idShipClass"))
    private final Set<SupportFitting> supportFittings = new HashSet<>();

    /**
     * The references to the predecessor ship classes in case that this is a class which is another version of their predecessors.
     */
    @Nullable
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idPredecessor", unique = true)
    private ShipClass predecessor;

    /**
     * The successor of this. The opposite of {@link ShipClass#predecessor}.
     */
    @Nullable
    @OneToOne
    @JoinColumn(name = "idSuccessor", unique = true)
    private ShipClass successor;

    public ShipClass() {
    }

    public ShipClass(@Nonnull final User owner,
                     @Nonnull final String name,
                     @Nonnull final Hull hull,
                     @Nullable final ShipClass predecessor) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(hull, "hull shouldn't be null!");

        this.owner = owner;
        this.name = name;
        this.hull = hull;
        if (predecessor != null) {
            this.predecessor = predecessor;
            this.predecessor.successor = this;
        }
    }

    @Nonnull
    public User getOwner() {
        return owner;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");

        this.name = name;
    }

    public void setHull(@Nullable final Hull hull) {
        this.hull = hull;
    }

    @Nullable
    public Hull getHull() {
        return hull;
    }

    /**
     * Will calculate and return the full costs of this ship.
     *
     * @return the total costs
     */
    @Nonnull
    public ResourceDeposit getCosts() {
        final ResourceDeposit clonedDeposit = new ResourceDeposit(EDepositType.COSTS);

        final Map<ESupportType, SupportFitting> supportTypeToModule = supportFittings.stream()
                .collect(Collectors.toMap(e -> e.getPassiveModule().getSupportType(), Function.identity()));

        if (hull != null) {
            updateCosts(clonedDeposit, supportTypeToModule, hull.getCosts());
            if (propulsion != null) {
                updateCosts(clonedDeposit, supportTypeToModule, propulsion.getCosts(hull));
            }
            if (armor != null) {
                updateCosts(clonedDeposit, supportTypeToModule, armor.getCosts(hull));
            }
            if (electronicWarfare != null) {
                updateCosts(clonedDeposit, supportTypeToModule, electronicWarfare.getCosts());
            }
            if (sidewall != null) {
                updateCosts(clonedDeposit, supportTypeToModule, sidewall.getCosts());
            }
        }
        fittings.forEach(fitting -> {
            int amount = fitting.getAmount();
            for (; amount >= 0; amount--) {
                final Weapon weapon = fitting.getWeapon();
                final Launcher launcher = fitting.getLauncher();
                if (weapon != null) {
                    updateCosts(clonedDeposit, supportTypeToModule, weapon.getCosts());
                }
                if (launcher != null) {
                    updateCosts(clonedDeposit, supportTypeToModule, launcher.getCosts());
                }
            }
        });
        ammunitionFittings.forEach(s -> {
            int amount = s.getAmount();
            for (; amount >= 0; amount--) {
                updateCosts(clonedDeposit, supportTypeToModule, s.getAmmunitionModule().getCosts());
            }
        });
        supportFittings.forEach(s -> {
            int amount = s.getAmount();
            for (; amount >= 0; amount--) {
                updateCosts(clonedDeposit, supportTypeToModule, s.getPassiveModule().getCosts());
            }
        });
        return clonedDeposit;
    }

    /**
     * Calculates and sets the costs by a possible existent support module.
     *
     * @param clonedDeposit       the deposit to update
     * @param supportTypeToModule the support module map
     * @param costsToAdd          the resource map
     */
    private void updateCosts(@Nonnull final ResourceDeposit clonedDeposit,
                             @Nonnull final Map<ESupportType, SupportFitting> supportTypeToModule,
                             @Nonnull final ResourceDeposit costsToAdd) {
        Preconditions.checkNotNull(clonedDeposit, "clonedDeposit shouldn't be null!");
        Preconditions.checkNotNull(supportTypeToModule, "supportTypeToModule shouldn't be null!");
        Preconditions.checkNotNull(costsToAdd, "costsToAdd shouldn't be null!");

        for (final EResourceType resourceType : EResourceType.values()) {
            final SupportFitting supportFitting = supportTypeToModule.get(ESupportType.getByValue(resourceType));
            final double absoluteValueAsFactor = supportFitting != null ? supportFitting.getAbsoluteValueAsFactor() : 1;
            if (resourceType == EResourceType.POPULATION) {
                clonedDeposit.updateCrew(costsToAdd.getCrewRequirement(), ECalculationType.ADD);
            } else {
                final long amount = costsToAdd.getResourceAmountByType(resourceType);
                final BigDecimal effectiveAmount = new BigDecimal(amount).multiply(new BigDecimal(absoluteValueAsFactor), ResourceDeposit.MATH_CONTEXT_INTEGER);
                clonedDeposit.updateResource(resourceType, effectiveAmount.longValue());
            }
        }
    }

    @Nonnull
    public Set<AlignedFitting> getFittings() {
        return fittings;
    }

    @Nonnull
    public Set<AlignedFitting> getFittingByAlignment(@Nonnull final EWeaponAlignment weaponAlignment) {
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment shouldn't be null!");

        return fittings.stream()
                .filter(e -> weaponAlignment == e.getWeaponAlignment())
                .collect(Collectors.toSet());
    }

    /**
     * Necessary while vaadin data binding needs a setter for a full set of data and is not able to use an incremental add method.
     * Removes all not longer contained modules and add or replace everything else.
     *
     * @param fittings the set of fittings
     */
    public void setFittings(@Nonnull final Set<AlignedFitting> fittings) {
        Preconditions.checkNotNull(fittings, "fittings shouldn't be null!");

        this.fittings.clear();
        this.fittings.addAll(fittings);
    }

    @Nullable
    public Propulsion getPropulsion() {
        return propulsion;
    }

    public void setPropulsion(@Nullable Propulsion propulsion) {
        this.propulsion = propulsion;
    }

    @Nullable
    public Armor getArmor() {
        return armor;
    }

    public void setArmor(@Nullable Armor armor) {
        this.armor = armor;
    }

    @Nullable
    public Sidewall getSidewall() {
        return sidewall;
    }

    public void setSidewall(@Nullable Sidewall sidewall) {
        this.sidewall = sidewall;
    }


    /**
     * Adds a fitting to the ammunition fitting.
     *
     * @param fitting the fitting to add
     */
    public void addAmmunitionFitting(@Nonnull final AmmunitionFitting fitting) {
        Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

        ammunitionFittings.stream()
                .filter(fitting::equals)
                .findFirst()
                .ifPresentOrElse(
                        alignedFitting -> alignedFitting.setAmount(fitting.getAmount()),
                        () -> ammunitionFittings.add(fitting));
    }

    /**
     * Necessary while vaadin data binding needs a setter for a full set of data and is not able to use an incremental add method.
     * Removes all no longer contained modules and add or replace everything else.
     *
     * @param fittings the set of fittings
     */
    public void setAmmunitionFittings(@Nonnull final Set<AmmunitionFitting> fittings) {
        Preconditions.checkNotNull(fittings, "fittings shouldn't be null!");

        this.ammunitionFittings.clear();
        this.ammunitionFittings.addAll(fittings);
    }

    @Nonnull
    public Set<AmmunitionFitting> getAmmunitionFittings() {
        return ammunitionFittings;
    }

    /**
     * Necessary while vaadin data binding needs a setter for a full set of data and is not able to use an incremental add method.
     * Removes all not longer contained modules and add or replace everything else.
     *
     * @param fittings the set of fittings
     */
    public void setSupportFittings(@Nonnull final Set<SupportFitting> fittings) {
        Preconditions.checkNotNull(fittings, "fittings shouldn't be null!");

        this.supportFittings.clear();
        this.supportFittings.addAll(fittings);
    }

    @Nonnull
    public Set<SupportFitting> getSupportFittings() {
        return supportFittings;
    }

    /**
     * Returns the version or mark of this class - like in 'Mk. I' or 'Mk. II'.
     *
     * @return the mark
     */
    public int getMark() {
        int counter = 1;
        ShipClass runner = this;
        while (runner.getPredecessor() != null) {
            runner = runner.getPredecessor();
            counter++;
        }

        return counter;
    }

    @Nullable
    public ShipClass getPredecessor() {
        return predecessor;
    }

    @Nullable
    public ShipClass getSuccessor() {
        return successor;
    }

    /**
     * Checks if this class has a successor.
     *
     * @return <code>true</code> if yes, <code>false</code> otherwise
     */
    public boolean hasSuccessor() {
        return successor != null;
    }

    /**
     * Checks if this class is the last in it's ancestry row.
     *
     * @return <code>true</code> if yes, <code>false</code> otherwise
     */
    public boolean hasNoSuccessor() {
        return !hasSuccessor();
    }

    /**
     * Checks if the class is capable of faster than light flights.
     *
     * @return <code>true</code> if the class can drive faster than light, <code>false</code> otherwise
     */
    public boolean isFTLCapable() {
        if (propulsion == null) {
            return false;
        }
        return propulsion.isFtlCapable();
    }

    @Nullable
    public ElectronicWarfare getElectronicWarfare() {
        return electronicWarfare;
    }

    public void setElectronicWarfare(@Nullable ElectronicWarfare electronicWarfare) {
        this.electronicWarfare = electronicWarfare;
    }

    /**
     * Returns the maximum weapon range of the ship class.
     *
     * @return the maximum weapon range
     */
    public Distance getMaximumWeaponRange() {

        final List<Distance> sortedRanged = fittings.stream()
                .map(fitting -> {
                    Distance damageProjectionRange = Distance.ZERO;
                    final Weapon weapon = fitting.getWeapon();
                    if (weapon != null) {
                        damageProjectionRange = weapon.getDamageProjectionRange();
                    }
                    final Launcher launcher = fitting.getLauncher();
                    if (launcher != null) {
                        final Missile missile = launcher.getAmmunitionModule().getMissile();
                        damageProjectionRange = missile.getMaximumMissileRange();
                    }
                    return damageProjectionRange;
                })
                .sorted(Distance::compareTo).collect(Collectors.toList());
        return sortedRanged.stream().max(Distance::compareTo).orElse(Distance.ZERO);
    }

    /**
     * Returns the maximum weapon range of the ship class.
     *
     * @param weaponType the weapon type to filter
     * @return the maximum weapon range
     */
    public Distance getMaximumWeaponRangePerType(@Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        final List<Distance> sortedRanged = fittings.stream()
                .filter(fitting -> weaponType == fitting.getWeaponType())
                .map(fitting -> {
                    Distance damageProjectionRange = Distance.ZERO;
                    final Weapon weapon = fitting.getWeapon();
                    if (weapon != null) {
                        damageProjectionRange = weapon.getDamageProjectionRange();
                    }
                    final Launcher launcher = fitting.getLauncher();
                    if (launcher != null) {
                        final Missile missile = launcher.getAmmunitionModule().getMissile();
                        damageProjectionRange = missile.getMaximumMissileRange();
                    }
                    return damageProjectionRange;
                })
                .sorted(Distance::compareTo).collect(Collectors.toList());
        return sortedRanged.stream().max(Distance::compareTo).orElse(Distance.ZERO);
    }

    /**
     * Returns the damage which can be applied by this class to the given range in meter.
     *
     * @param boundaries the boundaries
     * @return the damage value
     */
    public List<DamagePerRangeAndAlignment> getDamagePerRangePerWeaponType(@Nonnull final RangeDefinition boundaries,
                                                                           @Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(boundaries, "boundaries shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        return fittings.stream()
                .filter(fitting -> weaponType == fitting.getWeaponType())
                .map(fitting -> fitting.getDamagePerRange(boundaries))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipClass)) return false;

        ShipClass shipClass = (ShipClass) o;
        return id == shipClass.id;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }

    public int getUsedCapacity(@Nonnull final ECapacityAreaType capacityAreaType) {
        Preconditions.checkNotNull(capacityAreaType, "capacityAreaType must not be empty");

        switch (capacityAreaType) {
            case BOW:
            case STERN:
            case BROADSIDE:
                assert capacityAreaType.getAlignment() != null : "Otherwise we had a problem.";
                return getFittingByAlignment(capacityAreaType.getAlignment()).stream()
                        .map(AlignedFitting::calculateUsedCapacity)
                        .reduce(0, Integer::sum);
            case MODULE:
                int usedCapacity = getSupportFittings().stream()
                        .map(SupportFitting::calculateUsedCapacity)
                        .reduce(0, Integer::sum);
                usedCapacity += getAmmunitionFittings().stream()
                        .map(AmmunitionFitting::calculateUsedCapacity)
                        .reduce(0, Integer::sum);
                if (hull != null) {
                    usedCapacity += propulsion != null ? propulsion.getUseCapacity(hull) : 0;
                    usedCapacity += armor != null ? armor.getUseCapacity(hull) : 0;
                    usedCapacity += sidewall != null ? sidewall.getUseCapacity() : 0;
                    usedCapacity += electronicWarfare != null ? electronicWarfare.getUseCapacity() : 0;
                }
                return usedCapacity;
            case OVERALL:
                return ECapacityAreaType.getValuesWithoutOverall().stream().map(this::getUsedCapacity).reduce(0, Integer::sum);
            default:
                return 0;
        }
    }

    @Nonnull
    public Acceleration getAcceleration(@Nonnull final EHyperBand hyperBand) {
        Preconditions.checkNotNull(hyperBand, "hyperBand must not be empty");

        final EModuleType eModuleType = hyperBand == EHyperBand.NONE ? EModuleType.PROPULSION : EModuleType.FTLPROPULSION;
        final double propulsionSupportFactor = getSupportFittings().stream()
                .filter(s -> eModuleType == s.getPassiveModule().getSupportType().getModifiedProperty())
                .findAny().stream()
                .map(SupportFitting::getAbsoluteValueAsFactor).reduce(0D, Double::sum);
        final BigDecimal factor = BigDecimal.ONE.add(new BigDecimal(propulsionSupportFactor));
        final BigDecimal accelerationValue = getMathematicallyAcceleration(hyperBand).multiply(factor, ResourceDeposit.MATH_CONTEXT_INTEGER);
        return new Acceleration(accelerationValue, EAccelerationMetric.G, hyperBand);
    }

    @Nonnull
    private BigDecimal getMathematicallyAcceleration(@Nonnull final EHyperBand hyperBand) {
        Preconditions.checkNotNull(hyperBand, "hyperBand must not be empty");

        if (getPropulsion() == null || getHull() == null) {
            return BigDecimal.ZERO;
        }

        if (getPropulsion().getHyperBand().getVelocityMultiplier() < hyperBand.getVelocityMultiplier()) {
            return BigDecimal.ZERO;
        }

        //y = 558.1465 - 0.0001075032*x + 7.261618e-11*x^2 - 2.1753440000000002e-17*x^3 + 2.786797e-24*x^4 - 1.275354e-31*x^5
        final BigDecimal a = BigDecimal.valueOf(getPropulsion().getEffectValue());
        final List<BigDecimal> paramList = List.of(
                BigDecimal.valueOf(-0.0001075032),
                BigDecimal.valueOf(7.261618).scaleByPowerOfTen(-11),
                BigDecimal.valueOf(-2.175344).scaleByPowerOfTen(-17),
                BigDecimal.valueOf(2.786797).scaleByPowerOfTen(-24),
                BigDecimal.valueOf(-1.275354).scaleByPowerOfTen(-31)
        );

        final BigDecimal x = BigDecimal.valueOf(getHull().getTonnage());
        /* fixme
            currently no influence from the tonnage:, solve by
                - research hyper band -> is enum
                - research efficiency improvement -> is running research level
                - researchable base-propulsion module by tech level -> changes effect value -> acceleration
                - prop-module costs x% of the hull, including construction capacity
                - user chooses hyperband -> cost change (e.g. NONE to ALPHA +80% CC)
                - user chooses CIVIL vs MILITARY
                - prop will be property of ship class as "prop tech level, prop hyper band, prop technology type -> is present by default
        */
        BigDecimal result = a;
        for (int i = 0; i < paramList.size(); i++) {
            final BigDecimal coefficient = paramList.get(i);
            final BigDecimal inBetween = coefficient.multiply(x.pow(i + 1), MATH_CONTEXT);
            result = result.add(inBetween);
        }
        result = result.setScale(0, RoundingMode.HALF_EVEN);
        return result.multiply(BigDecimal.valueOf(hyperBand.getVelocityMultiplier()), MATH_CONTEXT);
    }
}
