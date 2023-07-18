package de.yuga.spacebattle.backend.services.caches;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.services.caches.file.CacheFileWriter;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
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
public class RaidingPirateCache {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(RaidingPirateCache.class);

    @Nonnull
    private final CacheStore<Fleet, List<EMissionAction>> doNotMoveCache = new CacheStore<>(5, TimeUnit.DAYS);

    @Nonnull
    private final CacheStore<Fleet, Planet> targetCache = new CacheStore<>(10, TimeUnit.DAYS);

    @Nonnull
    private final CacheFileWriter cacheFileWriter;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final PlanetService planetService;

    @Autowired
    public RaidingPirateCache(@Nonnull final CacheFileWriter cacheFileWriter,
                              @Nonnull final FleetService fleetService,
                              @Nonnull final PlanetService planetService) {
        this.cacheFileWriter = Preconditions.checkNotNull(cacheFileWriter, "cacheFileWriter must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
    }

    @PostConstruct
    private void loadCache() {
        LOGGER.info("Loading from persistent cache.");

        final Map<String, List<String>> targetCacheContent = cacheFileWriter.getFileCacheContent(TargetDto.class);
        final List<TargetDto> targetDtos = targetCacheContent.values().stream()
                .flatMap(Collection::stream)
                .map(this::fromJsonTargetDto)
                .collect(Collectors.toList());


        final Map<String, List<String>> missionActionContent = cacheFileWriter.getFileCacheContent(RaidingPirateCache.MissionActionDto.class);
        final List<MissionActionDto> actionDtos = missionActionContent.values().stream()
                .flatMap(Collection::stream)
                .map(this::fromJson)
                .collect(Collectors.toList());

        LOGGER.info("\t...loading fleets");
        final Set<Integer> idFleets = actionDtos.stream().map(MissionActionDto::getIdFleet).collect(Collectors.toSet());
        idFleets.addAll(targetDtos.stream().map(TargetDto::getIdFleet).collect(Collectors.toSet()));
        final Map<Integer, Fleet> fleets = fleetService.findAll(idFleets).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        LOGGER.info("\t...loading planets");
        final Set<Integer> idPlanets = targetDtos.stream().map(TargetDto::getIdPlanet).collect(Collectors.toSet());
        final Map<Integer, Planet> planets = planetService.findAll(idPlanets).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));


        actionDtos.forEach(dto -> doNotMoveCache.put(fleets.get(dto.getIdFleet()), dto.getActions()));
        targetDtos.forEach(targetDto -> targetCache.put(fleets.get(targetDto.idFleet), planets.get(targetDto.getIdPlanet())));
    }

    public void executeNext(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final EMissionAction... missionAction) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(missionAction, "missionAction must not be empty");

        put(today, pirateFleet, Arrays.asList(missionAction));
    }

    private void put(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final List<EMissionAction> missionActions) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(missionActions, "missionActions must not be empty");

        doNotMoveCache.put(pirateFleet, missionActions);
        final String key = getKey(today, pirateFleet);
        cacheFileWriter.writeToFile(RaidingPirateCache.MissionActionDto.class, key, toJson(pirateFleet, missionActions));
    }

    public void dropFirstActionItem(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final EMissionAction missionAction) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(missionAction, "missionAction must not be empty");

        List<EMissionAction> eMissionActions = doNotMoveCache.get(pirateFleet);
        if (eMissionActions != null && !eMissionActions.isEmpty() && missionAction == eMissionActions.get(0)) {
            eMissionActions = new ArrayList<>(eMissionActions);
            eMissionActions.remove(0);
            put(today, pirateFleet, eMissionActions);
        }
    }

    @Nonnull
    public List<EMissionAction> getNextActions(@Nonnull final Fleet pirateFleet) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");

        final List<EMissionAction> eMissionActions = doNotMoveCache.get(pirateFleet);
        return eMissionActions != null ? eMissionActions : List.of();
    }

    public boolean isPhaseSequenceValid(@Nonnull final Fleet pirateFleet,
                                        @Nonnull final EMissionAction... actions) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(actions, "actions must not be empty");

        final List<EMissionAction> nextActions = getNextActions(pirateFleet);
        for (int i = 0; i < actions.length; i++) {
            if (nextActions.size() - 1 < i) {
                return false;
            }
            if (nextActions.get(i) != actions[i]) {
                return false;
            }
        }
        return true;
    }

    public void setTarget(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        targetCache.put(pirateFleet, target);
        cacheFileWriter.writeToFile(RaidingPirateCache.TargetDto.class, String.valueOf(pirateFleet.getId()), toJsonTargetDto(pirateFleet, target));
    }

    @Nullable
    public Planet getTarget(@Nonnull final Fleet pirateFleet) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");

        return targetCache.get(pirateFleet);
    }

    @Nonnull
    private String toJsonTargetDto(@Nonnull final Fleet pirateFleet,
                                   @Nonnull final Planet planet) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        return new GsonBuilder().create().toJson(new TargetDto(pirateFleet, planet));
    }

    @Nonnull
    private TargetDto fromJsonTargetDto(@Nonnull final String string) {
        Preconditions.checkNotNull(string, "string must not be empty");

        return new GsonBuilder().create().fromJson(string, TargetDto.class);
    }


    @Nonnull
    private String toJson(@Nonnull final Fleet pirateFleet,
                          @Nonnull final List<EMissionAction> actions) {
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(actions, "actions must not be empty");

        return new GsonBuilder().create().toJson(new MissionActionDto(pirateFleet, actions));
    }

    @Nonnull
    private MissionActionDto fromJson(@Nonnull final String string) {
        Preconditions.checkNotNull(string, "string must not be empty");

        return new GsonBuilder().create().fromJson(string, MissionActionDto.class);
    }

    @Nonnull
    private String getKey(@Nonnull final Tick today, final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return today.getNo() + "|" + idUser;
    }

    @Nonnull
    private String getKey(@Nonnull final Tick today, @Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(fleet.getOwner(), "fleet.getOwner() must not be empty");

        return getKey(today, fleet.getOwner().getId());
    }

    private int getTickId(@Nonnull final String key) {
        return Integer.parseInt(key.split("\\|")[0]);
    }

    @Schema
    private static class MissionActionDto {

        @JsonProperty
        private int idFleet;

        @JsonProperty
        private List<EMissionAction> actions;

        public MissionActionDto() {
        }

        public MissionActionDto(@Nonnull final Fleet pirateFleet, @Nonnull final List<EMissionAction> actions) {
            Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
            this.actions = Preconditions.checkNotNull(actions, "actions must not be empty");

            this.idFleet = pirateFleet.getId();
        }

        public int getIdFleet() {
            return idFleet;
        }

        public List<EMissionAction> getActions() {
            return actions;
        }
    }

    @Schema
    private static class TargetDto {

        @JsonProperty
        private int idFleet;

        @JsonProperty
        private int idPlanet;


        public TargetDto() {
        }

        public TargetDto(@Nonnull final Fleet pirateFleet, @Nonnull final Planet targetPlanet) {
            Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
            Preconditions.checkNotNull(targetPlanet, "targetPlanet must not be empty");

            this.idFleet = pirateFleet.getId();
            this.idPlanet = targetPlanet.getId();
        }

        public int getIdFleet() {
            return idFleet;
        }

        public int getIdPlanet() {
            return idPlanet;
        }
    }
}
