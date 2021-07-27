package de.yuga.spacebattle.rest.dto.enums;

import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.HasIconName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

@ApiModel(parent = HasTypeName.class)
public class HasIcon extends HasTypeName {

    @Nonnull
    @ApiModelProperty(required = true, value = "The icon name for this resource.")
    private final String iconName;

    @Nonnull
    @ApiModelProperty(required = true, value = "The folder name for resources.")
    private final String folder;

    public HasIcon() {
        super();
        folder = "";
        iconName = "";
    }

    public <ENUM extends Enum<?> & HasIconName> HasIcon(@Nonnull final ENUM enumValue) {
        super(enumValue);

        iconName = enumValue.getIconName();
        folder = EIconPath.getFolder(enumValue);
    }

    @Nonnull
    public String getIconName() {
        return iconName;
    }

    @Nonnull
    public String getFolder() {
        return folder;
    }
}
