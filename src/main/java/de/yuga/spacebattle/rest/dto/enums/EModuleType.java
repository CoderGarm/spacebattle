package de.yuga.spacebattle.rest.dto.enums;

import io.swagger.annotations.ApiModel;

import javax.annotation.Nonnull;

@ApiModel(parent = HasIcon.class)
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
