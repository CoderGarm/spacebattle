package de.yuga.spacebattle.rest.dto.misc.descriptors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Time;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead;
import de.yuga.spacebattle.backend.enums.EWarheadType;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class MissileDescriptor {

    @JsonProperty
    @Schema(required = true, description = "The projected damage of this warhead.")
    private long damageValue;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The effective range of this warhead.")
    private Distance damageProjectionRange;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The warhead type.")
    private EWarheadType warheadType;

    @JsonProperty
    @Schema(required = true, description = "The endurance of this engine.")
    private Time endurance;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The acceleration of this engine.")
    private Acceleration acceleration;

    @JsonProperty
    @Schema(required = true,
            description = "Defines the capability of this weapon to penetrate the shield.\n" +
                    " The means the maneuver capability to find a gap in the tank to fire into it, for instance.")
    private double maneuverability;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The maximum range under drive.")
    private Distance maxRange;

    @JsonProperty
    @Schema(required = true,
            description = "Defines the capability of this weapon to ignore the enemies electronic warfare measurements.")
    private int elokaResistance;

    public MissileDescriptor() {
    }

    public MissileDescriptor(@Nonnull final Missile content) {
        Preconditions.checkNotNull(content, "content must not be empty");

        final Warhead warhead = content.getWarhead();
        this.warheadType = warhead.getWarheadType();
        this.damageValue = warhead.getDamageValue();
        this.damageProjectionRange = warhead.getDamageProjectionRange();

        final MissileMotor missileMotor = content.getMissileMotor();
        this.acceleration = missileMotor.getAcceleration();
        this.endurance = new Time(missileMotor.getEndurance(), ETimeMetric.SECOND);
        this.maneuverability = missileMotor.getManeuverability();

        this.maxRange = content.getMaximumMissileRange();

        this.elokaResistance = content.getElokaResistance();
    }
}
