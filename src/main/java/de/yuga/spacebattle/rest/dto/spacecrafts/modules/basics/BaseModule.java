package de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.enums.ETechLevel;
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

    @Nonnull
    @Schema(required = true, description = "The level of this module.")
    private ETechLevel techLevel;

    protected BaseModule() {
    }

    public BaseModule(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule baseModule,
                      @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(baseModule, "baseModule shouldn't be null!");

        this.idModule = baseModule.getId();
        this.name = baseModule.getName(languageCode);
        this.description = baseModule.getDescription(languageCode);
        this.useCapacity = baseModule.getUseCapacity();
        this.techLevel = baseModule.getTechLevel();
    }

    public BaseModule(@Nonnull final BaseModuleWithEffectValue baseModuleWithEffectValue,
                      @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(baseModuleWithEffectValue, "baseModuleWithEffectValue shouldn't be null!");

        this.idModule = baseModuleWithEffectValue.getId();
        this.name = baseModuleWithEffectValue.getName(languageCode);
        this.description = baseModuleWithEffectValue.getDescription(languageCode);
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

    @Nonnull
    public ETechLevel getTechLevel() {
        return techLevel;
    }
}
