package de.yuga.spacebattle.rest.dto.enums;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

public class HasTypeName {

    @Nonnull
    @Schema(required = true, description = "The type name of this  transformed enum.")
    private final String typeName;

    public HasTypeName() {
        typeName = "";
    }

    public HasTypeName(@Nonnull final Enum<?> enumValue) {
        Preconditions.checkNotNull(enumValue, "enumValue shouldn't be null!");

        this.typeName = enumValue.name();
    }

    @Nonnull
    public String getTypeName() {
        return typeName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final HasTypeName that = (HasTypeName) o;

        return new EqualsBuilder().append(typeName, that.typeName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(typeName).toHashCode();
    }
}
