package de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


@Schema(description = ".")
public class BaseModule {

    @Nonnull
    @Schema(required = true, description = "The id of this module.")
    private int idModule;

    @Nonnull
    @Schema(required = true, description = "The name of this module.")
    private String name;

    @Nonnull
    @Schema(required = true, description = "The description of this module.")
    private String description;

    @Schema(required = true, description = "The capacity usage of this module.")
    private int useCapacity;

    @Nullable
    @Schema(description = "The base effect value of this module.")
    private Integer effectValue;

    @Schema(required = true, description = "The level of this module.")
    private int techLevel;

    protected BaseModule() {
    }

    public BaseModule(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule baseModule) {
        Preconditions.checkNotNull(baseModule, "baseModule shouldn't be null!");

        this.idModule = baseModule.getId();
        this.name = baseModule.getName();
        this.description = baseModule.getDescription();
        this.useCapacity = baseModule.getUseCapacity();
        this.techLevel = baseModule.getTechLevel();
    }

    public BaseModule(@Nonnull final BaseModuleWithEffectValue baseModuleWithEffectValue) {
        Preconditions.checkNotNull(baseModuleWithEffectValue, "baseModuleWithEffectValue shouldn't be null!");

        this.idModule = baseModuleWithEffectValue.getId();
        this.name = baseModuleWithEffectValue.getName();
        this.description = baseModuleWithEffectValue.getDescription();
        this.useCapacity = baseModuleWithEffectValue.getUseCapacity();
        this.effectValue = baseModuleWithEffectValue.getEffectValue();
        this.techLevel = baseModuleWithEffectValue.getTechLevel();
    }

    public int getIdModule() {
        return idModule;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    @Nullable
    public Integer getEffectValue() {
        return effectValue;
    }

    public int getTechLevel() {
        return techLevel;
    }
}
