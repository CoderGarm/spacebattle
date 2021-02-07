package de.yuga.spacebattle.enums;

public enum EResourceSubType {

    MININGFACTORS(0),
    DEPOSITS(1),
    COSTS(2);

    int subType;

    EResourceSubType(int subType) {
        this.subType = subType;
    }
}
