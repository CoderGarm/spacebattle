package de.yuga.spacebattle.backend.entities.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.DamagePerRangeAndAlignment;
import de.yuga.spacebattle.backend.combat.dto.RangeDefinition;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.Deletable;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.validators.ShipValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
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
@Table(name = "shipClass")
@AttributeOverride(name = "id", column = @Column(name = "idShipClass"))
@ShipValidator
public class ShipClass extends Deletable {

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

    @Nullable
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idHull", updatable = false)
    private Hull hull;

    @Nullable
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPropulsion")
    private Propulsion propulsion;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idArmor")
    private Armor armor;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idSidewall")
    private Sidewall sidewall;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idElectronicWarfare")
    private ElectronicWarfare electronicWarfare;

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alignedFitting", joinColumns = @JoinColumn(name = "idShipClass"))
    private final Set<AlignedFitting> fittings = new HashSet<>();

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
        }
        if (propulsion != null) {
            updateCosts(clonedDeposit, supportTypeToModule, propulsion.getCosts());
        }
        if (armor != null) {
            updateCosts(clonedDeposit, supportTypeToModule, armor.getCosts());
        }
        if (electronicWarfare != null) {
            updateCosts(clonedDeposit, supportTypeToModule, electronicWarfare.getCosts());
        }
        if (sidewall != null) {
            updateCosts(clonedDeposit, supportTypeToModule, sidewall.getCosts());
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
    public Set<AlignedFitting> getFittingByType(@Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        return fittings.stream()
                .filter(e -> weaponType == e.getWeaponType())
                .collect(Collectors.toSet());
    }

    @Nonnull
    public Set<AlignedFitting> getFittingByTypeAndAlignment(@Nonnull final EWeaponType weaponType,
                                                            @Nonnull final EWeaponAlignment weaponAlignment) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment shouldn't be null!");

        return fittings.stream()
                .filter(e -> weaponType == e.getWeaponType() && weaponAlignment == e.getWeaponAlignment())
                .collect(Collectors.toSet());
    }

    /**
     * Adds a fitting to the ships modules.
     *
     * @param fitting the fitting to add
     */
    public void addFitting(@Nonnull final AlignedFitting fitting) {
        Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

        fittings.stream()
                .filter(fitting::equals)
                .findFirst()
                .ifPresentOrElse(
                        alignedFitting -> alignedFitting.setAmount(fitting.getAmount()),
                        () -> fittings.add(fitting));
    }

    /**
     * Removes a module from the ships modules.
     *
     * @param fitting the fitting to remove
     */
    public void removeFitting(@Nonnull final AlignedFitting fitting) {
        Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

        fittings.stream()
                .filter(fitting::equals)
                .findFirst()
                .ifPresent(fittings::remove);
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
     * Removes a module from the ammunition fitting.
     *
     * @param fitting the fitting to remove
     */
    public void removeAmmunitionFitting(@Nonnull final AmmunitionFitting fitting) {
        Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

        ammunitionFittings.stream()
                .filter(fitting::equals)
                .findFirst()
                .ifPresent(ammunitionFittings::remove);
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
     * Adds a fitting to the ammunition fitting.
     *
     * @param fitting the fitting to add
     */
    public void addSupportFitting(@Nonnull final SupportFitting fitting) {
        Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

        supportFittings.stream()
                .filter(fitting::equals)
                .findFirst()
                .ifPresentOrElse(
                        alignedFitting -> alignedFitting.setAmount(fitting.getAmount()),
                        () -> supportFittings.add(fitting));
    }

    /**
     * Removes a module from the ammunition fitting.
     *
     * @param fitting the fitting to remove
     */
    public void removeSupportFitting(@Nonnull final SupportFitting fitting) {
        Preconditions.checkNotNull(fitting, "fitting shouldn't be null!");

        supportFittings.stream()
                .filter(fitting::equals)
                .findFirst()
                .ifPresent(supportFittings::remove);
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
    public List<DamagePerRangeAndAlignment> getDamagePerRange(@Nonnull final RangeDefinition boundaries) {
        Preconditions.checkNotNull(boundaries, "boundaries shouldn't be null!");

        return fittings.stream()
                .map(fitting -> fitting.getDamagePerRange(boundaries))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
}
