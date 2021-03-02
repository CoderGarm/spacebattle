package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.definitions.RaceBonus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EModuleType {


    WEAPON("Attack", false, RaceBonus.getStandartKandorianBonus(), EIconPath.STATS.getPath(), "attack"),
    ARMOR("Armor", false, null, EIconPath.STATS.getPath(), "armor"),
    SHIELD("Shield", false, RaceBonus.getStandartHumanBonus(), EIconPath.STATS.getPath(), "shield"),
    PROPULSION("Propulsion", true, null, EIconPath.STATS.getPath(), "propulsion"),
    FTLPROPULSION("FTLPropulsion", false, null, EIconPath.STATS.getPath(), "ftlpropulsion"),
    SCANNER("Scanner", true, null, EIconPath.STATS.getPath(), "scanner");

    @Nonnull
    final RaceBonus[] raceBonus;

    @Nonnull
    final String name;

    @Nonnull
    final boolean mandatory;

    @Nonnull
    final String directory;

    @Nonnull
    final String iconName;

    EModuleType(@Nonnull final String name, final boolean mandatory, @Nullable final RaceBonus[] raceBonus,
                @Nonnull final String directory, @Nonnull final String iconName) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(directory, "directory shouldn't be null!");
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");

        this.raceBonus = raceBonus;
        this.name = name;
        this.mandatory = mandatory;
        this.directory = directory;
        this.iconName = iconName;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nullable
    public RaceBonus[] getRaceBonus() {
        return raceBonus;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    @Nonnull
    public static List<EModuleType> getMandatories() {
        return Arrays.stream(EModuleType.values()).filter(EModuleType::isMandatory).collect(Collectors.toList());
    }

    @Nonnull
    public int getBonus(ERaceType raceType) {
        if (this.raceBonus == null || this.raceBonus.length == 0)
            return 0;

        RaceBonus raceBonus = Arrays.stream(this.raceBonus).filter(r -> r.getRaceType().equals(raceType)).findFirst().orElse(null);
        if (raceBonus != null)
            return raceBonus.getBonus();


        return 0;
    }

    @Nonnull
    public String getDirectory() {
        return directory;
    }

    @Nonnull
    public String getIconName() {
        return iconName;
    }
}
