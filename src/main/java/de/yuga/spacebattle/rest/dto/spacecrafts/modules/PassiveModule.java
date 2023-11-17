package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.ESupportType;
import de.yuga.spacebattle.rest.dto.WithCosts;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class PassiveModule extends WithCosts<PassiveModule> {

    @Nonnull
    @Schema(required = true, description = "The basic values of this module.")
    private BaseModule baseModule;

    @Nonnull
    @Schema(required = true, description = "What type of property is supported.")
    private ESupportType supportType;

    /**
     * Defines if the support an increase or a decrease of the property.
     */
    @Nonnull
    @Schema(required = true, description = "If the support is increasing or decreasing.")
    private ECalculationType calculationType;

    public PassiveModule() {
    }

    public PassiveModule(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule passiveModule,
                         @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(passiveModule, "passiveModule shouldn't be null!");

        this.baseModule = new BaseModule(passiveModule, languageCode);
        this.supportType = passiveModule.getSupportType();
        this.calculationType = passiveModule.getCalculationType();
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }

    public void setBaseModule(@Nonnull BaseModule baseModule) {
        this.baseModule = baseModule;
    }

    @Nonnull
    public ESupportType getSupportType() {
        return supportType;
    }

    public void setSupportType(@Nonnull ESupportType supportType) {
        this.supportType = supportType;
    }

    @Nonnull
    public ECalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(@Nonnull ECalculationType calculationType) {
        this.calculationType = calculationType;
    }
}
