package de.yuga.spacebattle.backend.enums;

public enum EResourceSubType {

    DEFAULT(-1),
    MININGFACTORS(0),
    DEPOSITS(1),
    COSTS(2);

    int subType;

    EResourceSubType(int subType) {
        this.subType = subType;
    }
}
