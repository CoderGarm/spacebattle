package de.yuga.spacebattle.backend.enums;

/**
 * Defines if a value in a calculation will be in- or decreased.
 */
public enum ECalculationType {

    NONE(1),
    ADD(1),
    SUBTRACT(-1);

    final int multiplier;

    ECalculationType(int multiplier) {
        this.multiplier = multiplier;
    }

    public int getMultiplier() {
        return multiplier;
    }
}
