package de.yuga.spacebattle.backend.services.caches;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import de.yuga.spacebattle.backend.dto.turn.mission.MissionItem;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.caches.file.CacheFileWriter;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class MissionCache {

    @Nonnull
    private final CacheStore<String, List<MissionItem>> cache = new CacheStore<>(10, TimeUnit.DAYS);

    @Nonnull
    private final CacheFileWriter cacheFileWriter;

    @Nonnull
    private final TickTimeService tickTimeService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final BattleReportService battleReportService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final FleetService fleetService;

    @Autowired
    public MissionCache(@Nonnull final CacheFileWriter cacheFileWriter,
                        @Nonnull final TickTimeService tickTimeService,
                        @Nonnull final UserService userService,
                        @Nonnull final BattleReportService battleReportService,
                        @Nonnull final PlanetService planetService,
                        @Nonnull final FleetService fleetService) {
        this.cacheFileWriter = Preconditions.checkNotNull(cacheFileWriter, "cacheFileWriter must not be empty");
        this.tickTimeService = Preconditions.checkNotNull(tickTimeService, "tickTimeService must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
        this.battleReportService = Preconditions.checkNotNull(battleReportService, "battleReportService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
    }

    @PostConstruct
    private void loadCache() {

        final Map<String, List<String>> fileCacheContent = cacheFileWriter.getFileCacheContent(this.getClass());

        final List<MissionItemDto> allItems = fileCacheContent.values().stream()
                .flatMap(Collection::stream)
                .map(this::fromJson)
                .collect(Collectors.toList());

        final Set<Integer> idBattleReports = allItems.stream()
                .map(MissionItemDto::getIdBattleReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        final Map<Integer, BattleReport> battleReports = battleReportService.findAll(idBattleReports).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        final Set<Integer> idPlanets = allItems.stream().map(MissionItemDto::getIdPlanetTarget).collect(Collectors.toSet());
        final Map<Integer, Planet> planets = planetService.findAll(idPlanets).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        final Set<Integer> idFleets = allItems.stream().map(MissionItemDto::getIdPirateFleet).collect(Collectors.toSet());
        final Map<Integer, Fleet> fleets = fleetService.findAll(idFleets).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        final Set<Integer> idTicks = fileCacheContent.keySet().stream().map(this::getTickId).collect(Collectors.toSet());
        final Map<Integer, Tick> ticks = tickTimeService.findAll(idTicks).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        final Map<String, List<MissionItemDto>> result = fileCacheContent.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(this::fromJson).collect(Collectors.toList())));

        result.forEach((key, serializedItems) -> {
            final List<MissionItem> lst = serializedItems.stream()
                    .map(s -> {
                                final MissionItem item = new MissionItem(
                                        ticks.get(s.getTickNo()),
                                        fleets.get(s.getIdPirateFleet()),
                                        planets.get(s.getIdPlanetTarget()),
                                        s.geteMissionType(),
                                        s.getMissionAction());
                                item.setUserDefeated(s.isUserDefeated());
                                if (s.getIdBattleReport() != null) {
                                    item.setBattleReport(battleReports.get(s.getIdBattleReport()));
                                }
                                return item;
                            }
                    ).collect(Collectors.toList());
            cache.put(key, lst);
        });

    }

    public void pirateRaidSpawn(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionAction.SPAWN);
        addItem(today, target, missionItem);
    }

    public void pirateRaidApproach(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionAction.APPROACH);
        addItem(today, target, missionItem);
    }

    public void pirateRaidWithdraw(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionAction.WITHDRAW);
        addItem(today, target, missionItem);
    }

    public void pirateRaidWithdrawFromOrbit(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionAction.LEAVE_ORBIT);
        addItem(today, target, missionItem);
    }

    public void pirateRaidBattleResult(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target, @Nullable final BattleReport battleReport, final boolean userDefeated) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        if (battleReport == null) {
            final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionAction.NO_BATTLE);
            addItem(today, target, missionItem);
            return;
        }

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionAction.BATTLE);
        missionItem.setUserDefeated(userDefeated);
        missionItem.setBattleReport(battleReport);
        addItem(today, target, missionItem);
    }

    public void pirateRaidTargetRaided(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionAction.RAID);
        addItem(today, target, missionItem);
    }


    public void pirateRaidWait(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final MissionItem missionItem = new MissionItem(today, pirateFleet, target, EMissionType.PIRATE_RAID, EMissionAction.WAIT);
        addItem(today, target, missionItem);
    }

    @Nonnull
    public List<MissionItem> get(@Nonnull final Tick today, final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return Objects.requireNonNullElse(cache.get(getKey(today, idUser)), new ArrayList<>());
    }

    private void addItem(@Nonnull final Tick today, @Nonnull final Planet target, @Nonnull final MissionItem missionItem) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");
        Preconditions.checkNotNull(missionItem, "missionItem must not be empty");

        final String key = getKey(today, target);
        List<MissionItem> missionItems = cache.get(key);
        if (missionItems == null) {
            missionItems = new ArrayList<>();
        }
        missionItems.add(missionItem);
        cache.put(key, missionItems);
        write(key, missionItem);
    }

    private void write(@Nonnull final String key, @Nonnull final MissionItem missionItem) {
        Preconditions.checkNotNull(key, "key must not be empty");
        Preconditions.checkNotNull(missionItem, "missionItem must not be empty");

        final String value = toJson(missionItem);
        cacheFileWriter.writeToFile(this.getClass(), key, value);
    }

    @Nonnull
    private String toJson(@Nonnull final MissionItem missionItem) {
        Preconditions.checkNotNull(missionItem, "missionItem must not be empty");

        return new GsonBuilder().create().toJson(new MissionItemDto(missionItem));
    }

    @Nonnull
    private MissionItemDto fromJson(@Nonnull final String missionItem) {
        Preconditions.checkNotNull(missionItem, "missionItem must not be empty");

        return new GsonBuilder().create().fromJson(missionItem, MissionItemDto.class);
    }

    @Nonnull
    private String getKey(@Nonnull final Tick today, final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return today.getNo() + "|" + idUser;
    }

    @Nonnull
    private String getKey(@Nonnull final Tick today, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");
        Preconditions.checkNotNull(target.getOwner(), "target.getOwner() must not be empty");

        return getKey(today, target.getOwner().getId());
    }

    private int getTickId(@Nonnull final String key) {
        return Integer.parseInt(key.split("\\|")[0]);
    }

    private int getUserId(@Nonnull final String key) {
        return Integer.parseInt(key.split("\\|")[1]);
    }

    @Schema
    private static class MissionItemDto {

        @JsonProperty
        private int tickNo;

        @JsonProperty
        private int idPirateFleet;

        @JsonProperty
        private int idPlanetTarget;

        @Nonnull
        @JsonProperty
        private EMissionType eMissionType;

        @Nonnull
        @JsonProperty
        private EMissionAction missionAction;

        @JsonProperty
        private boolean userDefeated;

        @Nullable
        @JsonProperty
        private Integer idBattleReport;

        public MissionItemDto() {
        }

        public MissionItemDto(@Nonnull final MissionItem item) {
            Preconditions.checkNotNull(item, "item must not be empty");

            this.tickNo = item.getToday().getNo();
            this.idPirateFleet = item.getPirateFleet().getId();
            this.idPlanetTarget = item.getTarget().getId();
            this.eMissionType = item.geteMissionType();
            this.missionAction = item.getEMissionAction();
            this.userDefeated = item.isUserDefeated();
            this.idBattleReport = item.getBattleReport() != null ? item.getBattleReport().getId() : null;
        }

        public int getTickNo() {
            return tickNo;
        }

        public int getIdPirateFleet() {
            return idPirateFleet;
        }

        public int getIdPlanetTarget() {
            return idPlanetTarget;
        }

        @Nonnull
        public EMissionType geteMissionType() {
            return eMissionType;
        }

        @Nonnull
        public EMissionAction getMissionAction() {
            return missionAction;
        }

        public boolean isUserDefeated() {
            return userDefeated;
        }

        @Nullable
        public Integer getIdBattleReport() {
            return idBattleReport;
        }
    }
}
