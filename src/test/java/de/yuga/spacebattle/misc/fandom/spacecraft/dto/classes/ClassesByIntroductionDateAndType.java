package de.yuga.spacebattle.misc.fandom.spacecraft.dto.classes;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.List;

public class ClassesByIntroductionDateAndType {

    @Nonnull
    private final String shipType;

    @Nonnull
    private final List<ClassesByIntroductionDate> byIntroductionDate;

    public ClassesByIntroductionDateAndType(@Nonnull final String shipType, @Nonnull final List<ClassesByIntroductionDate> byIntroductionDate) {
        this.shipType = Preconditions.checkNotNull(shipType, "shipType must not be empty");
        this.byIntroductionDate = Preconditions.checkNotNull(byIntroductionDate, "byIntroductionDate must not be empty");
    }

    @Nonnull
    public String getShipType() {
        return shipType;
    }

    @Nonnull
    public List<ClassesByIntroductionDate> getByIntroductionDate() {
        return byIntroductionDate;
    }
}
