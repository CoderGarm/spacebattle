package de.yuga.spacebattle.rest.dto.misc.descriptors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.ESupportType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class PassiveModuleDescriptor {

    @JsonProperty
    @Schema(required = true, description = "If the module supports an effect or is a standalone effect module.")
    private boolean isStandalone;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "What type of property is supported.")
    private ESupportType supportType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "If the support is increasing or decreasing for the effect.")
    private ECalculationType calculationType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The base effect value of this module.")
    private int effectValue;

    @Nullable
    @JsonProperty
    @Schema(description = "The cargo capacity.")
    private Mass cargoCapacity;

    @Nullable
    @JsonProperty
    @Schema(description = "The amount of passengers.")
    private Integer passengers;

    public PassiveModuleDescriptor() {
    }

    public PassiveModuleDescriptor(@Nonnull final PassiveModule content) {
        Preconditions.checkNotNull(content, "content must not be empty");


        this.isStandalone = !content.isCargo() && !content.isPassenger();
        this.supportType = content.getSupportType();
        this.calculationType = content.getCalculationType();
        this.effectValue = content.getEffectValue();

        if (content.isCargo()) {
            this.cargoCapacity = content.getCargoCapacity();
        }
        if (content.isPassenger()) {
            this.passengers = content.getPassengers();
        }
    }
}
