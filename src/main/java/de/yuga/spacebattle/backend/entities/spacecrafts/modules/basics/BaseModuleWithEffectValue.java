package de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics;

import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;

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
                                     @Nonnull final EHullType hullType,
                                     @Nonnull final ETechLevel techLevel,
                                     @Nonnull final CrewRequirement crewRequirement,
                                     @Nonnull final Class<?> clazz) {
        super(name, description, unlockedThrough, useCapacity, hullType, techLevel, crewRequirement, clazz);

        this.effectValue = effectValue;
    }

    public int getEffectValue() {
        return effectValue;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setEffectValue(final int effectValue) {
        this.effectValue = effectValue;
    }
}
