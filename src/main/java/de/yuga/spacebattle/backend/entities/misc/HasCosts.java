package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceDemand;
import de.yuga.spacebattle.backend.enums.ETechLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public class HasCosts extends HasName {

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private ResourceDeposit costs;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ETechLevel techLevel;

    public HasCosts() {
    }

    public HasCosts(@Nonnull final Translation translatableName,
                    @Nonnull final Translation translatableDescription,
                    @Nonnull final ETechLevel techLevel,
                    @Nullable final Integer tonnage,
                    @Nonnull final Class<?> clazz) {
        super(translatableName, translatableDescription, techLevel, clazz);
        Preconditions.checkNotNull(translatableName, "translatableName must not be empty");
        Preconditions.checkNotNull(translatableDescription, "translatableDescription must not be empty");
        Preconditions.checkArgument(translatableName.getLanguageCode().equals(Translation.DEFAULT_LANGUAGE), "translatableName: common language must be english");
        Preconditions.checkArgument(translatableDescription.getLanguageCode().equals(Translation.DEFAULT_LANGUAGE), "translatableDescription: common language must be english");
        Preconditions.checkNotNull(techLevel, "techLevel shouldn't be null!");
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");

        this.costs = ResourceDepositInitializerCalculator.initializeCosts(techLevel, tonnage, EResourceDemand.getByClazz(this.getClass()));
        this.techLevel = techLevel;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    @Nonnull
    public ETechLevel getTechLevel() {
        return techLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamedTechLevel)) return false;

        NamedTechLevel module = (NamedTechLevel) o;
        return id == module.id;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }
}
