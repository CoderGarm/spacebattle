package de.yuga.spacebattle.backend.enums;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.enums.EResourceType.HEAVY_METALS;
import static de.yuga.spacebattle.backend.enums.EResourceType.RARE_ELEMENTS;

public enum ETechLevel {

    TECH_I(RARE_ELEMENTS, HEAVY_METALS),
    TECH_II(HEAVY_METALS),
    TECH_III();

    @Nonnull
    private final Set<EResourceType> excludedResources;

    ETechLevel(@Nullable final EResourceType... excludedResources) {

        if (excludedResources != null) {
            this.excludedResources = Arrays.stream(excludedResources).collect(Collectors.toSet());
        } else {
            this.excludedResources = new HashSet<>();
        }
    }

    @Nonnull
    public Set<EResourceType> getExcludedResources() {
        return excludedResources;
    }
}
