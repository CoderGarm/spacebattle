package de.yuga.spacebattle.rest.dto.enums;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

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
}
