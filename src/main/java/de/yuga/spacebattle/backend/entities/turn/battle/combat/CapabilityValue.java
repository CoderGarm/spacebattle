package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EModuleType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

@Embeddable
public class CapabilityValue {

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EModuleType moduleType;

    @Nonnull
    @NotNull
    @Column(columnDefinition = "decimal(19, 0)")
    private BigDecimal value;

    public CapabilityValue() {
    }

    public CapabilityValue(@Nonnull final de.yuga.spacebattle.backend.enums.EModuleType moduleType, @Nonnull final BigDecimal value) {
        Preconditions.checkNotNull(moduleType, "moduleType shouldn't be null!");
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        this.moduleType = moduleType;
        this.value = value;
    }

    public CapabilityValue(@Nonnull final Map.Entry<de.yuga.spacebattle.backend.enums.EModuleType, BigDecimal> capability) {
        Preconditions.checkNotNull(capability, "capability shouldn't be null!");

        this.moduleType = capability.getKey();
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

    public void setValue(@Nonnull final BigDecimal value) {
        Preconditions.checkNotNull(value, "value must not be empty");

        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final CapabilityValue that = (CapabilityValue) o;

        return new EqualsBuilder().append(moduleType, that.moduleType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(moduleType).toHashCode();
    }

    @Override
    public String toString() {
        return "moduleType: " + moduleType.name() + ", value: " + value;
    }
}
