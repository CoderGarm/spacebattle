package de.yuga.spacebattle.misc.fandom.spacecraft.dto.classes;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.WikiShipClass;

import javax.annotation.Nonnull;
import java.util.List;

public class ClassesByIntroductionDate {

    @Nonnull
    private final String introductionDate;

    @Nonnull
    private final List<WikiShipClass> classes;

    public ClassesByIntroductionDate(@Nonnull final String introductionDate, @Nonnull final List<WikiShipClass> classes) {
        this.introductionDate = Preconditions.checkNotNull(introductionDate, "introductionDate must not be empty");
        this.classes = Preconditions.checkNotNull(classes, "classes must not be empty");
    }

    @Nonnull
    public String getIntroductionDate() {
        return introductionDate;
    }

    @Nonnull
    public List<WikiShipClass> getClasses() {
        return classes;
    }
}
