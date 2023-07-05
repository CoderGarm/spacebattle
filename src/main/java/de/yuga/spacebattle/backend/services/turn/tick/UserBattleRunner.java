package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.spacecraft.BattleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Service
public class UserBattleRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(UserBattleRunner.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final BattleService battleService;

    @Autowired
    public UserBattleRunner(@Nonnull final BattleService battleService) {
        this.battleService = Preconditions.checkNotNull(battleService, "battleService must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Run user battles");
        battleService.runBattles(today);
    }


}
