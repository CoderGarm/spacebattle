package de.yuga.spacebattle.backend.services.caches;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import de.yuga.spacebattle.backend.dto.turn.FinishedColonization;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.caches.file.CacheFileWriter;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ColonizationCache extends BaseCache {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationalCache.class);

    @Nonnull
    private final CacheStore<Integer, Set<FinishedColonization>> cache = new CacheStore<>(2, TimeUnit.DAYS);

    @Nonnull
    private final CacheFileWriter cacheFileWriter;

    @Nonnull
    private final TickTimeService tickTimeService;

    @Nonnull
    private final PlanetService planetService;

    @Autowired
    public ColonizationCache(@Nonnull final CacheFileWriter cacheFileWriter,
                             @Nonnull final TickTimeService tickTimeService,
                             @Nonnull final PlanetService planetService) {
        this.cacheFileWriter = Preconditions.checkNotNull(cacheFileWriter, "cacheFileWriter must not be empty");
        this.tickTimeService = Preconditions.checkNotNull(tickTimeService, "tickTimeService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
    }

    @PostConstruct
    private void loadCache() {
        LOGGER.info("Loading from persistent cache.");

        final Map<String, List<String>> fileCacheContent = cacheFileWriter.getFileCacheContent(this.getClass());
        final Map<String, List<ColoDto>> dtos = new HashMap<>();
        fileCacheContent.forEach((key, values) -> dtos.put(key, values.stream().map(this::fromJson).collect(Collectors.toList())));

        final Set<Integer> tickIDs = fileCacheContent.keySet().stream().map(this::getTickId).collect(Collectors.toSet());
        final Map<Integer, Tick> tickMap = tickTimeService.findAll(tickIDs).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        final List<Integer> planetIds = dtos.values().stream().flatMap(Collection::stream).map(ColoDto::getPlanets).flatMap(Collection::stream).collect(Collectors.toList());
        final Map<Integer, Planet> planets = planetService.findAll(planetIds).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        dtos.forEach((key, coloDtos) -> {
            final Tick tick = tickMap.get(getTickId(key));
            coloDtos.forEach(dto -> dto.getPlanets().forEach(p -> addColonizedPlanet(tick, planets.get(p))));
        });
    }

    @Nonnull
    public Set<FinishedColonization> getColonizations(@Nonnull final Tick today,
                                                      final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return Objects.requireNonNullElse(cache.get(idUser), new HashSet<FinishedColonization>()).stream().filter(t -> t.isToday(today)).collect(Collectors.toSet());
    }

    public void add(@Nonnull final Tick today,
                    @Nonnull final Planet planet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(planet.getHumanOwner(), "planet.getHumanOwner() must not be empty");

        addColonizedPlanet(today, planet);
        dropAndWrite(today, planet.getHumanOwner());
    }

    private void addColonizedPlanet(@Nonnull final Tick today, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(planet.getHumanOwner(), "planet.getHumanOwner() must not be empty");

        final Set<FinishedColonization> job = getTodays(today, planet.getHumanOwner());
        job.add(new FinishedColonization(today, planet));
    }

    @Nonnull
    private Set<FinishedColonization> getTodays(@Nonnull final Tick today,
                                                @Nonnull final User user) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");

        Set<FinishedColonization> movements = cache.get(user.getId());
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
        final Set<FinishedColonization> todays = getTodays(today, user);

        cacheFileWriter.dropKeyFromFileCache(this.getClass(), cacheKey);
        cacheFileWriter.writeToFile(this.getClass(), cacheKey, toJson(today, user, todays.stream().map(FinishedColonization::getPlanet).collect(Collectors.toSet())));
    }

    @Nonnull
    private String toJson(@Nonnull final Tick today,
                          @Nonnull final User user,
                          @Nonnull final Set<Planet> planets) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");
        Preconditions.checkNotNull(planets, "planets must not be empty");

        return new GsonBuilder().create().toJson(new ColoDto(planets));
    }

    @Nonnull
    private ColoDto fromJson(@Nonnull final String string) {
        Preconditions.checkNotNull(string, "string must not be empty");

        return new GsonBuilder().create().fromJson(string, ColoDto.class);
    }

    @Schema
    public static class ColoDto {

        @Nonnull
        @JsonProperty
        private final List<Integer> planets;

        public ColoDto(@Nonnull final Set<Planet> planets) {
            Preconditions.checkNotNull(planets, "planets must not be empty");

            this.planets = planets.stream().map(AbstractEntityKey::getId).collect(Collectors.toList());
        }

        @Nonnull
        public List<Integer> getPlanets() {
            return planets;
        }
    }
}
