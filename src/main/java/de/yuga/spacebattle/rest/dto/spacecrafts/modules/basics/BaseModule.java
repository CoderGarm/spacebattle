package de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByOwn;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.rest.dto.enums.EModuleType;
import de.yuga.spacebattle.rest.dto.enums.EShipClassType;
import de.yuga.spacebattle.rest.dto.misc.descriptors.PropertyDescriptor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Nullable
    @JsonProperty
    @Schema(description = "The module's type.")
    private EModuleType moduleType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The specific properties of this module.")
    private PropertyDescriptor propertyDescriptor;

    protected BaseModule() {
    }

    public BaseModule(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule module,
                      @Nonnull final String languageCode) {
        Preconditions.checkNotNull(module, "module must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.idModule = module.getId();
        this.name = module.getName(languageCode);
        this.description = module.getDescription(languageCode);
        this.tonnage = module.getTonnage();
        this.techLevel = module.getTechLevel();
        this.shipClassType = new EShipClassType(module.getShipClassType());
        this.effectValue = module.getEffectValue();
        this.propertyDescriptor = new PropertyDescriptor(module);
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
        this.moduleType = BaseModule.getTypeBy(module);
        this.propertyDescriptor = new PropertyDescriptor(module);
    }

    public BaseModule(@Nonnull final Propulsion module, @Nonnull final String languageCode) {
        Preconditions.checkNotNull(module, "module must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.idModule = module.getId();
        this.name = module.getName(languageCode);
        this.description = module.getDescription(languageCode);
        this.techLevel = module.getTechLevel();
        this.moduleType = new EModuleType(de.yuga.spacebattle.backend.enums.EModuleType.PROPULSION);
        this.propertyDescriptor = new PropertyDescriptor(module);
    }

    @Nullable
    @JsonIgnore
    private static EModuleType getTypeBy(@Nonnull final HasCostsByOwn module) {
        Preconditions.checkNotNull(module, "module shouldn't be null!");

        if (module instanceof Weapon || module instanceof Launcher || module instanceof Missile) {
            return new EModuleType(de.yuga.spacebattle.backend.enums.EModuleType.WEAPON);
        }

        if (module instanceof Armor) {
            return new EModuleType(de.yuga.spacebattle.backend.enums.EModuleType.ARMOR);
        }

        if (module instanceof Sidewall) {
            return new EModuleType(de.yuga.spacebattle.backend.enums.EModuleType.SIDEWALL);
        }

        if (module instanceof ElectronicWarfare) {
            return new EModuleType(de.yuga.spacebattle.backend.enums.EModuleType.ELECTRONIC_WARFARE);
        }
        return null;
    }

    @JsonIgnore
    public int getIdModule() {
        return idModule;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final BaseModule that = (BaseModule) o;

        return new EqualsBuilder().append(idModule, that.idModule).append(technicalTypeName, that.technicalTypeName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(idModule).append(technicalTypeName).toHashCode();
    }
}
