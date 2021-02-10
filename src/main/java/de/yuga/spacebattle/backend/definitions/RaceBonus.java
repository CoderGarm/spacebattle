package de.yuga.spacebattle.backend.definitions;

import de.yuga.spacebattle.backend.enums.ERaceType;

public class RaceBonus {

    public RaceBonus() {
    }

    public RaceBonus(ERaceType raceType, int bonus) {
        this.raceType = raceType;
        this.bonus = bonus;
    }

    private ERaceType raceType;
    private int bonus;


    public static RaceBonus[] getStandartKandorianBonus() {
        RaceBonus[] a = {new RaceBonus(ERaceType.KANDORIAN, 10)};
        return a;
    }

    public static RaceBonus[] getStandartHumanBonus() {
        RaceBonus[] a = {new RaceBonus(ERaceType.HUMAN, 10)};
        return a;
    }

    public ERaceType getRaceType() {
        return raceType;
    }

    public void setRaceType(ERaceType raceType) {
        this.raceType = raceType;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

}
