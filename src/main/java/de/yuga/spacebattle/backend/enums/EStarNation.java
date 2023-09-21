package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public enum EStarNation {

    MANTICORE,
    HAVEN,
    ANDERMAN,
    SILESIA,
    SOLARIAN_LEAGUE;


    @Nullable
    public static EStarNation getRoleByName(@Nonnull final String starNation) {
        Preconditions.checkNotNull(starNation, "starNation shouldn't be null!");

        return Arrays.stream(EStarNation.values()).filter(e -> e.name().equals(starNation)).findFirst().orElse(null);
    }
}
