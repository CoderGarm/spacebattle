package de.yuga.spacebattle.backend.entities.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.EResourceDemand;
import de.yuga.spacebattle.backend.enums.ETechLevel;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Simply the entity key.
 */
@MappedSuperclass
public class HasCosts extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ETechLevel techLevel;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private ResourceDeposit costs;

    public HasCosts() {
    }

    public HasCosts(@Nonnull final ETechLevel techLevel, @Nonnull final Class<?> clazz) {
        Preconditions.checkNotNull(techLevel, "techLevel shouldn't be null!");
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");

        this.techLevel = techLevel;
        this.costs = ResourceDepositInitializerCalculator.initializeResourceDeposit(techLevel, EResourceDemand.getByClazz(this.getClass()));
    }

    @Nonnull
    public ETechLevel getTechLevel() {
        return techLevel;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }
}
