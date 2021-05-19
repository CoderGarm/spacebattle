package de.yuga.spacebattle.backend.entities.constructables.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.enums.EResourceSubType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.backend.validators.ShipValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "ShipClass.getAll", query = "SELECT a FROM ShipClass a"),
        @NamedQuery(name = "ShipClass.getAllByOwner", query = "SELECT a FROM ShipClass a WHERE a.owner = :owner")
})
@Entity
@Table(name = "shipClass",
        uniqueConstraints = @UniqueConstraint(name = "SHIPCLASS_UK", columnNames = {"idOwner", "name"}))
@AttributeOverride(name = "id", column = @Column(name = "idShipClass"))
@ShipValidator
public class ShipClass extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "owner should not be null")
    @ManyToOne(optional = false)
    @JoinColumn(name = "idOwner")
    private User owner;

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
    @JoinColumn(name = "idPropulsion", updatable = false)
    private Propulsion propulsion;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idArmor", updatable = false)
    private Armor armor;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idSidewall", updatable = false)
    private Sidewall sidewall;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idElectronicWarfare", updatable = false)
    private ElectronicWarfare electronicWarfare;

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alignedFitting", joinColumns = @JoinColumn(name = "idShipClass"))
    private final Set<AlignedFitting> fittings = new HashSet<>();

    public ShipClass() {
    }

    /**
     * The construction costs.
     */
    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = new ResourceDeposit(EResourceSubType.COSTS);

    public ShipClass(@Nonnull final User owner,
                     @Nonnull final String name,
                     @Nonnull final Hull hull) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(hull, "hull shouldn't be null!");

        this.owner = owner;
        this.name = name;
        this.hull = hull;
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

        if (hull != null) {
            hull.getCosts().getResources().forEach(clonedDeposit::updateResource);
        }
        if (propulsion != null) {
            propulsion.getCosts().getResources().forEach(clonedDeposit::updateResource);
        }
        if (armor != null) {
            armor.getCosts().getResources().forEach(clonedDeposit::updateResource);
        }
        if (electronicWarfare != null) {
            electronicWarfare.getCosts().getResources().forEach(clonedDeposit::updateResource);
        }
        if (sidewall != null) {
            sidewall.getCosts().getResources().forEach(clonedDeposit::updateResource);
        }
        fittings.forEach(a -> a.getWeapon().getCosts().getResources().forEach(clonedDeposit::updateResource));
        return clonedDeposit;
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
