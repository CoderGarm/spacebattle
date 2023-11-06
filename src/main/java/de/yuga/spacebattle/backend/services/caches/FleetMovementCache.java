package de.yuga.spacebattle.backend.services.caches;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import de.yuga.spacebattle.backend.dto.turn.FleetMovement;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.caches.file.CacheFileWriter;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FleetMovementCache extends BaseCache {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(FleetMovementCache.class);

    @Nonnull
    private final CacheStore<Integer, Set<FleetMovement>> cache = new CacheStore<>(2, TimeUnit.DAYS); /* fixme remove me */

    @Nonnull
    private final CacheFileWriter cacheFileWriter;

    @Nonnull
    private final TickTimeService tickTimeService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final StarSystemService systemService;

    @Nonnull
    private final FleetService fleetService;

    @Autowired
    public FleetMovementCache(@Nonnull final CacheFileWriter cacheFileWriter,
                              @Nonnull final TickTimeService tickTimeService,
                              @Nonnull final PlanetService planetService,
                              @Nonnull final StarSystemService systemService,
                              @Nonnull final FleetService fleetService) {
        this.cacheFileWriter = Preconditions.checkNotNull(cacheFileWriter, "cacheFileWriter must not be empty");
        this.tickTimeService = Preconditions.checkNotNull(tickTimeService, "tickTimeService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.systemService = Preconditions.checkNotNull(systemService, "systemService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
    }


    @PostConstruct
    private void loadCache() {
        LOGGER.info("Loading from persistent cache.");

        final Map<String, List<String>> fileCacheContent = cacheFileWriter.getFileCacheContent(this.getClass());
        final Map<String, List<FleetMovementDtoCollector>> dtos = new HashMap<>();
        fileCacheContent.forEach((key, values) -> dtos.put(key, values.stream().map(this::fromJson).collect(Collectors.toList())));

        final Set<Integer> tickIDs = fileCacheContent.keySet().stream().map(this::getTickId).collect(Collectors.toSet());
        final Map<Integer, Tick> tickMap = tickTimeService.findAll(tickIDs).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        final Set<Integer> planetIds = dtos.values().stream().flatMap(Collection::stream)
                .map(c -> c.getMovements().stream().map(FleetMovementDto::getIdPlanet).filter(Objects::nonNull).collect(Collectors.toList()))
                .flatMap(Collection::stream).collect(Collectors.toSet());
        final Map<Integer, Planet> planetMap = planetService.findAll(planetIds).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        final Set<Integer> systemIds = dtos.values().stream().flatMap(Collection::stream)
                .map(c -> c.getMovements().stream().map(FleetMovementDto::getIdStarSystem).collect(Collectors.toList()))
                .flatMap(Collection::stream).collect(Collectors.toSet());
        final Map<Integer, StarSystem> systemMap = systemService.findAll(systemIds).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        final Set<Integer> fleetIds = dtos.values().stream().flatMap(Collection::stream)
                .map(c -> c.getMovements().stream().map(FleetMovementDto::getIdFleet).collect(Collectors.toList()))
                .flatMap(Collection::stream).collect(Collectors.toSet());
        final Map<Integer, Fleet> fleetMap = fleetService.findAll(fleetIds).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        dtos.forEach((key, coloDtos) -> {
            final int userId = getUserId(key);
            final Tick tick = tickMap.get(getTickId(key));
            coloDtos.forEach(dto -> dto.getMovements().forEach(m -> {
                final Fleet fleet = fleetMap.get(m.getIdFleet());
                final Planet planet = planetMap.get(m.getIdPlanet());
                final StarSystem starSystem = systemMap.get(m.getIdStarSystem());
                final int originalDuration = m.getOriginalDuration();
                final boolean foreignFleet = m.isForeignFleet();
                Set<FleetMovement> movements = cache.get(userId);
                if (movements == null) {
                    movements = new HashSet<>();
                    cache.put(userId, movements);
                }
                movements.add(new FleetMovement(tick, fleet, planet, starSystem, originalDuration, foreignFleet));
            }));
        });
    }

    @Nonnull
    public Set<FleetMovement> getMovements(@Nonnull final Tick today,
                                           final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return Objects.requireNonNullElse(cache.get(idUser), new HashSet<FleetMovement>()).stream().filter(t -> t.isToday(today)).collect(Collectors.toSet());
    }

    public void add(@Nonnull final Tick today,
                    @Nonnull final Fleet fleet,
                    @Nonnull final Move move,
                    @Nonnull final Planet destination) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(move, "move must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        final Owner notificationTarget;
        if (destination.getHumanOwner() == null || fleet.getOwner().equals(destination.getOwner())) {
            notificationTarget = fleet.getOwner();
        } else {
            notificationTarget = destination.getHumanOwner();
            notify(today, fleet.getOwner(), new FleetMovement(today, fleet, destination, move));
        }

        notify(today, notificationTarget, new FleetMovement(today, fleet, destination, move));
    }

    public void add(@Nonnull final Tick today,
                    @Nonnull final Fleet fleet,
                    @Nonnull final Move move,
                    @Nonnull final StarSystem destinationSystem) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(move, "move must not be empty");
        Preconditions.checkNotNull(destinationSystem, "destinationSystem must not be empty");

        final Set<Owner> owners = destinationSystem.getPlanets().stream()
                .map(Planet::getHumanOwner)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        owners.forEach(notificationTarget -> {
            notify(today, notificationTarget, new FleetMovement(today, fleet, destinationSystem, move, !fleet.getOwner().equals(notificationTarget)));
        });

        if (!owners.contains(fleet.getOwner())) {
            notify(today, fleet.getOwner(), new FleetMovement(today, fleet, destinationSystem, move, false));
        }
    }

    private void notify(@Nonnull final Tick today, @Nonnull final Owner notificationTarget, @Nonnull final FleetMovement fleetMovement) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(notificationTarget, "notificationTarget must not be empty");
        Preconditions.checkNotNull(fleetMovement, "fleetMovement must not be empty");

        if (notificationTarget instanceof User) {
            getTodayMovement(today, notificationTarget).add(fleetMovement);
            dropAndWrite(today, (User) notificationTarget);
        }
    }

    @Nonnull
    private Set<FleetMovement> getTodayMovement(@Nonnull final Tick today,
                                                @Nonnull final Owner user) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");

        Set<FleetMovement> movements = cache.get(user.getId());
        if (movements == null) {
            movements = new HashSet<>();
            cache.put(user.getId(), movements);
        }
        return movements;
    }

    private void dropAndWrite(@Nonnull final Tick today,
                              @Nonnull final User user) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");

        final String cacheKey = getCacheKey(today, user);
        final Set<FleetMovement> cacheObjects = getTodayMovement(today, user);

        cacheFileWriter.dropKeyFromFileCache(this.getClass(), cacheKey);
        cacheFileWriter.writeToFile(this.getClass(), cacheKey, toJson(today, user, cacheObjects));
    }

    @Nonnull
    private String toJson(@Nonnull final Tick today,
                          @Nonnull final User user,
                          @Nonnull final Set<FleetMovement> cacheObjects) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");
        Preconditions.checkNotNull(cacheObjects, "cacheObjects must not be empty");

        return new GsonBuilder().create().toJson(new FleetMovementDtoCollector(cacheObjects));
    }

    @Nonnull
    private FleetMovementDtoCollector fromJson(@Nonnull final String string) {
        Preconditions.checkNotNull(string, "string must not be empty");

        return new GsonBuilder().create().fromJson(string, FleetMovementDtoCollector.class);
    }


    @Schema
    private static class FleetMovementDtoCollector {

        @Nonnull
        @JsonProperty
        private final List<FleetMovementDto> movements;

        private FleetMovementDtoCollector(@Nonnull final Set<FleetMovement> movements) {
            this.movements = movements.stream().map(FleetMovementDto::new).collect(Collectors.toList());
        }

        @Nonnull
        public List<FleetMovementDto> getMovements() {
            return movements;
        }
    }

    @Schema
    private static class FleetMovementDto {

        private final int idFleet;

        private final int idStarSystem;

        @Nullable
        private final Integer idPlanet;

        private final int originalDuration;

        private final boolean isForeignFleet;


        public FleetMovementDto(@Nonnull final FleetMovement m) {
            Preconditions.checkNotNull(m, "m must not be empty");

            this.idFleet = m.getFleet().getId();
            this.idStarSystem = m.getDestinationSystem().getId();
            this.idPlanet = m.getDestinationPlanet() != null ? m.getDestinationPlanet().getId() : null;
            this.originalDuration = m.getOriginalDuration();
            this.isForeignFleet = m.isForeignFleet();
        }

        public int getIdFleet() {
            return idFleet;
        }

        public int getIdStarSystem() {
            return idStarSystem;
        }

        @Nullable
        public Integer getIdPlanet() {
            return idPlanet;
        }

        public int getOriginalDuration() {
            return originalDuration;
        }

        public boolean isForeignFleet() {
            return isForeignFleet;
        }
    }
}
