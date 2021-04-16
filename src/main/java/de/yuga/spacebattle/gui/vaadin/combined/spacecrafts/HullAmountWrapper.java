package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;

import javax.annotation.Nonnull;

/**
 * Wraps a {@link Hull} and it's value.
 */
public class HullAmountWrapper {

    @Nonnull
    private final Hull hull;

    private final int value;

    public HullAmountWrapper(@Nonnull final Hull hull,
                             final int value) {
        Preconditions.checkNotNull(hull, "hull shouldn't be null!");

        this.hull = hull;
        this.value = value;
    }

    public String getValue() {
        return String.valueOf(value);
    }

    @Nonnull
    public Hull getHull() {
        return hull;
    }
}
