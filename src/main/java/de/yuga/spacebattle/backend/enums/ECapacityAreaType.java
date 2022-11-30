package de.yuga.spacebattle.backend.enums;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ECapacityAreaType {

    OVERALL(null),
    BOW(EWeaponAlignment.BOW),
    STERN(EWeaponAlignment.STERN),
    BROADSIDE(EWeaponAlignment.BROADSIDE),
    MODULE(null);

    @Nullable
    private final EWeaponAlignment alignment;

    ECapacityAreaType(@Nullable final EWeaponAlignment eWeaponAlignment) {
        this.alignment = eWeaponAlignment;
    }

    @Nullable
    public EWeaponAlignment getAlignment() {
        return alignment;
    }

    @Nonnull
    public static Set<ECapacityAreaType> getValuesWithoutOverall() {
        return Arrays.stream(ECapacityAreaType.values()).filter(c -> c != OVERALL).collect(Collectors.toSet());
    }
}
