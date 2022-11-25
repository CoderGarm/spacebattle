package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

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
    NONE(null, "baby"),

    /**
     * civil Mk I is kind of elementary school
     */
    SCHOOL(EEducationType.NONE, "civilI"),

    /**
     * civil Mk II is kind of secondary school or untrained workers
     */
    COLLEGE(EEducationType.SCHOOL, "civilII"),

    /**
     * civil Mk III is kind of internship, vocational training or a university  education
     */
    UNIVERSITY(EEducationType.COLLEGE, "civilIII"),

    /**
     * military Mk I is a teams rank
     */
    ENLISTED(EEducationType.COLLEGE, "soldier"),

    /**
     * military Mk II is a officers rank
     */
    OFFICER(EEducationType.UNIVERSITY, "officer"),
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

    EEducationType(@Nullable final EEducationType requirement,
                   @Nonnull final String iconName) {
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");

        this.requirement = requirement;
        this.iconName = iconName;
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
}
