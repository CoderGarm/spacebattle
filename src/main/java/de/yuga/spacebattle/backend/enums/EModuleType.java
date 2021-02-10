package de.yuga.spacebattle.backend.enums;


import de.yuga.spacebattle.backend.definitions.RaceBonus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EModuleType {

    WEAPON("weapon", false, RaceBonus.getStandartKandorianBonus()),
    ARMOR("armor", false, null),
    SHIELD("shield", false, RaceBonus.getStandartHumanBonus()),
    PROPULSION("propulsion", true, null),
    FTLPROPULSION("ftlpropulsion", false, null),
    SCANNER("scanner", true, null);

    RaceBonus[] raceBonus;
    String name;
    boolean mandatory;

    EModuleType(String name, boolean mandatory, RaceBonus[] raceBonus) {
        this.raceBonus = raceBonus;
        this.name = name;
        this.mandatory = mandatory;
    }

    public String getName() {
        return name;
    }

    public RaceBonus[] getRaceBoni() {
        return raceBonus;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public static List<EModuleType> getMandatories() {
        return Arrays.stream(EModuleType.values()).filter(EModuleType::isMandatory).collect(Collectors.toList());
    }

    public int getBonus(ERaceType raceType) {
        if (this.raceBonus == null || this.raceBonus.length == 0)
            return 0;

        RaceBonus raceBonus = Arrays.stream(this.raceBonus).filter(r -> r.getRaceType().equals(raceType)).findFirst().orElse(null);
        if (raceBonus != null)
            return raceBonus.getBonus();


        return 0;
    }

}
