package de.yuga.spacebattle.rest.dto.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator.MC_HU;

@Schema(description = ".")
public class PropulsionCapacity {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The acceleration.")
    private final Acceleration acceleration;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The velocity.")
    private final Velocity velocity;

    @JsonProperty
    @Schema(required = true, description = "The time to reach the maximum velocity.")
    private final int timeToVMax;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The time metric.")
    private final ETimeMetric timeMetric = ETimeMetric.SECOND;

    public PropulsionCapacity(@Nonnull final Acceleration acceleration, @Nonnull final Velocity velocity) {
        this.acceleration = Preconditions.checkNotNull(acceleration, "acceleration must not be empty");
        this.velocity = Preconditions.checkNotNull(velocity, "velocity must not be empty");

        final BigDecimal velo = velocity.getCoordinateInMetric(EDistanceMetric.M, ETimeMetric.SECOND);
        final BigDecimal acc = acceleration.getCoordinateInMetric(EAccelerationMetric.MS2);
        if (velocity != Velocity.ZERO) {
            this.timeToVMax = velo.divide(acc, MC_HU).setScale(0, RoundingMode.HALF_EVEN).intValue();
        } else {
            this.timeToVMax = 0;
        }
    }
}
