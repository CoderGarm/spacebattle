package de.yuga.spacebattle.backend.services.turn.tick.mission;

import de.yuga.spacebattle.backend.entities.turn.Tick;

import javax.annotation.Nonnull;

public interface MissionRunner {

    /**
     * The handler method.
     */
    void executeMission(@Nonnull Tick today);
}
