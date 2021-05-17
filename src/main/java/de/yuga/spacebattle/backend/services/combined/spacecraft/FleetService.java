package de.yuga.spacebattle.backend.services.combined.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.repositories.combined.spacecraft.FleetRepository;
import de.yuga.spacebattle.backend.repositories.turn.MoveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FleetService {

    @Nonnull
    private final FleetRepository fleetR;

    @Nonnull
    private final MoveRepository moveR;

    @Autowired
    public FleetService(@Nonnull final FleetRepository fleetR,
                        @Nonnull final MoveRepository moveR) {
        Preconditions.checkNotNull(fleetR, "fleetR shouldn't be null!");
        Preconditions.checkNotNull(moveR, "moveR shouldn't be null!");

        this.fleetR = fleetR;
        this.moveR = moveR;
    }

    /**
     * Merges the second fleet into the first.
     */
    @Transactional(rollbackFor = Exception.class)
    public Fleet mergeFleets(@Nonnull final Fleet baseFleet, final Set<Fleet> fleetsToMerge) {
        Preconditions.checkNotNull(baseFleet, "baseFleet shouldn't be null!");
        Preconditions.checkNotNull(fleetsToMerge, "fleetsToMerge shouldn't be null!");
        Preconditions.checkState(baseFleet.getOrbit() != null, "baseFleets orbit shouldn't be empty!");

        if (fleetsToMerge.isEmpty()) {
            return baseFleet;
        }

        final FleetOrbit orbit = baseFleet.getOrbit();
        if (fleetsToMerge.stream().anyMatch(fleetToMerge -> !orbit.equals(fleetToMerge.getOrbit()))) {
            throw new NotifySBUserException("That's not possible, no.");
        }
        fleetsToMerge.forEach(fleet2 -> {
            final Map<ShipClass, Integer> ships2 = fleet2.getShips();
            for (ShipClass shipClass : ships2.keySet()) {
                Integer amount2 = ships2.get(shipClass);
                baseFleet.updateShips(shipClass, amount2);
            }
            fleet2.getShips().clear();
        });
        fleetR.deleteAll(fleetsToMerge.stream().map(Fleet::getId).collect(Collectors.toSet()));
        fleetR.save(baseFleet);
        return baseFleet;
    }

    /**
     * Sets a fleet in motion to the target.
     *
     * @param fleet  the fleet which knows it's position
     * @param target the target
     * @return the fleet in motion
     */
    public Fleet moveFleet(@Nonnull final Fleet fleet, @Nonnull final Planet start, @Nonnull final Planet target) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(start, "start shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final int calculatedDistance = DistanceCalculator.calculateTimeToTravel(fleet, start, target);
        final Move move = new Move(fleet, start, target, calculatedDistance);
        // set the move
        fleet.setMove(move);
        fleetR.save(fleet);
        return fleet;
    }

    /**
     * Cancels a running flight and heads the fleet back to their origin.
     * <p>
     * Only possible in planetary systems due the lack of communication on deep space missions.
     *
     * @param fleet the fleet which has to be flown back
     * @return the fleet with the ne movement
     */
    public Fleet cancelFlight(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkArgument(fleet.getMove() != null, "fleet's move shouldn't be null!");

        final Move move = fleet.getMove();
        final FleetOrbit targetOrbit = move.getTargetOrbit();
        final Planet targetPlanet = targetOrbit.getPlanet();

        final FleetOrbit startOrbit = move.getStartOrbit();
        final Planet startPlanet = startOrbit.getPlanet();

        if (targetPlanet == null || startPlanet == null) {
            throw new NotifySBUserException("A movement must always have a beginning and a designated target.");
        }

        // permute origin and destination
        return moveFleet(fleet, targetPlanet, startPlanet, true);
    }

    /**
     * Sets a fleet in motion to the target. From an origin which differs from the currents fleet's position.
     * That means that this should be used for things like "cancel a flight" but it can be used accidentally as god-like jump-drive.
     * Pay attention.
     *
     * @param fleet                      the fleet which knows it's position
     * @param start                      the start planet if it is different from the fleets current position
     * @param target                     the target
     * @param useCurrentMovementProgress if <code>true</code> the progress on the current track will be used for the new movement
     * @return the fleet in motion
     */
    private Fleet moveFleet(@Nonnull final Fleet fleet,
                            @Nonnull final Planet start,
                            @Nonnull final Planet target,
                            final boolean useCurrentMovementProgress) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(start, "start shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");
        Preconditions.checkArgument(fleet.getMove() != null, "fleet's move shouldn't be null!");

        final int moveDoneAtZero = fleet.getMove().getMoveDoneAtZero();

        fleet.setOrbit(new FleetOrbit(start));
        int calculateTimeToTravel = DistanceCalculator.calculateTimeToTravel(fleet, start, target);

        final int alreadyTravelled = calculateTimeToTravel - moveDoneAtZero;
        if (useCurrentMovementProgress) {
            if (calculateTimeToTravel - alreadyTravelled <= 0) {
                calculateTimeToTravel = 0;
            } else {
                calculateTimeToTravel = calculateTimeToTravel - alreadyTravelled;
            }
        }

        if (calculateTimeToTravel > 0) {
            // set move
            final Move move = new Move(fleet, start, target, calculateTimeToTravel);
            fleet.setMove(move);
        } else {
            // set fleet in planetary orbit
            fleet.setMove(null);
            fleet.setOrbit(new FleetOrbit(target));
        }
        fleetR.save(fleet);
        return fleet;
    }

    public List<Fleet> findAllFleets() {
        return fleetR.findAllFleets();
    }

    public Fleet findById(int idFleet) {
        return fleetR.findById(idFleet).orElse(null);
    }

    public Fleet find(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return fleetR.findById(fleet.getId()).orElse(null);
    }

    public void save(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        fleetR.save(fleet);
    }

    public Fleet saveAndFlush(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return fleetR.saveAndFlush(fleet);
    }

    public void delete(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        fleetR.delete(fleet);
    }

    public List<Fleet> findAllFleetsBy(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return fleetR.findAllFleetsBy(user);
    }
}
