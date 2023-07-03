package de.yuga.spacebattle.backend.services.turn.tick.mission;

import de.yuga.spacebattle.backend.entities.turn.Tick;

import javax.annotation.Nonnull;

public interface MissionPhaseRunner {
    /**
     * The handler method.
     */
    void executePhase(@Nonnull Tick today);
}
