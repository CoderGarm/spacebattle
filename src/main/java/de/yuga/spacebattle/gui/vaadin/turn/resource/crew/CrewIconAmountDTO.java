package de.yuga.spacebattle.gui.vaadin.turn.resource.crew;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageMapper;

import javax.annotation.Nonnull;

/**
 * Wraps a {@link EEducationType} and an amount.
 */
public class CrewIconAmountDTO implements ImageMapper {

    @Nonnull
    private final EEducationType educationType;

    private final long amount;

    public CrewIconAmountDTO(@Nonnull final EEducationType educationType, final long amount) {
        Preconditions.checkNotNull(educationType, "planet shouldn't be null!");

        this.educationType = educationType;
        this.amount = amount;
    }

    @Nonnull
    public EEducationType getEducationType() {
        return educationType;
    }

    public long getAmount() {
        return amount;
    }

    public String getAmountAsString() {
        return String.valueOf(amount);
    }

    @Override
    public String getAlternativeText() {
        return getTitleText();
    }

    @Override
    public String getTitleText() {
        return getEducationType().name();
    }

    @Override
    public String getPath(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final EEducationType educationType = getEducationType();
        final String iconName = educationType.getIconName();
        return EIconPath.getPath(educationType, iconName, resolution.getResolution());
    }
}
