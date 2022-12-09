package de.yuga.spacebattle.backend.services.combined.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.repositories.combined.spacecraft.FleetRepository;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FleetService {

    @Nonnull
    private final FleetRepository fleetRepository;

    @Autowired
    public FleetService(@Nonnull final FleetRepository fleetRepository) {
        Preconditions.checkNotNull(fleetRepository, "fleetR shouldn't be null!");

        this.fleetRepository = fleetRepository;
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
            throw new NotifyWebUserException("That's not possible, no.");
        }
        fleetsToMerge.stream().filter(fl -> !fl.getAllShips().isEmpty()).forEach(fleet2 -> {
            final Set<WarShip> ships = fleet2.getAllShips();

            baseFleet.updateShips(ships);
            fleet2.getShipsByClass().clear();
        });
        fleetRepository.save(baseFleet);
        markAsDestroyed(fleetsToMerge);
        return baseFleet;
    }

    public List<Fleet> moveFleets(@Nonnull final List<Move> moves) {
        Preconditions.checkNotNull(moves, "moves shouldn't be null!");

        final List<Fleet> fleets = moves.stream().map(move -> {
            // set the move
            final Fleet fleet = move.getFleet();
            fleet.setMove(move);
            return fleet;
        }).collect(Collectors.toList());

        fleetRepository.saveAll(fleets);
        return fleets;
    }

    @Nonnull
    public Fleet cancelFlight(final int idUser, final int idFleet) {
        final Fleet fleet = fleetRepository.findById(idFleet).orElse(null);
        if (fleet != null) {
            if (idUser == fleet.getOwner().getId()) {
                return cancelFlight(fleet);
            }
            throw new NotifyWebUserException("You cannot cancel this flight.");
        } else {
            throw new NotifyWebUserException("Nothing to see here.");
        }
    }

    /**
     * Cancels a running flight and heads the fleet back to their origin.
     * <p>
     * Only possible in planetary systems due the lack of communication on deep space missions.
     *
     * @param fleet the fleet which has to be flown back
     * @return the fleet with the new movement
     */
    public Fleet cancelFlight(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkArgument(fleet.getMove() != null, "fleet's move shouldn't be null!");

        final Move move = fleet.getMove();
        final FleetOrbit targetOrbit = move.getDestinationOrbit();
        final StarSystem targetSystem = move.getDestinationOrbit().getSystem();
        final Orbit targetOrbitOrbit = targetOrbit.getOrbit();

        final FleetOrbit startOrbit = move.getOriginOrbit();
        final StarSystem startSystem = move.getOriginOrbit().getSystem();
        final Orbit startOrbitOrbit = startOrbit.getOrbit();

        if (targetOrbitOrbit == null || startOrbitOrbit == null) {
            throw new NotifyWebUserException("A movement must always have a beginning and a designated target.");
        }
        if (targetSystem == null || startSystem == null) {
            throw new NotifyWebUserException("Sorry, but you can only cancel flights inside of a star system");
        }
        if (!targetSystem.equals(startSystem)) {
            throw new NotifyWebUserException("Sorry, but you can only cancel flights inside the same system");
        }

        // permute origin and destination
        return cancelFlight(fleet, targetOrbitOrbit, startOrbitOrbit, startSystem);
    }

    /**
     * Sets a fleet in motion to the target. From an origin which differs from the currents fleet's position.
     * That means that this should be used for things like "cancel a flight" but it can be used accidentally as god-like jump-drive.
     * Pay attention.
     *
     * @param fleet  the fleet which knows it's position
     * @param start  the start planet if it is different from the fleets current position
     * @param target the target
     * @param system the system in which the fleet will be relocated
     * @return the fleet in motion
     */
    private Fleet cancelFlight(@Nonnull final Fleet fleet,
                               @Nonnull final Orbit start,
                               @Nonnull final Orbit target,
                               @Nonnull final StarSystem system) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(start, "start shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");
        Preconditions.checkNotNull(system, "system shouldn't be null!");
        Preconditions.checkArgument(fleet.getMove() != null, "fleet's move shouldn't be null!");

        final int moveDoneAtZero = fleet.getMove().getMoveDoneAtZero();
        final FleetOrbit origin = new FleetOrbit(start, system);
        final FleetOrbit destination = new FleetOrbit(target, system);

        fleet.setOrbit(new FleetOrbit(start, system));
        int calculateTimeToTravel = DistanceCalculator.calculateTimeToTravel(fleet, origin, destination);

        final int alreadyTravelled = calculateTimeToTravel - moveDoneAtZero;
        if (calculateTimeToTravel - alreadyTravelled <= 0) {
            calculateTimeToTravel = 0;
        } else {
            calculateTimeToTravel = alreadyTravelled;
        }

        if (calculateTimeToTravel > 0) {
            // set move
            final Move move = new de.yuga.spacebattle.backend.entities.turn.Move(fleet, destination, calculateTimeToTravel, calculateTimeToTravel + moveDoneAtZero);
            fleet.setMove(move);
        } else {
            // set fleet in planetary orbit
            fleet.setMove(null);
            fleet.setOrbit(new FleetOrbit(target, system));
        }
        fleetRepository.save(fleet);
        return fleet;
    }

    @Nonnull
    public List<Fleet> findAllFleets() {
        return fleetRepository.findAllFleets();
    }

    /**
     * Removes all fleets without warships.
     *
     * @param fleets the fleets to check and possibly remove
     */
    public void markFleetsWithoutShipsAsDeleted(@Nonnull final List<Fleet> fleets) {
        Preconditions.checkNotNull(fleets, "fleets shouldn't be null!");

        final List<Fleet> toRemove = new ArrayList<>();
        final Iterable<Fleet> allById = fleetRepository.findAllById(fleets.stream().map(Fleet::getId).collect(Collectors.toList()));
        allById.forEach(fleet -> {
            if (!fleet.isAlive()) {
                toRemove.add(fleet);
            }
        });

        toRemove.forEach(Fleet::delete);
        fleetRepository.saveAll(toRemove);
    }

    @Nonnull
    public Set<Fleet> findAllFleetsWithoutInterstellarMovement() {
        return fleetRepository.findAllFleetsWithoutInterstellarMovement();
    }

    @Nonnull
    public List<Fleet> findAllFleetsWithMovement(final int idUser) {
        return fleetRepository.findAllFleetsWithMovement(idUser);
    }

    @Nullable
    public Fleet find(int idFleet) {
        return fleetRepository.findById(idFleet).orElse(null);
    }

    @Nonnull
    public List<Fleet> findByIds(List<Integer> fleetIDs) {
        final Iterable<Fleet> allById = fleetRepository.findAllById(fleetIDs);
        return StreamSupport.stream(allById.spliterator(), false).collect(Collectors.toList());
    }

    public Fleet find(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return fleetRepository.findById(fleet.getId()).orElse(null);
    }

    public Fleet save(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return fleetRepository.save(fleet);
    }

    public void markAsDestroyed(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        fleet.delete();
        fleetRepository.save(fleet);
    }

    public void markAsDestroyed(@Nonnull final Collection<Fleet> fleets) {
        Preconditions.checkNotNull(fleets, "fleets shouldn't be null!");

        fleets.forEach(Fleet::delete);
        fleetRepository.saveAll(fleets);
    }

    @Nonnull
    public List<Fleet> findAllFleetsByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return fleetRepository.findAllFleetsBy(user);
    }

    @Nonnull
    public List<Fleet> findAllFleetsBy(final int idStarSystem, final int idOwner) {
        return fleetRepository.findAllFleetsByStarSystemAndOwner(idStarSystem, idOwner);
    }

    @Nonnull
    public Set<Fleet> findAllFleetsByPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return fleetRepository.findAllFleetsByPlanet(planet);
    }

    @Nonnull
    public Set<Fleet> findAllDamagedFleetsByPlanetAndOwner(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return fleetRepository.findAllDamagedFleetsByPlanetAndOwner(planet);
    }

    public boolean isShipClassInUse(final int idShipClass) {
        return fleetRepository.isShipClassInUse(idShipClass);
    }

    @Nonnull
    public List<Fleet> findAllFleetsWithInterstellarMovement() {
        return fleetRepository.findAllFleetsWithInterstellarMovement();
    }

    @Nonnull
    public List<FleetClash> findAllFleetClashes() {
        return fleetRepository.findAllFleetClashes();
    }

    @Nullable
    public FleetClash findFleetClashesAtPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Set<Fleet> allFleetsByPlanet = fleetRepository.findAllFleetsByPlanet(planet);
        final Set<User> users = allFleetsByPlanet.stream().map(Fleet::getOwner).collect(Collectors.toSet());
        if (users.size() != 2) {
            // todo implement 3-way combat anyhow
            return null;
        }
        final Map<FleetOrbit, List<Fleet>> fleetsByOrbit = allFleetsByPlanet.stream()
                .collect(Collectors.groupingBy(Fleet::getOrbit, Collectors.mapping(Function.identity(), Collectors.toList())));
        if (fleetsByOrbit.size() > 1) {
            throw new NotifyWebUserException("There cannot be more than one orbit for a single planet.");
        }
        if (fleetsByOrbit.isEmpty()) {
            return null;
        }
        final List<FleetClash> clashes = new ArrayList<>();
        fleetsByOrbit.entrySet().forEach(entry -> clashes.add(new FleetClash(entry)));
        return clashes.get(0);
    }

    public List<Fleet> saveAll(@Nonnull final Collection<Fleet> fleets) {
        Preconditions.checkNotNull(fleets, "fleets shouldn't be null!");

        final Iterable<Fleet> saveAll = fleetRepository.saveAll(fleets);
        return StreamSupport.stream(saveAll.spliterator(), false).collect(Collectors.toList());
    }
}
