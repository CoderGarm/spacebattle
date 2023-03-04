package de.yuga.spacebattle.backend.enums;

public enum ETechnologyType {

    CIVIL(0.6),
    MILITARY(0.8);

    /**
     * The part of the speed of light which is reachable with this technology type.
     */
    private final double maxVelocitySOL;

    ETechnologyType(final double maxVelocitySOL) {
        this.maxVelocitySOL = maxVelocitySOL;
    }

    public double getMaxVelocitySOL() {
        return maxVelocitySOL;
    }
}
