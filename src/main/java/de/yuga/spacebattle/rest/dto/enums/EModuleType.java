package de.yuga.spacebattle.rest.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class EModuleType extends HasIcon {

    @Nonnull
    private final String moduleName;

    public EModuleType() {
        super();
        this.moduleName = "";
    }

    public EModuleType(@Nonnull final de.yuga.spacebattle.backend.enums.EModuleType moduleType) {
        super(moduleType);

        this.moduleName = moduleType.getName();
    }

    @Nonnull
    public String getModuleName() {
        return moduleName;
    }
}
