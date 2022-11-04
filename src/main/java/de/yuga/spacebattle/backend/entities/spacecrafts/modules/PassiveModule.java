package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.ESupportType;
import de.yuga.spacebattle.backend.enums.ETechLevel;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "PassiveModule.getAll", query = "SELECT a FROM PassiveModule a"),
        @NamedQuery(name = "PassiveModule.getAllByResearches", query = "SELECT a FROM PassiveModule a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "passiveModule")
@AttributeOverride(name = "id", column = @Column(name = "idPassiveModule"))
public class PassiveModule extends BaseModuleWithEffectValue {

    /**
     * Defines what kind of property is supported.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ESupportType supportType;

    /**
     * Defines if the support is increasing or decreasing the property.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ECalculationType calculationType;

    public PassiveModule() {
    }

    public PassiveModule(@Nonnull final String name,
                         @Nonnull final String description,
                         @Nonnull final Research unlockedThrough,
                         final int useCapacity,
                         final int effectValue,
                         @Nonnull final EHullType hullType,
                         @Nonnull final ETechLevel techLevel,
                         @Nonnull final ESupportType supportType,
                         @Nonnull final ECalculationType calculationType,
                         @Nonnull final CrewRequirement crewRequirement) {
        super(name, description, unlockedThrough, useCapacity, effectValue, hullType, techLevel, crewRequirement, PassiveModule.class);

        this.supportType = supportType;
        this.calculationType = calculationType;
    }

    @Nonnull
    public ESupportType getSupportType() {
        return supportType;
    }

    @Nonnull
    public ECalculationType getCalculationType() {
        return calculationType;
    }
}
