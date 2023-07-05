package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.turn.tick.mission.MissionRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

@Service
public class NPCMissionRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(NPCMissionRunner.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final Set<MissionRunner> missionRunners;

    @Autowired
    public NPCMissionRunner(@Nonnull final Set<MissionRunner> missionRunners) {
        this.missionRunners = Preconditions.checkNotNull(missionRunners, "missionRunners must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Execute NPC missions");
        for (final MissionRunner missionRunner : missionRunners) {
            missionRunner.executeMission(today);
        }
    }


}
