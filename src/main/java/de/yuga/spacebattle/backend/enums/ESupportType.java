package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Provides the mapping between a single enum and it's corresponding database column and a property which is modified
 * by the corresponding {@link PassiveModule}.
 */
public enum ESupportType {

    WEAPON(EModuleType.WEAPON),
    ARMOR(EModuleType.ARMOR),
    SHIELD(EModuleType.SHIELD),
    PROPULSION(EModuleType.PROPULSION),
    FTLPROPULSION(EModuleType.FTLPROPULSION),
    ELECTRONIC_WARFARE(EModuleType.ELECTRONIC_WARFARE),
    CREDITS(EResourceType.CREDITS),
    METALORE(EResourceType.METALORE),
    MERCURIUM(EResourceType.MERCURIUM),
    HYPERONIUM(EResourceType.HYPERONIUM),
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
