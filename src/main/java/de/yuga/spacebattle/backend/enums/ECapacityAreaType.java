package de.yuga.spacebattle.backend.enums;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ECapacityAreaType {

    OVERALL(null, 1),
    STERN(EWeaponAlignment.STERN, 2),
    BROADSIDE(EWeaponAlignment.BROADSIDE, 3),
    BOW(EWeaponAlignment.BOW, 4),
    MODULE(null, 5);


    @Nullable
    private final EWeaponAlignment alignment;

    private final int orderNo;

    ECapacityAreaType(@Nullable final EWeaponAlignment eWeaponAlignment, final int orderNo) {
        this.alignment = eWeaponAlignment;
        this.orderNo = orderNo;
    }

    @Nullable
    public EWeaponAlignment getAlignment() {
        return alignment;
    }

    @Nonnull
    public static Set<ECapacityAreaType> getValuesWithoutOverall() {
        return Arrays.stream(ECapacityAreaType.values()).filter(c -> c != OVERALL).collect(Collectors.toSet());
    }

    public static int compare(final ECapacityAreaType o1, final ECapacityAreaType o2) {
        return Integer.compare(o1.orderNo, o2.orderNo);
    }
}
