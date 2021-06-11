package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Provides the mapping between a single enum and it's corresponding database column and a property which is modified
 * by the corresponding e.g. {@link PassiveModule} or {@link Building}.
 */
public enum ESupportType {


    /**
     * A module which supports this will increase the effective value for this property.
     */
    WEAPON(EModuleType.WEAPON),

    /**
     * A module which supports this will increase the effective value for this property.
     */
    ARMOR(EModuleType.ARMOR),

    /**
     * A module which supports this will increase the effective value for this property.
     */
    SHIELD(EModuleType.SHIELD),

    /**
     * A module which supports this will increase the effective value for this property.
     */
    PROPULSION(EModuleType.PROPULSION),

    /**
     * A module which supports this will increase the effective value for this property.
     */
    FTLPROPULSION(EModuleType.FTLPROPULSION),

    /**
     * A module which supports this will increase the effective value for this property.
     */
    ELECTRONIC_WARFARE(EModuleType.ELECTRONIC_WARFARE),

    /**
     * A module which supports this will reduce the costs for this resource.
     */
    CREDITS(EResourceType.CREDITS),

    /**
     * A module which supports this will reduce the costs for this resource.
     */
    METALORE(EResourceType.METALORE),

    /**
     * A module which supports this will reduce the costs for this resource.
     */
    RARE_ELEMENTS(EResourceType.RARE_ELEMENTS),

    /**
     * A module which supports this will reduce the costs for this resource.
     */
    HEAVY_METALS(EResourceType.HEAVY_METALS),

    /**
     * A module which supports the population will reduce the general amount of crew members for a ship.
     */
    POPULATION(EResourceType.POPULATION),
    ;

    private final Enum<?> modifiedProperty;

    ESupportType(@Nonnull final Enum<?> value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        modifiedProperty = value;
    }

    public Enum<?> getModifiedProperty() {
        return modifiedProperty;
    }

    @Nullable
    public static ESupportType getByValue(@Nonnull final Enum<?> value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        return Arrays.stream(ESupportType.values()).filter(e -> value == e.getModifiedProperty()).findFirst().orElse(null);
    }
}
