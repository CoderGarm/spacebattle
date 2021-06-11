package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;

import javax.annotation.Nonnull;
import java.util.Arrays;

public enum EEntityType {

    DEFAULT("DEFAULT", String.class),
    BUILDING("Building", Building.class),
    HULL("Hull", Hull.class),
    MODULE("Module", BaseModule.class),
    ;

    @Nonnull
    private String type;

    @Nonnull
    private Class<?> clazz;

    EEntityType(@Nonnull final String type, @Nonnull final Class<?> clazz) {
        Preconditions.checkNotNull(type, "type shouldn't be null!");
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");

        this.type = type;
        this.clazz = clazz;
    }

    @Nonnull
    public String getType() {
        return type;
    }

    @Nonnull
    public Class<?> getClazz() {
        return clazz;
    }

    @Nonnull
    public static EEntityType getTypeByClazz(@Nonnull final Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");

        return Arrays.stream(EEntityType.values()).filter(eeType -> eeType.getClazz().isAssignableFrom(clazz)).findFirst().orElse(DEFAULT);
    }
}
