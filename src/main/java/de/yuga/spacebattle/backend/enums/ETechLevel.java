package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
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
            this.excludedResources = Set.of();
        }
    }

    @Nonnull
    public Set<EResourceType> getExcludedResources() {
        return excludedResources;
    }

    @Nonnull
    public Set<EResourceType> getIncludedResources() {
        return Arrays.stream(EResourceType.valuesWithoutPopulation()).filter(r -> !excludedResources.contains(r)).collect(Collectors.toSet());
    }

    @Nonnull
    public static ETechLevel getTechLevelOf(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");

        if (TECH_I.getIncludedResources().contains(resourceType)) {
            return TECH_I;
        }
        if (TECH_II.getIncludedResources().contains(resourceType)) {
            return TECH_II;
        }
        if (TECH_III.getIncludedResources().contains(resourceType)) {
            return TECH_III;
        }
        throw new NotifyWebUserException("Please implement the missing case.");
    }
}
