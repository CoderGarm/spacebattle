package de.yuga.spacebattle.backend.combat.maneuver;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;

import javax.annotation.Nonnull;

public class ManeuverFactory {

    @Nonnull
    private final Cage cage;

    public ManeuverFactory(@Nonnull final Cage cage) {
        this.cage = Preconditions.checkNotNull(cage, "cage must not be empty");
    }

    @Nonnull
    public Maneuver createInitial() {

        final Fleet agent = cage.getAggressor();
        final Fleet target = cage.getDefender();

        final FleetRoundState agentState = cage.getCurrentStateByFleet(agent);
        final FleetRoundState targetState = cage.getCurrentStateByFleet(target);

        final KinematicInfo agentsKinematicInitial = KinematicInfo.getFrom(agentState);
        final KinematicInfo agentsKinematicDesignation = KinematicInfo.getFrom(targetState);

        final KinematicInfo targetsKinematicInitial = KinematicInfo.getFrom(targetState);
        final KinematicInfo targetsKinematicDesignation = KinematicInfo.getFrom(agentState);

        return new TimeOptimizedCourse(
                cage,
                cage.getCurrentCombatRound(),
                agent,
                agentsKinematicInitial,
                agentsKinematicDesignation,
                target,
                targetsKinematicInitial,
                targetsKinematicDesignation
        );
    }

    @Nonnull
    public Maneuver createInitialResponseManeuver(@Nonnull final Maneuver aggression) {
        Preconditions.checkNotNull(aggression, "aggression must not be empty");

        final Fleet agent = cage.getDefender();
        final Fleet target = cage.getAggressor();

        final FleetRoundState agentState = cage.getCurrentStateByFleet(agent);
        final FleetRoundState targetState = cage.getCurrentStateByFleet(target);

        final KinematicInfo agentsKinematicInitial = KinematicInfo.getFrom(agentState);
        final KinematicInfo agentsKinematicDesignation = KinematicInfo.getFrom(targetState);

        final KinematicInfo targetsKinematicInitial = KinematicInfo.getFrom(targetState);
        final KinematicInfo targetsKinematicDesignation = KinematicInfo.getFrom(agentState);

        return new BroadsidePassing(
                cage,
                cage.getCurrentCombatRound(),
                agent,
                agentsKinematicInitial,
                agentsKinematicDesignation,
                target,
                targetsKinematicInitial,
                targetsKinematicDesignation
        );
    }
}
