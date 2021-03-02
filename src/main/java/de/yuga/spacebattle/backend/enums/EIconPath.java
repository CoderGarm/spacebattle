package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;

public enum EIconPath {

    STATS("icons/stats/"),
    RESOURCES("icons/resources/"),
    HULL("icons/hulls/");

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
    public static String getPath(@Nonnull final String directory, @Nonnull final String icon) {
        return directory + EResolution.PX32.getResolution() + icon + DELIMITER + EIconType.COLOR.getComplement() + FILE_EXTENSION_SEPARATOR + FILE_EXTENSION;
    }
}
