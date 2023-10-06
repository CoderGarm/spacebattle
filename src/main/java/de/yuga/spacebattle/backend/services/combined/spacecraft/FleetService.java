package de.yuga.spacebattle.backend.services.combined.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.CombatAllowanceCalculator;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.repositories.combined.spacecraft.FleetRepository;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMerge;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMergeResult;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetSplit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final StarSystemService starSystemService;

    @Autowired
    public FleetService(@Nonnull final FleetRepository fleetRepository,
                        @Nonnull final WarShipService warShipService,
                        @Nonnull final PlanetService planetService,
                        @Nonnull final StarSystemService starSystemService) {
        this.fleetRepository = Preconditions.checkNotNull(fleetRepository, "fleetR shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.starSystemService = Preconditions.checkNotNull(starSystemService, "starSystemService must not be empty");
    }

    /**
     * Merges the second fleet into the first.
     */
    @Nonnull
    public FleetMergeResult mergeFleets(@Nonnull final FleetMerge merge, final int idUser) {
        Preconditions.checkNotNull(merge, "merge must not be empty");


        final Map<Integer, List<Integer>> fleetConstellations = merge.getFleetConstellations();
        final List<Fleet> fleets = findByIds(fleetConstellations.keySet());
        final Set<WarShip> knownWarShips = fleets.stream().map(Fleet::getAliveShips).flatMap(Collection::stream).collect(Collectors.toSet());
        final Set<Integer> poolShipsToFetch = fleetConstellations.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        poolShipsToFetch.removeAll(knownWarShips.stream().map(AbstractEntityKey::getId).collect(Collectors.toSet()));

        final List<WarShip> pooledShips = warShipService.findByIds(poolShipsToFetch);
        knownWarShips.addAll(pooledShips);

        final Set<Fleet> foreign = fleets.stream().filter(f -> f.getOwner().getId() != idUser).collect(Collectors.toSet());
        if (!foreign.isEmpty()) {
            throw new NotifyWebUserException("You must own all the merged fleets.");
        }

        final Map<Integer, WarShip> warshipsById = knownWarShips.stream().collect(Collectors.toMap(WarShip::getId, Function.identity()));
        fleetConstellations.forEach((idFleet, warshipIDs) -> {
            final Fleet fleet = fleets.stream().filter(f -> f.getId() == idFleet).findFirst().orElse(null);
            assert fleet != null : "Would be good at this stage.";
            final Set<WarShip> toAdd = warshipsById.values().stream().filter(w -> warshipIDs.contains(w.getId())).collect(Collectors.toSet());
            fleet.addShips(toAdd);
        });

        Set<Fleet> toStore = fleets.stream().filter(f -> !f.getAliveShips().isEmpty()).collect(Collectors.toSet());
        saveAll(toStore);
        final Set<Fleet> toMarkAsDeleted = fleets.stream().filter(f -> f.getAliveShips().isEmpty()).collect(Collectors.toSet());
        markAsDestroyed(toMarkAsDeleted);
        toStore = calculateFleetState(toStore.stream().map(AbstractEntityKey::getId).collect(Collectors.toSet()));
        return new FleetMergeResult(toStore, toMarkAsDeleted);
    }

    @Nonnull
    public Set<Fleet> splitFleets(@Nonnull final FleetSplit fleetSplit, final int idUser) {
        Preconditions.checkNotNull(fleetSplit, "fleetSplit must not be empty");

        final Set<Integer> warShipIDs = fleetSplit.getFleetConstellations().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        if (warShipIDs.isEmpty()) {
            return Set.of();
        }
        final List<WarShip> shipsByUser = warShipService.findByIds(warShipIDs);
        final Set<Integer> fleetIDs = shipsByUser.stream().map(WarShip::getFleet).filter(Objects::nonNull).map(AbstractEntityKey::getId).collect(Collectors.toSet());

        if (shipsByUser.stream().anyMatch(w -> w.getShipClass().getOwner().getId() != idUser)) {
            throw new NotifyWebUserException("You must not change foreign fleets, you little Hax0r!");
        }

        final Owner owner = shipsByUser.stream()
                .findFirst()
                .map(WarShip::getShipClass)
                .map(ShipClass::getOwner)
                .orElseThrow(() -> new NotifyWebUserException("Every fleet must have an owner."));

        final de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit orbit = fleetSplit.getOrbit();
        if (orbit.getOrbit() == null || orbit.getSystem() == null) {
            throw new NotifyWebUserException("Sorry, but this will not work. You need a place to be!");
        }
        final StarSystem starSystem = starSystemService.find(orbit.getSystem().getIdStarSystem());
        final FleetOrbit fleetOrbit = new FleetOrbit(new Orbit(orbit.getOrbit()), starSystem);
        final Planet planet = planetService.findByCoordinates(fleetOrbit);
        assert planet != null;

        final Set<Fleet> result = new HashSet<>();
        fleetSplit.getFleetConstellations().forEach((name, warshipIDs) -> {
            // the name is followed by a negative number to make the names unique
            name = name.split("-")[0];
            final Set<WarShip> warShips = shipsByUser.stream().filter(w -> warshipIDs.contains(w.getId())).collect(Collectors.toSet());
            final Fleet fleet = createFleet(owner, planet, name);
            warShips.forEach(w -> w.setFleet(fleet));
            warShipService.saveAll(warShips);

            result.add(fleetRepository.findById(fleet.getId()).orElseThrow(NullPointerException::new));
        });

        final Set<Fleet> newStatedFleets = calculateFleetState(fleetIDs);

        final Set<Fleet> known = result.stream().filter(newStatedFleets::contains).collect(Collectors.toSet());
        final Set<Fleet> changed = newStatedFleets.stream().filter(known::contains).collect(Collectors.toSet());
        result.removeAll(changed);
        result.addAll(changed);

        return result;
    }

    @Nonnull
    public Fleet createFleet(@Nonnull final Owner user, @Nonnull final Planet planet, @Nonnull final String name) {
        Preconditions.checkNotNull(user, "user must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(name, "name must not be empty");

        final FleetOrbit fleetOrbit = new FleetOrbit(planet.getOrbit(), planet.getSystem());
        final Fleet fleet = new Fleet(name, user, fleetOrbit);
        fleet.setOperational();
        return save(fleet);
    }

    @Nonnull
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
    public Set<Fleet> cancelFlights(final int idUser, @Nonnull final List<Integer> fleetIds) {
        Preconditions.checkNotNull(fleetIds, "fleetIds must not be empty");

        final Set<Fleet> cancelled = new HashSet<>();
        final Iterable<Fleet> fleets = fleetRepository.findAllById(fleetIds);
        for (final Fleet fleet : fleets) {
            if (fleet != null) {
                if (idUser == fleet.getOwner().getId()) {
                    cancelled.add(cancelFlight(fleet));
                    continue;
                }
                throw new NotifyWebUserException("You cannot cancel this flight.");
            } else {
                throw new NotifyWebUserException("Nothing to see here.");
            }
        }
        return cancelled;
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

    @Nonnull
    public List<Fleet> findAllAliveFleets() {
        return Objects.requireNonNullElse(fleetRepository.findAllAliveFleets(), new ArrayList<>());
    }

    @Nonnull
    public List<Fleet> findAllAliveFleetsInSystems(@Nonnull final Collection<Integer> systemIds) {
        Preconditions.checkNotNull(systemIds, "systemIds must not be empty");

        return Objects.requireNonNullElse(fleetRepository.findAllAliveFleetsInSystems(systemIds), new ArrayList<>());
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
            if (fleet.getAliveShips().isEmpty()) {
                toRemove.add(fleet);
            }
        });

        toRemove.forEach(Fleet::delete);
        fleetRepository.saveAll(toRemove);
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
    public List<Fleet> findByIds(@Nonnull final Collection<Integer> fleetIDs) {
        Preconditions.checkNotNull(fleetIDs, "fleetIDs must not be empty");

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

    public void markAsInoperational(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        fleet.setOperational(false);
        save(fleet);
    }

    public void markAsDestroyed(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        fleet.delete();
        fleet.getAliveShips().forEach(s -> s.setFleet(null));
        fleetRepository.save(fleet);
    }

    public void markAsDestroyed(@Nonnull final Collection<Fleet> fleets) {
        Preconditions.checkNotNull(fleets, "fleets shouldn't be null!");

        fleets.forEach(Fleet::delete);
        fleetRepository.saveAll(fleets);
    }

    @Nonnull
    public List<Fleet> findAllFleetsByUser(@Nonnull final Owner owner) {
        Preconditions.checkNotNull(owner, "owner must not be empty");

        return fleetRepository.findAllFleetsBy(owner.getId());
    }

    @Nonnull
    public List<Fleet> findAllFleetsWithoutMovementByUser(final int idOwner) {
        return Objects.requireNonNullElse(fleetRepository.findAllFleetsWithoutMovementByUser(idOwner), new ArrayList<>());
    }

    @Nonnull
    public List<Fleet> findAllFleetsByUser(final int idUser) {
        return fleetRepository.findAllFleetsBy(idUser);
    }

    @Nonnull
    public List<AbstractId> findAllAliveFleetsBy(final int idUser) {
        return Objects.requireNonNullElse(fleetRepository.findAllAliveFleetsBy(idUser), new ArrayList<>());
    }

    @Nonnull
    public List<Fleet> findAllFleetsBy(final int idStarSystem, final int idOwner) {
        return fleetRepository.findAllFleetsByStarSystemAndOwner(idStarSystem, idOwner);
    }

    @Nonnull
    public Set<Fleet> findAllAnchoredForPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return fleetRepository.findAllAnchoredForPlanet(planet);
    }

    public boolean isShipClassInUse(final int idShipClass) {
        return fleetRepository.isShipClassInUse(idShipClass);
    }

    @Nonnull
    public List<Fleet> findAllFleetsWithInterstellarMovement(final int idUser) {
        return fleetRepository.findAllFleetsWithInterstellarMovement(idUser);
    }

    @Nonnull
    public List<FleetClash> findAllFleetClashes() {
        return fleetRepository.findAllFleetClashes();
    }

    @Nullable
    public FleetClash findFleetClashesAtPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Set<Fleet> allFleetsByPlanet = fleetRepository.findAllFleetsByPlanet(planet);
        final Set<Owner> users = allFleetsByPlanet.stream().map(Fleet::getOwner).collect(Collectors.toSet());
        if (!CombatAllowanceCalculator.isCombatAllowed(users)) {
            // todo implement 3-way combat anyhow
            return null;
        }
        final Map<FleetOrbit, List<Fleet>> fleetsByOrbit = allFleetsByPlanet.stream()
                .filter(f -> Objects.nonNull(f.getOrbit()))
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

    @Nonnull
    public Set<Fleet> saveAll(@Nonnull final Collection<Fleet> fleets) {
        Preconditions.checkNotNull(fleets, "fleets shouldn't be null!");

        final Iterable<Fleet> saveAll = fleetRepository.saveAll(fleets);
        return StreamSupport.stream(saveAll.spliterator(), false).collect(Collectors.toSet());
    }

    /**
     * Sends all the warships to the pool.
     */
    public void poolWarships(final int idUser, @Nonnull final List<Integer> warshipIDs) {
        Preconditions.checkNotNull(warshipIDs, "warshipIDs must not be empty");

        final Set<WarShip> warShips = warShipService.findByIds(warshipIDs).stream()
                .filter(w -> w.getShipClass().getOwner().getId() == idUser)
                .collect(Collectors.toSet());

        final Set<Integer> fleetIDs = warShips.stream().map(WarShip::getFleet).filter(Objects::nonNull).map(AbstractEntityKey::getId).collect(Collectors.toSet());

        warShips.forEach(WarShip::sendToPool);
        warShipService.saveAll(warShips);
        calculateFleetState(fleetIDs);
    }

    @Nonnull
    private Set<Fleet> calculateFleetState(@Nonnull final Set<Integer> fleetIDs) {
        Preconditions.checkNotNull(fleetIDs, "fleetIDs must not be empty");

        final List<Fleet> fleets = findAll(fleetIDs);
        final Set<Fleet> toSetOperational = fleets.stream().filter(fleet -> {
            final boolean isOperational = fleet.getAliveShips().stream().allMatch(WarShip::isOperational);
            return isOperational && !fleet.isOperationalFromSuper();
        }).collect(Collectors.toSet());
        toSetOperational.forEach(Fleet::setOperational);
        return Objects.requireNonNullElse(saveAll(toSetOperational), new HashSet<>());
    }

    @Nonnull
    public Set<WarShip> findPooledWarships(final int idUser) {
        return warShipService.findPooledShipsByUser(idUser);
    }

    @Nonnull
    public List<Fleet> findAll(@Nonnull final Collection<Integer> idFleets) {
        Preconditions.checkNotNull(idFleets, "idFleets must not be empty");

        return Objects.requireNonNullElse(fleetRepository.findAllById(idFleets), new ArrayList<>());
    }

    @Nonnull
    public Set<StarSystem> findMovementDestinationsAndSojourns() {
        final Set<StarSystem> systems = Objects.requireNonNullElse(fleetRepository.findSojourns(), new HashSet<>());
        systems.addAll(Objects.requireNonNullElse(fleetRepository.findMovementDestinations(), new HashSet<>()));
        return systems;
    }
}
