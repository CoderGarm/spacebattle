package de.yuga.spacebattle.rest.dto.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class MissileMotor {

    @ApiModelProperty(required = true, value = "The id of this MissileMotor.")
    private int idMissileMotor;

    @Nonnull
    @ApiModelProperty(required = true, value = "The type name of this engine.")
    private String typeName;

    /**
     * The duration which the missile engine can fire and accelerate the missile in seconds.
     */
    @ApiModelProperty(required = true, value = "The endurance of this engine.")
    private int endurance;

    /**
     * The acceleration in m/s² which is set if using the engine.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The acceleration of this engine in ms².")
    private Acceleration acceleration;

    /**
     * Defines the capability of this weapon to penetrate the shield.
     * The means the maneuver capability to find a gap in the tank to fire into it, for instance.
     */
    @ApiModelProperty(required = true,
            value = "Defines the capability of this weapon to penetrate the shield.\n" +
                    " The means the maneuver capability to find a gap in the tank to fire into it, for instance.")
    private double maneuverability;

    @ApiModelProperty(required = true, value = "The capacity usage of this module.")
    private int useCapacity;

    public MissileMotor() {
    }

    public MissileMotor(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor missileMotor) {
        Preconditions.checkNotNull(missileMotor, "missileMotor shouldn't be null!");

        this.idMissileMotor = missileMotor.getId();
        this.typeName = missileMotor.getTypeName();
        this.endurance = missileMotor.getEndurance();
        this.acceleration = missileMotor.getAcceleration();
        this.maneuverability = missileMotor.getManeuverability();
        this.useCapacity = missileMotor.getUseCapacity();
    }

    public int getIdMissileMotor() {
        return idMissileMotor;
    }

    public void setIdMissileMotor(int idMissileMotor) {
        this.idMissileMotor = idMissileMotor;
    }

    @Nonnull
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(@Nonnull String typeName) {
        this.typeName = typeName;
    }

    public int getEndurance() {
        return endurance;
    }

    public void setEndurance(int endurance) {
        this.endurance = endurance;
    }

    @Nonnull
    public Acceleration getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Acceleration acceleration) {
        this.acceleration = acceleration;
    }

    public double getManeuverability() {
        return maneuverability;
    }

    public void setManeuverability(double maneuverability) {
        this.maneuverability = maneuverability;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    public void setUseCapacity(int useCapacity) {
        this.useCapacity = useCapacity;
    }
}
