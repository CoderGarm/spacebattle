package de.yuga.spacebattle.logic.combined.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.distance.DistanceCalculator;
import de.yuga.spacebattle.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.entities.orbitals.Planet;
import de.yuga.spacebattle.entities.turn.Move;
import de.yuga.spacebattle.repositories.combined.spacecraft.FleetRepository;
import de.yuga.spacebattle.repositories.orbitals.PlanetRepository;
import de.yuga.spacebattle.repositories.turn.MoveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@Service
public class FleetService {

    @Nonnull
    private final FleetRepository fleetR;

    @Nonnull
    private final PlanetRepository planetR;

    @Nonnull
    private final MoveRepository moveR;

    @Autowired
    public FleetService(@Nonnull final FleetRepository fleetR,
                        @Nonnull final PlanetRepository planetR,
                        @Nonnull final MoveRepository moveR) {
        Preconditions.checkNotNull(fleetR, "fleetR shouldn't be null!");
        Preconditions.checkNotNull(planetR, "planetR shouldn't be null!");
        Preconditions.checkNotNull(moveR, "moveR shouldn't be null!");

        this.fleetR = fleetR;
        this.planetR = planetR;
        this.moveR = moveR;
    }

    /**
     * Merges the second fleet into the first.
     *
     * @param idFleet1 id of the merge-into fleet
     * @param idFleet2 if of the to-merge fleet
     */
    public Fleet mergeFleets(final int idFleet1, final int idFleet2) {
        Fleet fleet1 = fleetR.findById(idFleet1).orElse(null);
        Fleet fleet2 = fleetR.findById(idFleet2).orElse(null);

        if (fleet1 == null || fleet2 == null) {
            throw new NotifySBUserException("Couldn't find at least one of the fleets.");
        }

        FleetOrbit orbit = fleet1.getOrbit();
        FleetOrbit orbit1 = fleet2.getOrbit();

        if (!orbit.equals(orbit1)) {
            throw new NotifySBUserException("haha, no.");
        }

        Map<ShipClass, Integer> ships1 = fleet1.getShips();
        Map<ShipClass, Integer> ships2 = fleet2.getShips();
        for (ShipClass shipClass : ships2.keySet()) {
            Integer amount2 = ships2.get(shipClass);
            if (ships1.containsKey(shipClass)) {
                Integer amount1 = ships1.get(shipClass);
                ships1.put(shipClass, amount1 + amount2);
            } else {
                ships1.put(shipClass, amount2);
            }
        }
        fleetR.delete(fleet2);
        fleetR.save(fleet1);
        return fleet1;
    }

    public int calculateDistance(final int idFleet, final int idPlanet) {
        Preconditions.checkState(idFleet < 1, "idFleet shouldn't be null!");
        Preconditions.checkState(idPlanet < 1, "idPlanet shouldn't be null!");

        Fleet fleet = fleetR.findById(idFleet).orElse(null);
        Planet planet = planetR.findById(idPlanet).orElse(null);
        if (fleet == null || planet == null) {
            throw new NotifySBUserException("You should chose existing entities.");
        }
        int calculatedDistance = DistanceCalculator.calculateDistance(fleet, planet);
        return calculatedDistance;
    }

    public Move moveFleet(final int idFleet, final int idPlanet) {
        Preconditions.checkState(idFleet < 1, "idFleet shouldn't be null!");
        Preconditions.checkState(idPlanet < 1, "idPlanet shouldn't be null!");

        Fleet fleet = fleetR.findById(idFleet).orElse(null);
        Planet planet = planetR.findById(idPlanet).orElse(null);
        if (fleet == null || planet == null) {
            throw new NotifySBUserException("You should chose existing entities.");
        }
        int calculatedDistance = DistanceCalculator.calculateDistance(fleet, planet);
        Move move = new Move(fleet, planet, calculatedDistance);
        moveR.save(move);
        return move;
    }

    public List<Fleet> findAllFleets() {
        return fleetR.findAllFleets();
    }

    public Fleet findById(int idFleet) {
        return fleetR.findById(idFleet).orElse(null);
    }

    public void save(Fleet fleet) {
        fleetR.save(fleet);
    }

    public void delete(Fleet fleet) {
        fleetR.delete(fleet);
    }
}
