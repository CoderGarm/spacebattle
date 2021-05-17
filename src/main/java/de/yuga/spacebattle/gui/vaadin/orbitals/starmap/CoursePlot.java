package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.elements.Path;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoursePlot {

    @Nonnull
    private final Path course;

    @Nonnull
    private final QuadCurve2D quadCurve2D;

    @Nonnull
    private final Map<ProgressOnCourse, List<Fleet>> segmentPoint = new HashMap<>();

    public CoursePlot(@Nonnull final Path course, @Nonnull final QuadCurve2D quadCurve2D) {
        Preconditions.checkNotNull(course, "course shouldn't be null!");
        Preconditions.checkNotNull(quadCurve2D, "quadCurve2D shouldn't be null!");

        this.course = course;
        this.quadCurve2D = quadCurve2D;
    }

    @Nonnull
    public Path getCourse() {
        return course;
    }

    @Nonnull
    public QuadCurve2D getQuadCurve2D() {
        return quadCurve2D;
    }

    @Nonnull
    public List<Fleet> getFleetsOnStage(@Nonnull final ProgressOnCourse progressOnCourse) {
        return segmentPoint.computeIfAbsent(progressOnCourse, k -> new ArrayList<>());
    }

    /**
     * Checks if the fleet is part of this course plot.
     *
     * @param fleet the fleet
     * @return the related progress, if found, or null
     */
    @Nullable
    public ProgressOnCourse getProgressByFleet(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return segmentPoint.entrySet().stream()
                .filter(entry -> entry.getValue().contains(fleet))
                .findFirst()
                .map(Map.Entry::getKey).orElse(null);
    }

    /**
     * Checks that there are fleet left on this course.
     *
     * @return <code>true</code> if no fleets are left on this course, <code>false</code> otherwise
     */
    public boolean isEmpty() {
        final List<Fleet> fleets = segmentPoint.values().stream().filter(l -> !l.isEmpty()).findFirst().orElse(null);
        return fleets == null;
    }


    public void removeFleet(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        segmentPoint.entrySet().stream()
                .filter(entry -> entry.getValue().contains(fleet))
                .findFirst()
                .ifPresent(p -> segmentPoint.get(p.getKey()).remove(fleet));
    }
}
