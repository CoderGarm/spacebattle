package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import javax.annotation.Nonnull;

/**
 * Represents the three stages on a track.
 * It's much more easy to place fleets an well-defined places and not exactly where they really are.
 * <p>
 * These well-defined places are a third of a track, the half or bigger then the half.
 */
public enum ProgressOnCourse {

    NOT_ON_TRACK(0),
    ORIGIN((double) 1 / 3),
    HALFWAY((double) 1 / 2),
    DESTINATION((double) 2 / 3);

    private final double progressOntrack;

    ProgressOnCourse(final double progressOntrack) {
        this.progressOntrack = progressOntrack;
    }

    public double getProgressOntrack() {
        return progressOntrack;
    }

    /**
     * Returns the progress as enum to sort it in a map.
     *
     * @param progress a factor between 0 and 1 which represents the progress af a movement
     * @return the progress as enum
     */
    @Nonnull
    public static ProgressOnCourse getByProgress(final double progress) {
        if (progress <= ORIGIN.progressOntrack) {
            return ORIGIN;
        }
        if (progress < DESTINATION.progressOntrack) {
            return HALFWAY;
        }
        if (progress >= DESTINATION.progressOntrack) {
            return DESTINATION;
        }
        return NOT_ON_TRACK;
    }
}
