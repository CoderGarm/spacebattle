package de.yuga.spacebattle.backend.services.turn.tick.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.turn.tick.mission.phases.PirateRaiderPhase;
import de.yuga.spacebattle.backend.services.turn.tick.mission.phases.PirateSpawnPhase;
import de.yuga.spacebattle.backend.services.turn.tick.mission.phases.PirateWithdrawPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

/**
 * Spawns pirates and raids planets.
 */
@Service
public class RaidingPiratesMission implements MissionRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(RaidingPiratesMission.class);

    @Nonnull
    private final PirateSpawnPhase pirateSpawnPhase;

    @Nonnull
    private final PirateRaiderPhase pirateRaiderPhase;

    @Nonnull
    private final PirateWithdrawPhase pirateWithdrawPhase;

    @Autowired
    public RaidingPiratesMission(@Nonnull final PirateSpawnPhase pirateSpawnPhase,
                                 @Nonnull final PirateRaiderPhase pirateRaiderPhase,
                                 @Nonnull final PirateWithdrawPhase pirateWithdrawPhase) {
        this.pirateSpawnPhase = Preconditions.checkNotNull(pirateSpawnPhase, "pirateSpawnPhase must not be empty");
        this.pirateRaiderPhase = Preconditions.checkNotNull(pirateRaiderPhase, "pirateRaiderPhase must not be empty");
        this.pirateWithdrawPhase = Preconditions.checkNotNull(pirateWithdrawPhase, "pirateWithdrawPhase must not be empty");
    }

    @Override
    public void executeMission(@Nonnull final Tick today) {
        Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Active pirate mission started");

        pirateSpawnPhase.executePhase(today);
        pirateRaiderPhase.executePhase(today);
        pirateWithdrawPhase.executePhase(today);
    }

    public static boolean canRun(@Nonnull final Tick today) {
        return today.getNo() % 3 == 0;
    }
}
