package de.yuga.spacebattle.rest.dto.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static de.yuga.spacebattle.backend.enums.EIconPath.STATS;

@Schema(description = ".")
public class HasIcon extends HasTypeName {

    @Nonnull
    @Schema(required = true, description = "The icon name for this resource.")
    private final String iconName;

    @Nonnull
    @Schema(required = true, description = "The folder name for resources.")
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

    public HasIcon(@Nonnull final String iconName, @Nonnull final String folder) {
        this.iconName = Preconditions.checkNotNull(iconName, "iconName must not be empty");
        this.folder = Preconditions.checkNotNull(folder, "folder must not be empty");
    }

    @Nonnull
    public String getIconName() {
        return iconName;
    }

    @Nonnull
    public String getFolder() {
        return folder;
    }


    @Nullable
    public static HasIcon getBy(@Nonnull final ETranslationTarget translationTarget) {
        Preconditions.checkNotNull(translationTarget, "translationTarget must not be empty");

        switch (translationTarget) {
            case WEAPON:
            case MISSILE:
            case LAUNCHER:
                return new HasIcon(de.yuga.spacebattle.backend.enums.EModuleType.WEAPON);
            case ARMOR:
                return new HasIcon(de.yuga.spacebattle.backend.enums.EModuleType.ARMOR);
            case ELECTRONIC_WARFARE:
                return new HasIcon(de.yuga.spacebattle.backend.enums.EModuleType.ELECTRONIC_WARFARE);
            case PROPULSION:
                return new HasIcon(de.yuga.spacebattle.backend.enums.EModuleType.PROPULSION);
            case SIDEWALL:
                return new HasIcon(EModuleType.SIDEWALL);
            case BUILDING:
                return new HasIcon(EBuildingType.BUILDING);
            case RESEARCH:
            case PASSIVE_MODULE:
                return new HasIcon("support", STATS.getPath());
            default:
                break;
        }
        return null;
    }
}
