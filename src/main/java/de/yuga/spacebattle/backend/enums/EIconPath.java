package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;

public enum EIconPath {

    STATS("icons/stats/"),
    RESOURCES("icons/resources/"),
    HULL("icons/hulls/"),
    BUILDING("icons/buildings/"),
    PLANET("icons/planets/"),
    STAR("icons/stars/"),
    CREW("icons/crew/"),

    ;

    @Nonnull
    final String path;

    EIconPath(@Nonnull final String path) {
        Preconditions.checkNotNull(path, "path shouldn't be null!");

        this.path = path;
    }

    @Nonnull
    public String getPath() {
        return path;
    }

    @Nonnull
    private static final String DELIMITER = "_";

    @Nonnull
    private static final String FILE_EXTENSION = "png";

    @Nonnull
    private static final String FILE_EXTENSION_SEPARATOR = ".";

    @Nonnull
    public static <ENUM extends Enum<?> & HasIconName> String getFolder(@Nonnull final ENUM isForEnum) {
        Preconditions.checkNotNull(isForEnum, "isForEnum shouldn't be null!");

        final String directory;
        if (isForEnum instanceof EResourceType) {
            directory = RESOURCES.getPath();
        } else if (isForEnum instanceof EModuleType) {
            directory = STATS.getPath();
        } else if (isForEnum instanceof EShipClassType) {
            directory = HULL.getPath();
        } else if (isForEnum instanceof EBuildingType) {
            directory = BUILDING.getPath();
        } else if (isForEnum instanceof EPlanetClassType) {
            directory = PLANET.getPath();
        } else if (isForEnum instanceof EEducationType) {
            directory = CREW.getPath();
        } else if (isForEnum instanceof EStarClassType) {
            directory = STAR.getPath();
        } else {
            throw new NotifyWebUserException("Nope, not this, not here!");
        }

        return directory;
    }

    @Nonnull
    public static String getPath(@Nonnull final Enum<?> isForEnum, @Nonnull final String icon, @Nonnull final String resolution) {
        Preconditions.checkNotNull(isForEnum, "isForEnum shouldn't be null!");
        Preconditions.checkNotNull(icon, "icon shouldn't be null!");
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final String directory;
        if (isForEnum instanceof EResourceType) {
            directory = RESOURCES.getPath();
        } else if (isForEnum instanceof EModuleType) {
            directory = STATS.getPath();
        } else if (isForEnum instanceof EShipClassType) {
            directory = HULL.getPath();
        } else if (isForEnum instanceof EBuildingType) {
            directory = BUILDING.getPath();
        } else if (isForEnum instanceof EPlanetClassType) {
            directory = PLANET.getPath();
        } else if (isForEnum instanceof EEducationType) {
            directory = CREW.getPath();
        } else if (isForEnum instanceof EStarClassType) {
            directory = STAR.getPath();
        } else {
            throw new NotifyWebUserException("Nope, not this, not here!");
        }

        return directory + resolution + icon + DELIMITER + EIconType.COLOR.getComplement() + FILE_EXTENSION_SEPARATOR + FILE_EXTENSION;
    }
}
