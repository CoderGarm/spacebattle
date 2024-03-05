package de.yuga.spacebattle.backend.enums.events;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;

public enum EGameEvent {

    SEASON_2("SEASON_2"),
    WAR_HARVEST_23("WAR_HARVEST_23"),
    TOURNAMENT_FOR_HONOR_24("TOURNAMENT_FOR_HONOR_24"),
    ;

    private final String name;

    EGameEvent(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");

        this.name = name;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    public static EGameEvent getRoleByName(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name must not be empty");

        return Arrays.stream(EGameEvent.values()).filter(e -> e.getName().equals(name)).findFirst().orElse(null);
    }
}
