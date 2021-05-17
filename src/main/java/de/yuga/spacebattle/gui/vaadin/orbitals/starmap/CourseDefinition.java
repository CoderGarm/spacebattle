package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;

public class CourseDefinition {

    @Nonnull
    private final Orbit startOrbit;

    @Nonnull
    private final Orbit targetOrbit;

    public CourseDefinition(@Nonnull final Orbit startOrbit, @Nonnull final Orbit targetOrbit) {
        Preconditions.checkNotNull(startOrbit, "startOrbit shouldn't be null!");
        Preconditions.checkNotNull(targetOrbit, "targetOrbit shouldn't be null!");

        this.startOrbit = startOrbit;
        this.targetOrbit = targetOrbit;
    }

    @Nonnull
    public Orbit getStartOrbit() {
        return startOrbit;
    }

    @Nonnull
    public Orbit getTargetOrbit() {
        return targetOrbit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseDefinition)) return false;

        CourseDefinition that = (CourseDefinition) o;

        if (!startOrbit.equals(that.startOrbit)) return false;
        return targetOrbit.equals(that.targetOrbit);
    }

    @Override
    public int hashCode() {
        int result = startOrbit.hashCode();
        result = 31 * result + targetOrbit.hashCode();
        return result;
    }
}
