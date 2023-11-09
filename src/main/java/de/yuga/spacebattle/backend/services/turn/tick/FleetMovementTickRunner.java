package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.spacecraft.FleetMovementExecutorService;
import de.yuga.spacebattle.backend.services.turn.MoveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class FleetMovementTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(FleetMovementTickRunner.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final MoveService moveService;

    @Nonnull
    private final FleetMovementExecutorService fleetMovementExecutorService;

    @Autowired
    public FleetMovementTickRunner(@Nonnull final MoveService moveService,
                                   @Nonnull final FleetMovementExecutorService fleetMovementExecutorService) {
        this.moveService = Preconditions.checkNotNull(moveService, "moveService must not be empty");
        this.fleetMovementExecutorService = Preconditions.checkNotNull(fleetMovementExecutorService, "fleetMovementExecutorService must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Move fleets");
        tickMovements();
    }

    /**
     * Runs the tick for all movements.
     */
    private void tickMovements() {
        Preconditions.checkNotNull(today, "today must not be empty");

        final List<Move> movements = moveService.findAllUncompleted();
        for (final Move m : movements) {
            fleetMovementExecutorService.executeMove(m, today);
        }
    }
}
