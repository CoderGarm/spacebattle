package de.yuga.spacebattle.backend.dto.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.navigation.FlightPlan;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FlightPlanDto {

    private int ticksLeft = 0;

    @Nonnull
    private final List<StarSystem> waypoints = new ArrayList<>();

    @Nonnull
    private final List<FlightPlan> flightPlan = new ArrayList<>();

    public FlightPlanDto() {
    }

    public FlightPlanDto(final int ticksLeft,
                         @Nonnull final List<StarSystem> waypoints,
                         @Nonnull final List<FlightPlan> flightPlan) {
        Preconditions.checkNotNull(waypoints, "waypoints must not be empty");
        Preconditions.checkNotNull(flightPlan, "flightPlan must not be empty");

        this.ticksLeft = ticksLeft;
        this.waypoints.addAll(waypoints);
        this.flightPlan.addAll(flightPlan);
    }

    public static FlightPlanDto empty() {
        return new FlightPlanDto();
    }

    public int getTicksLeft() {
        return ticksLeft;
    }

    @Nonnull
    public List<FlightPlan> getFlightPlan(@Nonnull final Move move) {
        Preconditions.checkNotNull(move, "move must not be empty");

        flightPlan.forEach(flightPlan -> flightPlan.setMove(move));
        return flightPlan;
    }

    @Nonnull
    public List<StarSystem> getWaypoints() {
        return waypoints;
    }
}
