package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByOwn;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.enums.EResourceType.*;

/**
 * Defines which resource types must not be consumed to create a thing of it's class.
 */
public enum EResourceDemand {

    BUILDING(Building.class, ORBITAL_CONSTRUCTION, EResourceType.RESEARCH),
    RESEARCH(Research.class, ORBITAL_CONSTRUCTION, CONSTRUCTION, CREDITS, METALORE, HEAVY_METALS, RARE_ELEMENTS, POPULATION),
    WEAPON_SYSTEM(HasCostsByOwn.class, CONSTRUCTION, EResourceType.RESEARCH),
    PASSIVE_MODULE(PassiveModule.class, CONSTRUCTION, EResourceType.RESEARCH),
    BASE_MODULE(BaseModule.class, PASSIVE_MODULE.getOverrideResources().toArray(EResourceType[]::new)),
    ;

    @Nonnull
    private final Class<?> clazz;

    /**
     * Defines the resource types which must be passed by generating costs for this type.
     */
    @Nonnull
    private final Set<EResourceType> overrideResources;

    EResourceDemand(@Nonnull final Class<?> clazz, @Nullable final EResourceType... excludedResources) {

        this.clazz = clazz;
        if (excludedResources != null) {
            this.overrideResources = Arrays.stream(excludedResources).collect(Collectors.toSet());
        } else {
            this.overrideResources = new HashSet<>();
        }
    }

    @Nonnull
    public static EResourceDemand getByClazz(@Nonnull final Class<? extends HasCosts> clazz) {
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");

        return Arrays.stream(EResourceDemand.values())
                .filter(e -> e.clazz.isAssignableFrom(clazz))
                .findFirst()
                .orElseThrow(() -> new NotifyWebUserException("Something was wrong here. But it shouldn't."));
    }

    @Nonnull
    public Set<EResourceType> getOverrideResources() {
        return overrideResources;
    }
}
