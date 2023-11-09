package de.yuga.spacebattle.backend.services.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.turn.MoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class FleetMovementExecutorService {

    @Nonnull
    private final MoveService moveService;

    @Nonnull
    private final FleetService fleetService;

    @Autowired
    public FleetMovementExecutorService(@Nonnull final MoveService moveService,
                                        @Nonnull final FleetService fleetService) {
        this.moveService = Preconditions.checkNotNull(moveService, "moveService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
    }

    public void executeMove(@Nonnull final Move move, @Nonnull final Tick today) {
        Preconditions.checkNotNull(move, "move must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        final Fleet fleet = move.getFleet();
        final FleetOrbit destinationOrbit = move.getDestinationOrbit();
        boolean isDone = move(move, today);
        moveService.save(move);
        if (isDone) {
            fleet.setOrbit(destinationOrbit);
            fleet.setMove(null);
            fleetService.save(fleet);
        }
    }

    /**
     * Processes a movement.
     *
     * @param move the movement to process
     * @return <code>true</code> if the movement is done, <code>false</code> otherwise
     */
    private boolean move(@Nonnull final Move move, @Nonnull final Tick today) {
        Preconditions.checkNotNull(move, "move shouldn't be null!");
        Preconditions.checkNotNull(today, "today must not be empty");

        int moveDoneAtZero = move.getTicksLeft();
        moveDoneAtZero--;
        if (moveDoneAtZero > 0) {
            move.setTicksLeft(moveDoneAtZero);
            // todo detect if fleet is in hyperspace and remove fleet orbit completely
            return false;
        }
        move.setFinished(today);
        return true;
    }

}
