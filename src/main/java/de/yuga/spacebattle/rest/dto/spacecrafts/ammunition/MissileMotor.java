package de.yuga.spacebattle.rest.dto.spacecrafts.ammunition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Time;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class MissileMotor {

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

    @JsonProperty
    @Schema(required = true, description = "The capacity usage of this module.")
    private int useCapacity;

    public MissileMotor() {
    }

    public MissileMotor(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor missileMotor,
                        @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(missileMotor, "missileMotor shouldn't be null!");

        this.endurance = new Time(missileMotor.getEndurance(), ETimeMetric.SECOND);
        this.acceleration = missileMotor.getAcceleration();
        this.maneuverability = missileMotor.getManeuverability();
    }
}
