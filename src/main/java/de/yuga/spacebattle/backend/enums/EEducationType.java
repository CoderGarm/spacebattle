package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Describes the level of education.
 */
public enum EEducationType implements HasIconName {

    /**
     * no explicit education
     */
    NONE(false, null, "baby"),

    /**
     * civil Mk I is kind of elementary school
     */
    SCHOOL(false, EEducationType.NONE, "civilI"),

    /**
     * civil Mk II is kind of secondary school or untrained workers
     */
    COLLEGE(true, EEducationType.SCHOOL, "civilII"),

    /**
     * civil Mk III is kind of internship, vocational training or a university  education
     */
    UNIVERSITY(true, EEducationType.COLLEGE, "civilIII"),

    /**
     * military Mk I is a teams rank
     */
    ENLISTED(true, EEducationType.COLLEGE, "soldier"),

    /**
     * military Mk II is a officers rank
     */
    OFFICER(true, EEducationType.UNIVERSITY, "officer"),
    ;

    /**
     * Defines if the education level is part of the working people.
     */
    private final boolean isWorkforce;

    /**
     * The requirement of an educational level which must be fulfilled to reach *this* level.
     */
    @Nullable
    private final EEducationType requirement;

    @Nonnull
    final String iconName;

    EEducationType(final boolean idWorkForce,
                   @Nullable final EEducationType requirement,
                   @Nonnull final String iconName) {
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");

        this.isWorkforce = idWorkForce;
        this.requirement = requirement;
        this.iconName = iconName;
    }

    public boolean isWorkforce() {
        return isWorkforce;
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

    /**
     * Returns all the the values of {@link EEducationType} which could be applied to workplaces.
     *
     * @return the values
     */
    public static EEducationType[] valuesOfWorkforce() {
        return Arrays.stream(EEducationType.values()).filter(e -> e.isWorkforce).toArray(EEducationType[]::new);
    }
}
