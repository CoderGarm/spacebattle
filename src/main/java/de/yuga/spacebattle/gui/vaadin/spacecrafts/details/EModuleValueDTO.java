package de.yuga.spacebattle.gui.vaadin.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageMapper;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * Wraps a {@link EModuleType} and it's value.
 */
public class EModuleValueDTO implements ImageMapper {

    @Nonnull
    private final EModuleType eModuleType;

    @Nonnull
    private final BigDecimal value;

    public EModuleValueDTO(@Nonnull final EModuleType eModuleType,
                           @Nonnull final BigDecimal value) {
        Preconditions.checkNotNull(eModuleType, "eModuleType shouldn't be null!");
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        this.eModuleType = eModuleType;
        this.value = value;
    }

    public String getValue() {
        return value.toString();
    }

    @Nonnull
    public EModuleType getModuleType() {
        return eModuleType;
    }

    @Override
    public String getAlternativeText() {
        return getModuleType().getName();
    }

    @Override
    public String getTitleText() {
        return getModuleType().getName();
    }

    @Override
    public String getPath(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final EModuleType resourceType = getModuleType();
        final String iconName = resourceType.getIconName();
        return EIconPath.getPath(resourceType, iconName, resolution.getResolution());
    }
}
