package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.caches.FleetMovementCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
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
    private final FleetService fleetService;

    @Nonnull
    private final FleetMovementCache fleetMovementCache;

    @Nonnull
    private final PlanetService planetService;

    @Autowired
    public FleetMovementTickRunner(@Nonnull final PlanetService planetService,
                                   @Nonnull final MoveService moveService,
                                   @Nonnull final FleetService fleetService,
                                   @Nonnull final FleetMovementCache fleetMovementCache) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.moveService = Preconditions.checkNotNull(moveService, "moveService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.fleetMovementCache = Preconditions.checkNotNull(fleetMovementCache, "fleetMovementCache must not be empty");
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
        final List<Move> movements = moveService.findAll();
        for (final Move m : movements) {
            boolean isDone = move(m);
            if (isDone) {
                Fleet fleet = m.getFleet();
                fleet.setMove(null);
                fleet = fleetService.save(fleet);
                final Planet originPlanet = planetService.findByCoordinates(m.getOriginOrbit());
                final Planet destinationPlanet = planetService.findByCoordinates(m.getDestinationOrbit());
                if (originPlanet != null && destinationPlanet != null) {
                    // planet to planet travel
                    fleetMovementCache.add(today, fleet, m, destinationPlanet);
                }
                if (destinationPlanet == null && m.getOriginOrbit().getSystem() != null && m.getDestinationOrbit().getSystem() != null) {
                    // somewhere to hyperlimit travel
                    fleetMovementCache.add(today, fleet, m, m.getDestinationOrbit().getSystem());
                }
            } else {
                moveService.save(m);
            }
        }
    }

    /**
     * Processes a movement.
     *
     * @param move the movement to process
     * @return <code>true</code> if the movement is done, <code>false</code> otherwise
     */
    private boolean move(@Nonnull final Move move) {
        Preconditions.checkNotNull(move, "move shouldn't be null!");

        int moveDoneAtZero = move.getMoveDoneAtZero();
        moveDoneAtZero--;
        if (moveDoneAtZero > 0) {
            move.setMoveDoneAtZero(moveDoneAtZero);
            // todo detect if fleet is in hyperspace and remove fleet orbit completely
            return false;
        }

        final FleetOrbit targetOrbit = move.getDestinationOrbit();
        final StarSystem targetSystem = targetOrbit.getSystem();
        final Orbit orbit = targetOrbit.getOrbit();

        final Fleet fleet = move.getFleet();
        fleet.setOrbit(new FleetOrbit(orbit, targetSystem));
        return true;
    }

}
