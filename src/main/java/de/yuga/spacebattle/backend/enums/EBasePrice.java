package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.util.Arrays;

public enum EBasePrice {

    TYPE_I(ETechLevel.TECH_I, 10),
    TYPE_II(ETechLevel.TECH_II, 20),
    TYPE_III(ETechLevel.TECH_III, 30);

    @Nonnull
    private final ETechLevel techLevel;

    private final int basePrice;

    EBasePrice(@Nonnull final ETechLevel eTechLevel, final int basePrice) {
        this.techLevel = Preconditions.checkNotNull(eTechLevel, "eTechLevel must not be empty");
        this.basePrice = basePrice;
    }

    @Nonnull
    public static EBasePrice get(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");
        Preconditions.checkArgument(EResourceType.isCollectable(resourceType), "resourceType must be a collectable");

        return Arrays.stream(EBasePrice.values()).filter(b -> b.techLevel.getIncludedResources().contains(resourceType)).findFirst().orElseThrow(() -> new NotifyWebUserException("The resource isn't mapped: " + resourceType));
    }
}
