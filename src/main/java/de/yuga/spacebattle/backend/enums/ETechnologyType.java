package de.yuga.spacebattle.backend.enums;

public enum ETechnologyType {

    CIVIL(0.6, 0.45),
    MILITARY(0.8, 1);

    /**
     * The part of the speed of light which is reachable with this technology type.
     */
    private final double maxVelocitySOL;

    /**
     * The part of the (military as reference) efficiency in acceleration with the same propulsion value.
     */
    private final double maxAccelerationOfMilitary;

    ETechnologyType(final double maxVelocitySOL, final double maxAccelerationOfMilitary) {
        this.maxVelocitySOL = maxVelocitySOL;
        this.maxAccelerationOfMilitary = maxAccelerationOfMilitary;
    }

    public double getMaxVelocitySOL() {
        return maxVelocitySOL;
    }

    public double getMaxAccelerationOfMilitary() {
        return maxAccelerationOfMilitary;
    }
}
