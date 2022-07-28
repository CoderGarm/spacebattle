package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.enums.EModuleType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Map;

@Schema(description = ".")
public class CapabilityValue {

    @Nonnull
    @Schema(required = true, description = "The value's type.")
    private EModuleType moduleType;

    @Nonnull
    @Schema(required = true, description = "The value.")
    private BigDecimal value;

    public CapabilityValue() {
    }

    public CapabilityValue(@Nonnull final de.yuga.spacebattle.backend.enums.EModuleType moduleType, @Nonnull final BigDecimal value) {
        Preconditions.checkNotNull(moduleType, "moduleType shouldn't be null!");
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        this.moduleType = new EModuleType(moduleType);
        this.value = value;
    }

    public CapabilityValue(@Nonnull final Map.Entry<de.yuga.spacebattle.backend.enums.EModuleType, BigDecimal> capability) {
        Preconditions.checkNotNull(capability, "capability shouldn't be null!");

        this.moduleType = new EModuleType(capability.getKey());
        this.value = capability.getValue();
    }

    @Nonnull
    public EModuleType getModuleType() {
        return moduleType;
    }

    @Nonnull
    public BigDecimal getValue() {
        return value;
    }
}
