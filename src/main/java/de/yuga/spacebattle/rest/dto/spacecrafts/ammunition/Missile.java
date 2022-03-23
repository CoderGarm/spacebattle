package de.yuga.spacebattle.rest.dto.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Missile {

    @ApiModelProperty(required = true, value = "The id of this Missile.")
    private int idMissile;

    @Nonnull
    @ApiModelProperty(required = true, value = "The type name of this missile.")
    private String typeName;

    @ApiModelProperty(required = true, value = "The warhead capacity of this missile.")
    private int warheadCapacity;

    @ApiModelProperty(required = true, value = "The engine capacity of this missile.")
    private int motorCapacity;

    @ApiModelProperty(required = true, value = "The engine capacity of this missile.")
    private int elokaResistance;

    @Nonnull
    @ApiModelProperty(required = true, value = "The warhead of this missile.")
    private de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Warhead warhead;

    @Nonnull
    @ApiModelProperty(required = true, value = "The implemented engine of this missile.")
    private final List<MissileMotor> missileMotors = new ArrayList<>();

    public Missile() {
    }

    public Missile(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile missile) {
        Preconditions.checkNotNull(missile, "missile shouldn't be null!");

        this.idMissile = missile.getId();
        this.typeName = missile.getTypeName();
        this.warheadCapacity = missile.getWarheadCapacity();
        this.motorCapacity = missile.getMotorCapacity();
        this.elokaResistance = missile.getElokaResistance();
        this.warhead = new Warhead(missile.getWarhead());
        for (int i = 1; i <= missile.getMotorAmount(); i++) {
            this.missileMotors.add(new MissileMotor(missile.getMissileMotor()));
        }
    }


    public int getIdMissile() {
        return idMissile;
    }

    public void setIdMissile(int idMissile) {
        this.idMissile = idMissile;
    }

    @Nonnull
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(@Nonnull String typeName) {
        this.typeName = typeName;
    }

    public int getWarheadCapacity() {
        return warheadCapacity;
    }

    public void setWarheadCapacity(int warheadCapacity) {
        this.warheadCapacity = warheadCapacity;
    }

    public int getMotorCapacity() {
        return motorCapacity;
    }

    public void setMotorCapacity(int motorCapacity) {
        this.motorCapacity = motorCapacity;
    }

    public int getElokaResistance() {
        return elokaResistance;
    }

    public void setElokaResistance(final int elokaResistance) {
        this.elokaResistance = elokaResistance;
    }

    @Nonnull
    public Warhead getWarhead() {
        return warhead;
    }

    public void setWarhead(@Nonnull Warhead warhead) {
        this.warhead = warhead;
    }

    @Nonnull
    public List<MissileMotor> getMissileMotors() {
        return missileMotors;
    }

    public void setMissileMotors(@Nonnull List<MissileMotor> missileMotors) {
        this.missileMotors.addAll(missileMotors);
    }
}
