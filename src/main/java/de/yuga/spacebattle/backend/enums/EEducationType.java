package de.yuga.spacebattle.backend.enums;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Describes the level of education.
 */
public enum EEducationType implements HasIconName {

    /**
     * no explicit education
     */
    NONE(null, 0),

    /**
     * civil Mk I is kind of elementary school
     */
    SCHOOL(EEducationType.NONE, 0.1),

    /**
     * civil Mk II is kind of secondary school or untrained workers
     */
    COLLEGE(EEducationType.SCHOOL, 1),

    /**
     * civil Mk III is kind of internship, vocational training or a university  education
     */
    UNIVERSITY(EEducationType.COLLEGE, 1),

    /**
     * military Mk I is a teams rank
     */
    ENLISTED(EEducationType.COLLEGE, 0.4),

    /**
     * military Mk II is a officers rank
     */
    OFFICER(EEducationType.UNIVERSITY, 0.3),
    ;


    public static final Set<EEducationType> MILITARY = Set.of(ENLISTED, OFFICER);
    public static final Set<EEducationType> WORKFORCE = Set.of(COLLEGE, UNIVERSITY, ENLISTED, OFFICER);

    /**
     * The requirement of an educational level which must be fulfilled to reach *this* level.
     */
    @Nullable
    private final EEducationType requirement;

    @Nonnull
    final String iconName;

    final double reproductionCoefficient;

    EEducationType(@Nullable final EEducationType requirement, final double reproductionCoefficient) {
        this.requirement = requirement;
        this.iconName = this.name();
        this.reproductionCoefficient = reproductionCoefficient;
    }

    public boolean isWorkforce() {
        return WORKFORCE.contains(this);
    }

    public boolean isMilitary() {
        return MILITARY.contains(this);
    }

    @Nullable
    public EEducationType getRequirement() {
        return requirement;
    }

    @Nonnull
    @Override
    public String getIconName() {
        return iconName;
    }

    public double getReproductionCoefficient() {
        return reproductionCoefficient;
    }
}
