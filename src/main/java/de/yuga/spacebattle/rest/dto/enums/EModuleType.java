package de.yuga.spacebattle.rest.dto.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class EModuleType extends HasIcon {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The enum name")
    private final String moduleName;

    public EModuleType() {
        super();
        this.moduleName = "";
    }

    public EModuleType(@Nonnull final de.yuga.spacebattle.backend.enums.EModuleType moduleType) {
        super(moduleType);

        this.moduleName = moduleType.getName();
    }

    @Override
    @JsonIgnore
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final EModuleType that = (EModuleType) o;

        return new EqualsBuilder().append(moduleName, that.moduleName).isEquals();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(moduleName).toHashCode();
    }

    @Nonnull
    @JsonIgnore
    public String getModuleName() {
        return moduleName;
    }
}
