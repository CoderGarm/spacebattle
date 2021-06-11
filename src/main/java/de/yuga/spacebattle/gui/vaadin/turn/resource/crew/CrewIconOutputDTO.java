package de.yuga.spacebattle.gui.vaadin.turn.resource.crew;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageMapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a {@link EEducationType}, it's amount and the possible tick output.
 */
public class CrewIconOutputDTO implements ImageMapper {

    @Nonnull
    private final EEducationType educationType;

    private final long amount;

    @Nullable
    private final Long tickOutput;

    public CrewIconOutputDTO(@Nonnull final EEducationType educationType,
                             final long amount,
                             @Nullable final Long tickOutput) {
        Preconditions.checkNotNull(educationType, "planet shouldn't be null!");

        this.educationType = educationType;
        this.amount = amount;
        this.tickOutput = tickOutput;
    }

    @Nonnull
    public EEducationType getEducationType() {
        return educationType;
    }

    public long getAmount() {
        return amount;
    }

    @Nullable
    public Long getTickOutput() {
        return tickOutput;
    }

    /**
     * Returns a String like "500 (50)".
     * <p>
     * There are two possibilities:<br>
     * <br>
     * - without tick output it will return "500"<br>
     * - with tick output it will return "500 (50)"<br>
     * </p>
     *
     * @return the string
     */
    public String getAmountWithDiff() {
        String text = amount + "";
        if (tickOutput != null) {
            text += " (" + tickOutput.toString() + ")";
        }
        return text;
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
