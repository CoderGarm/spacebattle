package de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics;

import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.researches.Research;

import javax.annotation.Nonnull;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseModuleWithEffectValue extends BaseModule {

    /**
     * The basic value for this module's effect, e.g. it's attack value or shield value.
     */
    private int effectValue;

    protected BaseModuleWithEffectValue() {
    }

    public BaseModuleWithEffectValue(@Nonnull final String name,
                                     @Nonnull final String description,
                                     @Nonnull final Research unlockedThrough,
                                     final int useCapacity,
                                     final int effectValue,
                                     final int techLevel,
                                     @Nonnull final CrewRequirement crewRequirement) {
        super(name, description, unlockedThrough, useCapacity, techLevel, crewRequirement);

        this.effectValue = effectValue;
    }

    public int getEffectValue() {
        return effectValue;
    }
}
