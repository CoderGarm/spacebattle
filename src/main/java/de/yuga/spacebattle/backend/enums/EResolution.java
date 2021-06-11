package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public enum EResolution {

    PX16("png16x/"),
    PX24("png24x/"),
    PX32("png32x/"),
    PX64("png64x/");

    @Nonnull
    final String resolution;

    EResolution(@Nonnull final String resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        this.resolution = resolution;
    }

    @Nonnull
    public String getResolution() {
        return resolution;
    }
}
