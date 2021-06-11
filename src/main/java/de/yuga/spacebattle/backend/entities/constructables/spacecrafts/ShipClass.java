package de.yuga.spacebattle.backend.entities.constructables.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.validators.ShipValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "ShipClass.getAll", query = "SELECT a FROM ShipClass a WHERE a.isDeleted = false"),
        @NamedQuery(name = "ShipClass.getAllByOwner", query = "SELECT a FROM ShipClass a WHERE a.owner = :owner AND a.isDeleted = false"),
        @NamedQuery(name = "ShipClass.getAllLatestByOwner", query = "SELECT a FROM ShipClass a WHERE a.owner = :owner AND a.successor IS NULL AND a.isDeleted = false")
})
@Entity
@Table(name = "shipClass")
@AttributeOverride(name = "id", column = @Column(name = "idShipClass"))
@ShipValidator
public class ShipClass extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "owner should not be null")
    @ManyToOne(optional = false)
    @JoinColumn(name = "idOwner")
    private User owner;

    /**
     * Must not unique or unique for user because at least the ships in a ancestry row could have the same names.
     */
    @Nonnull
    @NotNull(message = "name should not be null")
    @Size(min = 3, max = 30, message = "name should be between 3 and 30 characters long")
    private String name;

    @Nullable
    @NotNull(message = "hull should not be null")
    @ManyToOne
    @JoinColumn(name = "idHull", updatable = false)
    private Hull hull;

    @Nullable
    @NotNull(message = "propulsion must not be null")
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
     * The construction costs.
     */
    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = new ResourceDeposit(EResourceSubType.COSTS);

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

    /**
     * Marks if the class is deleted.
     */
    private boolean isDeleted = false;

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

    public void setOwner(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.owner = owner;
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

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    public ResourceDeposit getCostsOverall() {
        final ResourceDeposit clonedDeposit = new ResourceDeposit(costs);

        final Set<SupportFitting> supportFittings = getSupportFittings();
        final Map<ESupportType, SupportFitting> supportTypeToModule = supportFittings.stream()
                .collect(Collectors.toMap(e -> e.getPassiveModule().getSupportType(), Function.identity()));

        if (hull != null) {
            final Map<EResourceType, BigDecimal> resources = hull.getCosts().getResources();
            updateCosts(clonedDeposit, supportTypeToModule, resources);
        }
        if (propulsion != null) {
            Map<EResourceType, BigDecimal> resources = propulsion.getCosts().getResources();
            updateCosts(clonedDeposit, supportTypeToModule, resources);
        }
        if (armor != null) {
            Map<EResourceType, BigDecimal> resources = armor.getCosts().getResources();
            updateCosts(clonedDeposit, supportTypeToModule, resources);
        }
        if (electronicWarfare != null) {
            Map<EResourceType, BigDecimal> resources = electronicWarfare.getCosts().getResources();
            updateCosts(clonedDeposit, supportTypeToModule, resources);
        }
        if (sidewall != null) {
            Map<EResourceType, BigDecimal> resources = sidewall.getCosts().getResources();
            updateCosts(clonedDeposit, supportTypeToModule, resources);
        }
        fittings.forEach(a -> a.getWeapon().getCosts().getResources().forEach(clonedDeposit::updateResource));
        supportFittings.forEach(s -> {
            final Map<EResourceType, BigDecimal> resources = s.getPassiveModule().getCosts().getResources();
            int amount = s.getAmount();
            for (; amount >= 0; amount--) {
                updateCosts(clonedDeposit, supportTypeToModule, resources);
            }
        });
        ammunitionFittings.forEach(s -> {
            final Map<EResourceType, BigDecimal> resources = s.getAmmunitionModule().getCosts().getResources();
            int amount = s.getAmount();
            for (; amount >= 0; amount--) {
                updateCosts(clonedDeposit, supportTypeToModule, resources);
            }
        });
        supportFittings.forEach(s -> {
            final Map<EResourceType, BigDecimal> resources = s.getPassiveModule().getCosts().getResources();
            int amount = s.getAmount();
            for (; amount >= 0; amount--) {
                updateCosts(clonedDeposit, supportTypeToModule, resources);
            }
        });
        return clonedDeposit;
    }

    /**
     * Calculates and sets the costs by a possible existent support module.
     *
     * @param clonedDeposit       the deposit to update
     * @param supportTypeToModule the support module map
     * @param resources           the resource map
     */
    private void updateCosts(ResourceDeposit clonedDeposit, Map<ESupportType, SupportFitting> supportTypeToModule, Map<EResourceType, BigDecimal> resources) {
        resources.forEach((resourceType, amount) -> {
            final SupportFitting supportFitting = supportTypeToModule.get(ESupportType.getByValue(resourceType));
            final double absoluteValueAsFactor = supportFitting != null ? supportFitting.getAbsoluteValueAsFactor() : 1;
            final BigDecimal effectiveAmount = amount.multiply(new BigDecimal(absoluteValueAsFactor), ResourceDeposit.mathContext);
            clonedDeposit.updateResource(resourceType, effectiveAmount);
        });
    }

    @Nonnull
    public Set<AlignedFitting> getFittings() {
        return fittings;
    }

    @Nonnull
    public Set<AlignedFitting> getFittingByType(@Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        return fittings.stream()
                .filter(e -> weaponType == e.getWeapon().getWeaponType())
                .collect(Collectors.toSet());
    }

    @Nonnull
    public Set<AlignedFitting> getFittingByTypeAndAlignment(@Nonnull final EWeaponType weaponType,
                                                            @Nonnull final EWeaponAlignment weaponAlignment) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment shouldn't be null!");

        return fittings.stream()
                .filter(e -> weaponType == e.getWeapon().getWeaponType() && weaponAlignment == e.getWeaponAlignment())
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
     * Removes all not longer contained modules and add or replace everything else.
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
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

    @Nullable
    public ElectronicWarfare getElectronicWarfare() {
        return electronicWarfare;
    }

    public void setElectronicWarfare(@Nullable ElectronicWarfare electronicWarfare) {
        this.electronicWarfare = electronicWarfare;
    }
}
