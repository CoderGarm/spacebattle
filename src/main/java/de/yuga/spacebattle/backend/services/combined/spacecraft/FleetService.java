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
    public Fleet moveFleet(@Nonnull final Fleet fleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final int calculatedDistance = DistanceCalculator.calculateTimeToTravel(fleet, target);
        final Move move = new Move(fleet, target, calculatedDistance);
        fleet.setMove(move);
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
