package de.yuga.spacebattle.backend.entities.constructables.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ERaceType;
import de.yuga.spacebattle.backend.enums.EResourceSubType;
import de.yuga.spacebattle.backend.validators.ShipValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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

    @Nonnull
    @NotNull(message = "ERaceType should not be null")
    @Enumerated(EnumType.STRING)
    private ERaceType raceType;

    @Nullable
    @NotNull(message = "hull should not be null")
    @ManyToOne
    @JoinColumn(name = "idHull", updatable = false)
    private Hull hull;

    @Nonnull
    @NotNull
    //@Valid
    //@ModuleChecker
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "idModule", referencedColumnName = "idModule")
    @Column(name = "amount")
    @CollectionTable(name = "moduleComposition", joinColumns = @JoinColumn(name = "idShipClass"))
    private final Map<Module, Integer> modules = new HashMap<>();

    @Nullable
    @Transient
    private Map<Module, Integer> possibleModules;

    @Nullable
    @Transient
    private Collection<Hull> possibleHulls;

    public ShipClass() {
    }

    /**
     * The construction costs.
     */
    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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
        this.raceType = owner.getRaceType();
        this.hull = hull;
    }

    @Nonnull
    public User getOwner() {
        return owner;
    }

    public void setOwner(@Nonnull final User owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        this.owner = owner;
        this.raceType = owner.getRaceType();
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");

        this.name = name;
    }

    @Nonnull
    public ERaceType getRaceType() {
        return raceType;
    }

    public void setHull(@Nullable final Hull hull) {
        this.hull = hull;
    }

    @Nullable
    public Hull getHull() {
        return hull;
    }

    @Nonnull
    public Map<Module, Integer> getModules() {
        return modules;
    }

    /**
     * Adds a module to the ships modules.
     * <p>
     * IMPORTANT: This has to be validated later!
     * - The impact on construction points are not validated.
     * - The mandatory {@link EModuleType}s are not validated.
     *
     * @param module the module to add
     */
    public void addModule(@Nonnull final Module module) {
        Preconditions.checkNotNull(module, "module shouldn't be null!");

        if (modules.containsKey(module)) {
            Integer amount = modules.get(module);
            modules.put(module, ++amount);
        } else {
            modules.put(module, 1);
        }
    }

    /**
     * Necessary while vaadin data binding needs a setter for a full set of data and is not able to use an incremental add method.
     * Removes all not longer contained modules and add or replace everything else.
     *
     * @param modules the set of modules
     */
    public void setModules(@Nonnull final Map<Module, Integer> modules) {
        Preconditions.checkNotNull(modules, "modules shouldn't be null!");

        this.modules.clear();
        Map<Module, Integer> includedModules = modules.entrySet().stream()
                .filter(entry -> modules.get(entry.getKey()) > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.modules.putAll(includedModules);
    }

    /**
     * Adds every possible module which is not in {@link ShipClass#modules}.
     *
     * @param modules the modules to add
     */
    public void addModules(@Nonnull final Map<Module, Integer> modules) {
        Preconditions.checkNotNull(modules, "modules shouldn't be null!");

        Map<Module, Integer> notIncludedModules = modules.keySet().stream()
                .filter(module -> !this.modules.containsKey(module))
                .collect(Collectors.toMap(Function.identity(), value -> 0));
        this.modules.putAll(notIncludedModules);
    }

    /**
     * Removes a module from the ships modules.
     * <p>
     * IMPORTANT: This has to be validated later!
     * - The impact on construction points are not validated.
     * - The mandatory {@link EModuleType}s are not validated.
     *
     * @param module the module to remove
     */
    public void removeModule(@Nonnull final Module module) {
        Preconditions.checkNotNull(module, "module shouldn't be null!");

        if (modules.containsKey(module)) {
            Integer amount = modules.get(module);
            if (amount > 1) {
                modules.put(module, --amount);
            } else {
                modules.remove(module);
            }
        }
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    @Nullable
    public Map<Module, Integer> getPossibleModules() {
        return possibleModules;
    }

    public void setPossibleModules(@Nullable final Collection<Module> possibleModules) {
        if (possibleModules == null) {
            return;
        }
        this.possibleModules = possibleModules.stream().collect(Collectors.toMap(Function.identity(), val -> 0));
    }

    @Nullable
    public Collection<Hull> getPossibleHulls() {
        return possibleHulls;
    }

    public void setPossibleHulls(@Nullable final Collection<Hull> possibleHulls) {
        this.possibleHulls = possibleHulls;
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
