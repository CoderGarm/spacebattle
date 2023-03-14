package de.yuga.spacebattle.misc.fandom.spacecraft.dto.classes;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.List;

public class ClassesByAffiliation {

    @Nonnull
    private final String affiliation;

    @Nonnull
    private final List<ClassesByIntroductionDateAndType> byDateAndType;

    public ClassesByAffiliation(@Nonnull final String affiliation, @Nonnull final List<ClassesByIntroductionDateAndType> byDateAndType) {
        this.affiliation = Preconditions.checkNotNull(affiliation, "affiliation must not be empty");
        this.byDateAndType = Preconditions.checkNotNull(byDateAndType, "byDateAndType must not be empty");
    }

    @Nonnull
    public String getAffiliation() {
        return affiliation;
    }

    @Nonnull
    public List<ClassesByIntroductionDateAndType> getByDateAndType() {
        return byDateAndType;
    }
}
