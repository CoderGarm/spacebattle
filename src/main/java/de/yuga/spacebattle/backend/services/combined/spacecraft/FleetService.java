package de.yuga.spacebattle.backend.services.combined.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.misc.Operationable;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.TransportJob;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.repositories.combined.spacecraft.FleetRepository;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.GameEventService;
import de.yuga.spacebattle.backend.services.turn.MoveService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import de.yuga.spacebattle.backend.services.turn.TransportJobService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMerge;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetMergeResult;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.FleetSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit.MATH_CONTEXT_MORE_PRECISION;

@Service
public class FleetService {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(FleetService.class);

    @Nonnull
    private final FleetRepository fleetRepository;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final TickTimeService tickTimeService;

    @Nonnull
    private final MoveService moveService;

    @Nonnull
    private final TransportJobService transportJobService;

    @Autowired
    public FleetService(@Nonnull final FleetRepository fleetRepository,
                        @Nonnull final WarShipService warShipService,
                        @Nonnull final PlanetService planetService,
                        @Nonnull final TickTimeService tickTimeService,
                        @Nonnull final MoveService moveService,
                        @Nonnull final TransportJobService transportJobService) {
        this.fleetRepository = Preconditions.checkNotNull(fleetRepository, "fleetR shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.tickTimeService = Preconditions.checkNotNull(tickTimeService, "tickTimeService must not be empty");
        this.moveService = Preconditions.checkNotNull(moveService, "moveService must not be empty");
        this.transportJobService = Preconditions.checkNotNull(transportJobService, "transportJobService must not be empty");
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

        final Map<Fleet, Integer> interceptHas = fleets.stream().collect(Collectors.toMap(Function.identity(), f -> f.getAliveShips().size()));
        final Map<Fleet, Integer> interceptGains = new HashMap<>();

        final Set<TransportJob> toRemove = new HashSet<>();
        final Map<Integer, WarShip> warshipsById = knownWarShips.stream().collect(Collectors.toMap(WarShip::getId, Function.identity()));
        fleetConstellations.forEach((idFleet, warshipIDs) -> {
            final Fleet fleet = fleets.stream().filter(f -> f.getId() == idFleet).findFirst().orElse(null);
            assert fleet != null : "Would be good at this stage.";
            final Set<WarShip> toAdd = warshipsById.values().stream().filter(w -> warshipIDs.contains(w.getId())).collect(Collectors.toSet());
            fleet.addShips(toAdd);

            final boolean isNotOwnPlanet = fleet.getOrbit() == null || fleet.getOrbit().getPlanet() == null
                    || fleet.getOrbit().getPlanet().getOwner() == null || !fleet.getOrbit().getPlanet().getOwner().equals(fleet.getOwner());
            if (fleet.getName().startsWith(GameEventService.INTERCEPT_PREFIX)
                    && isNotOwnPlanet) {
                final int amount = interceptGains.getOrDefault(fleet, 0) + toAdd.size();
                interceptGains.put(fleet, amount);
            }

            final Set<TransportJob> transportJobs = toAdd.stream().map(WarShip::getTransportJob).filter(Objects::nonNull).collect(Collectors.toSet());
            toAdd.forEach(w -> w.setFleet(fleet));

            toAdd.forEach(ws -> transportJobs.forEach(t -> t.getShips().remove(ws)));
            toRemove.addAll(transportJobs.stream().filter(t -> t.getShips().isEmpty()).collect(Collectors.toSet()));
        });

        Set<Fleet> toStore = fleets.stream().filter(f -> !f.getAliveShips().isEmpty()).collect(Collectors.toSet());
        saveAll(toStore);
        final Set<Fleet> toMarkAsDeleted = fleets.stream().filter(f -> f.getAliveShips().isEmpty()).collect(Collectors.toSet());
        markAsDestroyed(toMarkAsDeleted);
        toStore = calculateFleetState(toStore.stream().map(AbstractEntityKey::getId).collect(Collectors.toSet()));

        interceptGains.forEach((fleet, newAmount) -> {
            final Integer hasHadBefore = interceptHas.getOrDefault(fleet, 0);
            LOGGER.info(GameEventService.WAR_HARVEST_2023_PREFIX + " Reinforced fleet '{}' of '{}' from '{}' to '{}' ships.",
                    fleet.getName(),
                    fleet.getOwner().getUsername(),
                    hasHadBefore,
                    newAmount);
        });

        transportJobService.deleteAll(toRemove);
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

        Planet planet = shipsByUser.stream().map(WarShip::getMothball).filter(Objects::nonNull).findFirst().orElse(null);
        if (planet == null) {
            planet = shipsByUser.stream().map(WarShip::getTransportJob).filter(Objects::nonNull).map(TransportJob::getOrigin).findFirst().orElse(null);
        }
        if (planet == null) {
            final FleetOrbit fleetOrbit = shipsByUser.stream()
                    .map(WarShip::getFleet)
                    .filter(Objects::nonNull)
                    .map(Fleet::getOrbit)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(() -> new NotifyWebUserException("Pretty sad to find no home."));
            planet = fleetOrbit.getPlanet();
        }

        final Set<TransportJob> toRemove = new HashSet<>();
        final Set<Fleet> result = new HashSet<>();
        for (final String key : fleetSplit.getFleetConstellations().keySet()) {
            final List<Integer> warshipIDs = fleetSplit.getFleetConstellations().get(key);

            // the name is followed by a negative number to make the names unique
            final String name = key.split("-")[0];
            final Set<WarShip> warShips = shipsByUser.stream().filter(w -> warshipIDs.contains(w.getId())).collect(Collectors.toSet());
            final Fleet fleet = createFleet(owner, Objects.requireNonNull(planet), name);

            final Set<TransportJob> transportJobs = warShips.stream().map(WarShip::getTransportJob).filter(Objects::nonNull).collect(Collectors.toSet());

            warShips.forEach(w -> {
                transportJobs.forEach(t -> t.getShips().remove(w));
                w.setFleet(fleet);
            });
            warShipService.saveAll(warShips);

            result.add(fleetRepository.findById(fleet.getId()).orElseThrow(NullPointerException::new));
            toRemove.addAll(transportJobs.stream().filter(t -> t.getShips().isEmpty()).collect(Collectors.toSet()));
        }

        final Set<Fleet> newStatedFleets = calculateFleetState(fleetIDs);

        final Set<Fleet> known = result.stream().filter(newStatedFleets::contains).collect(Collectors.toSet());
        final Set<Fleet> changed = newStatedFleets.stream().filter(known::contains).collect(Collectors.toSet());
        result.removeAll(changed);
        result.addAll(changed);

        transportJobService.deleteAll(toRemove);
        return result;
    }

    @Nonnull
    public Fleet createFleet(@Nonnull final Owner user, @Nonnull final Planet planet, @Nonnull final String name) {
        Preconditions.checkNotNull(user, "user must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(name, "name must not be empty");

        final FleetOrbit fleetOrbit = new FleetOrbit(planet);
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

        return fleetRepository.saveAll(fleets);
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
        final int moveDoneAtZero = move.getTicksLeft();

        final FleetOrbit origin = move.getOriginOrbit();
        final FleetOrbit destination = move.getDestinationOrbit();

        int calculateTimeToTravel = DistanceCalculator.calculateTimeToTravel(fleet, origin, destination);

        final int alreadyTravelled = calculateTimeToTravel - moveDoneAtZero;
        if (calculateTimeToTravel - alreadyTravelled <= 0) {
            calculateTimeToTravel = 0;
        } else {
            calculateTimeToTravel = alreadyTravelled;
        }

        final Tick today = tickTimeService.getToday();
        if (calculateTimeToTravel > 0) {
            // set move
            fleet.setMove(new Move(today, fleet, destination, calculateTimeToTravel, calculateTimeToTravel + moveDoneAtZero));
            fleet.setOrbit(null);
        } else {
            // set fleet in planetary orbit
            fleet.setMove(null);
            fleet.setOrbit(origin);
        }

        move.setFinished(today);
        moveService.save(move);
        return fleetRepository.save(fleet);
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
    public List<Fleet> forDeletionFindAllFleetsByUser(@Nonnull final Owner owner) {
        Preconditions.checkNotNull(owner, "owner must not be empty");

        return Objects.requireNonNullElse(fleetRepository.forDeletionFindAllFleetsByUser(owner.getId()), new ArrayList<>());
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

    @Nonnull
    public List<Fleet> findAllFleetsWithoutMovement() {
        return fleetRepository.findAllFleetsWithoutMovement();
    }

    @Nonnull
    public Set<Fleet> findAllFleetsByPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        return fleetRepository.findAllFleetsByPlanet(planet);
    }

    @Nonnull
    public Set<Fleet> saveAll(@Nonnull final Collection<Fleet> fleets) {
        Preconditions.checkNotNull(fleets, "fleets shouldn't be null!");

        final Iterable<Fleet> saveAll = fleetRepository.saveAll(fleets);
        return StreamSupport.stream(saveAll.spliterator(), false).collect(Collectors.toSet());
    }

    @Nonnull
    public Set<WarShip> findPooledWarships(final int idUser, @Nullable final Integer idPlanet) {
        return warShipService.findPooledShipsByUser(idUser, idPlanet);
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

    /**
     * Sends all the warships to the pool.
     */
    public void poolWarships(final int idUser, @Nonnull final List<Integer> warshipIDs) {
        Preconditions.checkNotNull(warshipIDs, "warshipIDs must not be empty");

        final Set<WarShip> warShips = warShipService.findByIds(warshipIDs).stream()
                .filter(w -> w.getShipClass().getOwner().getId() == idUser)
                .collect(Collectors.toSet());

        final Set<Integer> fleetIDs = warShips.stream().map(WarShip::getFleet).filter(Objects::nonNull).map(AbstractEntityKey::getId).collect(Collectors.toSet());

        warShips.forEach(this::mothballShip);
        warShipService.saveAll(warShips);
        warShips.forEach(this::transferCrewToPlanet);
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

    public void retire(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip must not be empty");

        warShip.delete();
        final ResourceDeposit costs = warShip.getShipClass().getCosts();
        final CrewRequirement crewRequirement = costs.getCrewRequirement();
        final Planet shipyard = warShip.getShipyard();
        shipyard.getResourceDeposit().updateCrew(crewRequirement, ECalculationType.ADD);
        costs.getResources().forEach((resourceType, amount) -> {
            final long cashBack = BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(0.5), MATH_CONTEXT_MORE_PRECISION).longValue();
            shipyard.getResourceDeposit().updateResource(resourceType, cashBack);
        });
        planetService.save(shipyard);
        warShipService.save(warShip);
    }

    public void mothballShip(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip must not be empty");
        Preconditions.checkNotNull(warShip.getFleet(), "warShip.getFleet() must not be empty");
        Preconditions.checkNotNull(warShip.getFleet().getOrbit(), "warShip.getFleet().getOrbit() must not be empty");
        Preconditions.checkNotNull(warShip.getFleet().getOrbit().getPlanet(), "warShip.getFleet().getOrbit().getPlanet() must not be empty");

        warShip.setMothball(warShip.getFleet().getOrbit().getPlanet());
        warShipService.save(warShip);
        transferCrewToPlanet(warShip);
    }

    private void transferCrewToPlanet(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip must not be empty");

        final CrewRequirement crewRequirement = warShip.getShipClass().getCosts().getCrewRequirement();
        final Planet shipyard = warShip.getShipyard();
        shipyard.getResourceDeposit().updateCrew(crewRequirement, ECalculationType.ADD);
        planetService.save(shipyard);
    }

    public void disableFleet(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        markAsInoperational(fleet);
        final Set<WarShip> ships = fleet.getAliveShips();
        warShipService.markAsInoperational(ships);
    }

    public void transferCrewToPlanet(@Nonnull final Fleet fleet, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final ResourceDeposit costs = new ResourceDeposit(EDepositType.COSTS);
        fleet.getAliveShips().stream().map(WarShip::getShipClass).map(ShipClass::getCosts).forEach(rd -> costs.updateCrew(rd.getCrewRequirement(), ECalculationType.ADD));
        planet.getResourceDeposit().updateCrew(costs.getCrewRequirement(), ECalculationType.ADD);
        planetService.save(planet);
    }

    public void operateShips(@Nonnull final List<Integer> orderOperational, @Nonnull final List<Integer> orderInoperational) {
        Preconditions.checkNotNull(orderOperational, "orderOperational must not be empty");
        Preconditions.checkNotNull(orderInoperational, "orderInoperational must not be empty");

        final List<WarShip> warShips = fetchAllShips(orderOperational, orderInoperational);
        final Planet planet = identifyPlanet(warShips);
        if (planet != null) {
            final Set<WarShip> toStore = inoperateShips(orderInoperational, warShips, planet);
            toStore.addAll(operateShips(orderOperational, warShips, planet));
            warShipService.saveAll(toStore);
            planetService.save(planet);
        }
        final Set<Integer> fleetIDs = warShips.stream().map(WarShip::getFleet).filter(Objects::nonNull).map(Fleet::getId).collect(Collectors.toSet());
        calculateFleetState(fleetIDs);
    }

    @Nonnull
    private List<WarShip> fetchAllShips(@Nonnull final List<Integer> orderOperational, @Nonnull final List<Integer> orderInoperational) {
        Preconditions.checkNotNull(orderOperational, "orderOperational must not be empty");
        Preconditions.checkNotNull(orderInoperational, "orderInoperational must not be empty");

        final HashSet<Integer> ids = new HashSet<>(orderOperational);
        ids.addAll(orderInoperational);
        return warShipService.findByIds(ids);
    }

    @Nullable
    private Planet identifyPlanet(@Nonnull final List<WarShip> warShips) {
        Preconditions.checkNotNull(warShips, "warShips must not be empty");

        Planet planet = warShips.stream()
                .map(WarShip::getMothball)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (planet == null) {
            final FleetOrbit fleetOrbit = warShips.stream()
                    .map(WarShip::getFleet)
                    .filter(Objects::nonNull)
                    .map(Fleet::getOrbit)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(NullPointerException::new);
            planet = fleetOrbit.getPlanet();
        }
        return planet;
    }

    @Nonnull
    private Set<WarShip> operateShips(@Nonnull final List<Integer> orderOperational, @Nonnull final List<WarShip> warShips, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(orderOperational, "orderOperational must not be empty");
        Preconditions.checkNotNull(warShips, "warShips must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Set<WarShip> toStore = new HashSet<>();
        final Set<WarShip> toOps = warShips.stream()
                .filter(w -> orderOperational.contains(w.getId()))
                .filter(w -> !w.isOperational())
                .collect(Collectors.toSet());
        for (final WarShip toOp : toOps) {
            final CrewRequirement crewRequirement = toOp.getShipClass().getCosts().getCrewRequirement();
            if (planet.getResourceDeposit().isPayingPossible(crewRequirement).isValid()) {
                toOp.setOperational(true);
                planet.getResourceDeposit().updateCrew(crewRequirement, ECalculationType.SUBTRACT);
                toStore.add(toOp);
            }
        }
        return toStore;
    }

    @Nonnull
    private Set<WarShip> inoperateShips(@Nonnull final List<Integer> orderInoperational, @Nonnull final List<WarShip> warShips, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(orderInoperational, "orderInoperational must not be empty");
        Preconditions.checkNotNull(warShips, "warShips must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Set<WarShip> toStore = new HashSet<>();
        final Set<WarShip> toInOps = warShips.stream()
                .filter(w -> orderInoperational.contains(w.getId()))
                .filter(Operationable::isOperational)
                .collect(Collectors.toSet());
        for (final WarShip toInOp : toInOps) {
            toInOp.setOperational(false);
            final CrewRequirement crewRequirement = toInOp.getShipClass().getCosts().getCrewRequirement();
            planet.getResourceDeposit().updateCrew(crewRequirement, ECalculationType.ADD);
            toStore.add(toInOp);
        }
        return toStore;
    }

    @Nonnull
    public Set<Integer> findAllSystemIDsWithFleetsForUser(final int idUser) {
        return Objects.requireNonNullElse(fleetRepository.findAllSystemIDsWithFleetsForUser(idUser), new HashSet<>());
    }

    public void executeTransferPooledWarship(@Nonnull final Owner owner,
                                             @Nonnull final Set<WarShip> warShips,
                                             @Nonnull final Planet planet) {
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(warShips, "warShips must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        warShips.forEach(warShip -> warShip.setMothball(planet));
        warShipService.saveAll(warShips);
    }

    @Deprecated
    public void deleteAll() {
        final List<Fleet> all = Objects.requireNonNullElse(fleetRepository.findAllAliveFleets(), new ArrayList<>());
        LOGGER.warn("You deleted all fleets '{}'!", all.stream().map(Fleet::getId).map(String::valueOf).collect(Collectors.joining(", ")));
        markAsDestroyed(all);
    }
}
