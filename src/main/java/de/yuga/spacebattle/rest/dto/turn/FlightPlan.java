package de.yuga.spacebattle.rest.dto.turn;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class FlightPlan {

    @JsonProperty
    @Schema(required = true, description = "The number of this tick after starting the move.")
    private int timeAfterStart;

    @Nullable
    @JsonProperty
    @Schema(description = "location of this waypoint.")
    private FleetOrbit location;

    public FlightPlan() {
    }

    public FlightPlan(@Nonnull final de.yuga.spacebattle.backend.entities.turn.navigation.FlightPlan flightPlan) {
        Preconditions.checkNotNull(flightPlan, "flightPlan must not be empty");

        this.timeAfterStart = flightPlan.getTimeAfterStart();
        this.location = new FleetOrbit(flightPlan.getLocation());
    }
}
