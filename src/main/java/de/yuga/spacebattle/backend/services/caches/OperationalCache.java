package de.yuga.spacebattle.backend.services.caches;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import de.yuga.spacebattle.backend.dto.turn.Commissioning;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.caches.file.CacheFileWriter;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
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
public class OperationalCache extends BaseCache {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationalCache.class);

    @Nonnull
    private final CacheStore<String, Set<Commissioning>> cache = new CacheStore<>(2, TimeUnit.DAYS);

    @Nonnull
    private final CacheFileWriter cacheFileWriter;

    @Nonnull
    private final TickTimeService tickTimeService;

    @Nonnull
    private final ConstructionService constructionService;

    @Nonnull
    private final WarShipService warShipService;

    @Autowired
    public OperationalCache(@Nonnull final CacheFileWriter cacheFileWriter,
                            @Nonnull final TickTimeService tickTimeService,
                            @Nonnull final ConstructionService constructionService,
                            @Nonnull final WarShipService warShipService) {
        this.cacheFileWriter = Preconditions.checkNotNull(cacheFileWriter, "cacheFileWriter must not be empty");
        this.tickTimeService = Preconditions.checkNotNull(tickTimeService, "tickTimeService must not be empty");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService must not be empty");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
    }

    @PostConstruct
    private void loadCache() {
        LOGGER.info("Loading from persistent cache.");

        final Map<String, List<String>> fileCacheContent = cacheFileWriter.getFileCacheContent(this.getClass());
        final Map<String, List<CommissioningDto>> dtos = new HashMap<>();
        fileCacheContent.forEach((key, values) -> dtos.put(key, values.stream().map(this::fromJson).collect(Collectors.toList())));

        final Set<Integer> tickIDs = fileCacheContent.keySet().stream().map(this::getTickId).collect(Collectors.toSet());
        final Map<Integer, Tick> tickMap = tickTimeService.findAll(tickIDs).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        final Set<Integer> constructionIDs = dtos.values().stream().flatMap(Collection::stream).map(CommissioningDto::getConstructions).flatMap(Collection::stream).collect(Collectors.toSet());
        final Map<Integer, Construction> constructionMap = constructionService.findAll(constructionIDs).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        final Set<Integer> warshipIDs = dtos.values().stream().flatMap(Collection::stream).map(CommissioningDto::getWarships).flatMap(Collection::stream).collect(Collectors.toSet());
        final Map<Integer, WarShip> warShipMap = warShipService.findByIds(warshipIDs).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        dtos.forEach((key, values) -> values.forEach(dto -> {
            final Tick tick = tickMap.get(getTickId(key));
            final Set<Construction> constructions = dto.getConstructions().stream().map(constructionMap::get).collect(Collectors.toSet());
            final List<WarShip> warships = dto.getWarships().stream().map(warShipMap::get).collect(Collectors.toList());

            constructions.forEach(c -> addConstruction(tick, c.getPlanet(), c));
            warships.forEach(c -> addWarship(tick, c.getShipyard(), c));
        }));
    }

    @Nonnull
    public Set<Commissioning> getOperationals(@Nonnull final Tick today,
                                              final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        final String cacheKey = getCacheKey(today, idUser);
        return Objects.requireNonNullElse(cache.get(cacheKey), new HashSet<Commissioning>()).stream().filter(t -> t.isToday(today)).collect(Collectors.toSet());
    }


    private void dropAndWrite(@Nonnull final Tick today,
                              @Nonnull final User user) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");

        final String cacheKey = getCacheKey(today, user);
        final Set<Commissioning> commissionings = getTodayCommissioning(today, user);

        cacheFileWriter.dropKeyFromFileCache(this.getClass(), cacheKey);
        cacheFileWriter.writeToFile(this.getClass(), cacheKey, toJson(today, user, commissionings));
    }


    @Nonnull
    private String toJson(@Nonnull final Tick today,
                          @Nonnull final User user,
                          @Nonnull final Set<Commissioning> commissionings) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");
        Preconditions.checkNotNull(commissionings, "commissionings must not be empty");

        return new GsonBuilder().create().toJson(new CommissioningDto(commissionings));
    }

    @Nonnull
    private CommissioningDto fromJson(@Nonnull final String string) {
        Preconditions.checkNotNull(string, "string must not be empty");

        return new GsonBuilder().create().fromJson(string, CommissioningDto.class);
    }

    @Nonnull
    private Set<Commissioning> getTodayCommissioning(@Nonnull final Tick today,
                                                     final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        final String cacheKey = getCacheKey(today, idUser);
        Set<Commissioning> commissionings = cache.get(cacheKey);
        if (commissionings == null) {
            commissionings = new HashSet<>();
            cache.put(cacheKey, commissionings);
        }
        return commissionings;
    }

    @Nonnull
    private Set<Commissioning> getTodayCommissioning(@Nonnull final Tick today,
                                                     @Nonnull final User user) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");

        return getTodayCommissioning(today, user.getId());
    }

    public void activateWarships(@Nonnull final Tick today,
                                 @Nonnull final Planet planet,
                                 @Nonnull final List<WarShip> operationals) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(planet.getHumanOwner(), "planet.getOwner() must not be empty");
        Preconditions.checkNotNull(operationals, "operationals must not be empty");

        addWarships(today, planet, operationals);
        dropAndWrite(today, planet.getHumanOwner());
    }

    public void activateConstructions(@Nonnull final Tick today,
                                      @Nonnull final Planet planet,
                                      @Nonnull final Set<Construction> operationals) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(planet.getHumanOwner(), "planet.getOwner() must not be empty");
        Preconditions.checkNotNull(operationals, "operationals must not be empty");

        addConstructions(today, planet, operationals);
        dropAndWrite(today, planet.getHumanOwner());
    }

    private void addConstructions(@Nonnull final Tick today, @Nonnull final Planet planet, @Nonnull final Set<Construction> operationals) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(operationals, "operationals must not be empty");

        final Set<Commissioning> commissionings = getTodayCommissioning(today, Objects.requireNonNull(planet.getHumanOwner()));
        commissionings.stream()
                .filter(c -> c.getPlanet().equals(planet))
                .findFirst()
                .ifPresentOrElse(commissioning -> commissioning.addConstructions(operationals),
                        () -> commissionings.add(new Commissioning(today, planet, operationals)));
    }

    private void addWarships(@Nonnull final Tick today, @Nonnull final Planet planet, @Nonnull final List<WarShip> operationals) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(operationals, "operationals must not be empty");

        final Set<Commissioning> commissionings = getTodayCommissioning(today, Objects.requireNonNull(planet.getHumanOwner()));
        commissionings.stream()
                .filter(c -> c.getPlanet().equals(planet))
                .findFirst()
                .ifPresentOrElse(commissioning -> commissioning.setWarships(operationals),
                        () -> commissionings.add(new Commissioning(today, planet, operationals)));
    }

    private void addConstruction(@Nonnull final Tick today, @Nonnull final Planet planet, @Nonnull final Construction operational) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(operational, "operational must not be empty");

        final Set<Commissioning> commissionings = getTodayCommissioning(today, Objects.requireNonNull(planet.getHumanOwner()));
        final Set<Construction> operationals = new HashSet<>();
        operationals.add(operational);
        commissionings.stream()
                .filter(c -> c.getPlanet().equals(planet))
                .findFirst()
                .ifPresentOrElse(commissioning -> commissioning.addConstructions(operationals),
                        () -> commissionings.add(new Commissioning(today, planet, operationals)));
    }

    private void addWarship(@Nonnull final Tick today, @Nonnull final Planet planet, @Nonnull final WarShip operational) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(operational, "operational must not be empty");

        final Set<Commissioning> commissionings = getTodayCommissioning(today, Objects.requireNonNull(planet.getHumanOwner()));
        final List<WarShip> operationals = new ArrayList<>();
        operationals.add(operational);
        commissionings.stream()
                .filter(c -> c.getPlanet().equals(planet))
                .findFirst()
                .ifPresentOrElse(commissioning -> commissioning.addWarships(operationals),
                        () -> commissionings.add(new Commissioning(today, planet, operationals)));
    }

    @Schema
    public static class CommissioningDto {

        @Nonnull
        @JsonProperty
        private final List<Integer> constructions;

        @Nonnull
        @JsonProperty
        private final List<Integer> warships;

        public CommissioningDto(@Nonnull final Set<Commissioning> commissionings) {
            this.constructions = commissionings.stream().map(Commissioning::getConstructions).flatMap(Collection::stream).map(AbstractEntityKey::getId).collect(Collectors.toList());
            this.warships = commissionings.stream().map(Commissioning::getWarships).flatMap(Collection::stream).map(AbstractEntityKey::getId).collect(Collectors.toList());
        }

        @Nonnull
        public List<Integer> getConstructions() {
            return constructions;
        }

        @Nonnull
        public List<Integer> getWarships() {
            return warships;
        }
    }
}
