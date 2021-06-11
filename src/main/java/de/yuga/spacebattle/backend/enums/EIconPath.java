package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;

public enum EIconPath {

    STATS("icons/stats/"),
    RESOURCES("icons/resources/"),
    HULL("icons/hulls/"),
    BUILDING("icons/buildings/"),
    PLANET("icons/planets/"),
    CREW("icons/crew/"),

    ;

    @NonNull
    final String path;

    EIconPath(@NonNull final String path) {
        Preconditions.checkNotNull(path, "path shouldn't be null!");

        this.path = path;
    }

    @Nonnull
    public String getPath() {
        return path;
    }

    @Nonnull
    private final static String DELIMITER = "_";

    @Nonnull
    private final static String FILE_EXTENSION = "png";

    @Nonnull
    private final static String FILE_EXTENSION_SEPARATOR = ".";

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
        } else if (isForEnum instanceof EHullType) {
            directory = HULL.getPath();
        } else if (isForEnum instanceof EBuildingType) {
            directory = BUILDING.getPath();
        } else if (isForEnum instanceof EPlanetType) {
            directory = PLANET.getPath();
        } else if (isForEnum instanceof EEducationType) {
            directory = CREW.getPath();
        } else {
            throw new NotifySBUserException("Nope, not this, not here!");
        }

        return directory + resolution + icon + DELIMITER + EIconType.COLOR.getComplement() + FILE_EXTENSION_SEPARATOR + FILE_EXTENSION;
    }
}
