package de.yuga.spacebattle.entities.constructables.spacecrafts;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.AbstractEntityKey;
import de.yuga.spacebattle.entities.ResourceDeposit;
import de.yuga.spacebattle.entities.account.User;
import de.yuga.spacebattle.entities.spacecrafts.Hull;
import de.yuga.spacebattle.entities.spacecrafts.Module;
import de.yuga.spacebattle.enums.EModuleType;
import de.yuga.spacebattle.enums.ERaceType;
import de.yuga.spacebattle.enums.EResourceSubType;
import de.yuga.spacebattle.validators.ShipValidator;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@NamedQueries({
        @NamedQuery(name = "ShipClass.getAll", query = "SELECT a FROM ShipClass a")
})
@Entity
@Table(name = "shipclass", uniqueConstraints = @UniqueConstraint(columnNames = {"idOwner", "name"}))
@AttributeOverride(name = "id", column = @Column(name = "idShipclass"))
@ShipValidator
public class ShipClass extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "owner should not be null")
    @ManyToOne
    @JoinColumn(name = "idOwner")
    private User owner;

    @Nonnull
    @NotNull(message = "name should not be null")
    @Size(min = 3, max = 30, message = "name should not be null")
    private String name;

    @Nonnull
    @NotNull(message = "ERaceType should not be null")
    @Enumerated(EnumType.STRING)
    private ERaceType raceType;

    @Nonnull
    @NotNull(message = "hull should not be null")
    @ManyToOne
    @JoinColumn(name = "idHull", updatable = false)
    private Hull hull;

    @Nonnull
    @NotNull
    @Valid
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "idModule", referencedColumnName = "idModule")
    @Column(name = "amount")
    @CollectionTable(name = "modulecomposition", joinColumns = @JoinColumn(name = "idShipclass"))
    private final Map<Module, Integer> modules = new HashMap<>();

    public ShipClass() {
    }

    /**
     * The construction costs.
     */
    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idCosts")
    private final ResourceDeposit costs = new ResourceDeposit(EResourceSubType.COSTS);

    public ShipClass(@Nonnull final User owner,
                     @Nonnull final String name,
                     @Nonnull final Hull hull) {
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

    @Nonnull
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

    @Override
    public String toString() {

        String moduleString = "";//modules != null && !modules.isEmpty() ? ",\n\tmodules=\n\t" + modules.stream().map(Module::toString).collect(Collectors.joining("\n\t\t")) : "";

        return "Ship{" +
                "name='" + name + "'" +
                ", raceType=" + raceType +
                ", shipLevel=" + "is hull now" +
                moduleString +
                "\n}\n";
    }
}
