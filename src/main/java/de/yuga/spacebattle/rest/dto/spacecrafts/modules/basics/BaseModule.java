package de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByOwn;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.rest.dto.enums.EShipClassType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class BaseModule {

    @JsonProperty
    @Schema(required = true, description = "The id of this module.")
    private int idModule;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this module.")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(description = "The technical type name of this module.")
    private String technicalTypeName;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The description of this module.")
    private String description;

    @JsonProperty
    @Schema(description = "The capacity usage of this module.")
    private Mass tonnage;

    @Nullable
    @JsonProperty
    @Schema(description = "The base effect value of this module.")
    private Integer effectValue;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The level of this module.")
    private ETechLevel techLevel;

    @Nonnull
    @JsonProperty
    @Schema(description = "The intended hull type of this module.")
    private EShipClassType shipClassType;

    protected BaseModule() {
    }

    public BaseModule(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule module,
                      @Nonnull final String languageCode) {
        Preconditions.checkNotNull(module, "module shouldn't be null!");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.idModule = module.getId();
        this.name = module.getName(languageCode);
        this.description = module.getDescription(languageCode);
        this.tonnage = module.getTonnage();
        this.techLevel = module.getTechLevel();
        this.shipClassType = new EShipClassType(module.getShipClassType());
    }

    public BaseModule(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue module,
                      @Nonnull final String languageCode) {
        this((de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule) module, languageCode);

        this.effectValue = module.getEffectValue();
    }

    public BaseModule(@Nonnull final HasCostsByOwn module,
                      @Nonnull final String languageCode) {
        Preconditions.checkNotNull(module, "module shouldn't be null!");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.idModule = module.getId();
        this.name = module.getName(languageCode);
        this.technicalTypeName = module.getTechnicalTypeName();
        this.description = module.getDescription(languageCode);
        this.techLevel = module.getTechLevel();
        this.tonnage = module.getTonnage();
        this.effectValue = module.getEffectValue();
        this.shipClassType = new EShipClassType(module.getShipClassType());
    }

    public BaseModule(@Nonnull final Propulsion module, @Nonnull final String languageCode) {
        Preconditions.checkNotNull(module, "module must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.idModule = module.getId();
        this.name = module.getName(languageCode);
        this.description = module.getDescription(languageCode);
        this.techLevel = module.getTechLevel();
    }

    @JsonIgnore
    public int getIdModule() {
        return idModule;
    }
}
