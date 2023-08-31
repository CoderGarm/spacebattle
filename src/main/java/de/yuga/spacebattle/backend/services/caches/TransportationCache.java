package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.yuga.spacebattle.backend.dto.turn.OrbitalTransportJob;
import de.yuga.spacebattle.backend.dto.turn.TransportJob;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.ETransportType;
import de.yuga.spacebattle.backend.services.caches.file.CacheFileWriter;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import de.yuga.spacebattle.rest.dto.turn.resources.HumanResourceAmount;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
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
public class TransportationCache {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportationCache.class);

    @Nonnull
    private final CacheStore<Integer, Set<TransportJob>> transportCache = new CacheStore<>(2, TimeUnit.DAYS);

    @Nonnull
    private final CacheStore<Integer, Set<OrbitalTransportJob>> orbitalTransportCache = new CacheStore<>(2, TimeUnit.DAYS);

    @Nonnull
    private final TickTimeService tickTimeService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final CacheFileWriter cacheFileWriter;
    private final FleetService fleetService;

    @Autowired
    public TransportationCache(@Nonnull final TickTimeService tickTimeService,
                               @Nonnull final PlanetService planetService,
                               @Nonnull final CacheFileWriter cacheFileWriter, final FleetService fleetService) {
        this.tickTimeService = Preconditions.checkNotNull(tickTimeService, "tickTimeService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.cacheFileWriter = Preconditions.checkNotNull(cacheFileWriter, "cacheFileWriter must not be empty");
        this.fleetService = fleetService;
    }

    @PostConstruct
    private void loadCache() {
        LOGGER.info("Loading from persistent cache.");

        final Map<String, List<String>> transportJobCache = cacheFileWriter.getFileCacheContent(TransportJob.class);
        final Map<String, List<String>> orbitalTransportJobCache = cacheFileWriter.getFileCacheContent(OrbitalTransportJob.class);

        LOGGER.info("\t...loading ticks");
        final Set<Integer> idTicks = transportJobCache.keySet().stream().map(this::getTickId).collect(Collectors.toSet());
        idTicks.addAll(orbitalTransportJobCache.keySet().stream().map(this::getTickId).collect(Collectors.toSet()));
        final Map<Integer, Tick> tickMap = tickTimeService.findAll(idTicks).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        LOGGER.info("\t...loading planets");
        final Set<Integer> idPlanets = transportJobCache.keySet()
                .stream()
                .map(this::getPlanetIDsFromKey)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        idPlanets.addAll(orbitalTransportJobCache.keySet()
                .stream()
                .map(this::getIdPlanetFrom)
                .collect(Collectors.toSet()));
        final Map<Integer, Planet> planetMap = planetService.findAll(idPlanets).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));


        final Set<Integer> fleetIDs = orbitalTransportJobCache.keySet().stream().map(this::getFleetId).collect(Collectors.toSet());
        final Map<Integer, Fleet> fleetMap = fleetService.findAll(fleetIDs).stream()
                .collect(Collectors.toMap(AbstractEntityKey::getId, Function.identity()));

        LOGGER.info("\t...constructing cache");
        for (final Map.Entry<String, List<String>> entry : orbitalTransportJobCache.entrySet()) {
            final String key = entry.getKey();
            final List<String> values = entry.getValue();

            final OrbitalTransportJob job = getTodayOrbitalTransportJob(
                    tickMap.get(getTickId(key)),
                    planetMap.get(getIdPlanetFrom(key)),
                    fleetMap.get(getFleetId(key)),
                    getTransportType(key));

            values.stream()
                    .filter(value -> Arrays.stream(EResourceType.values()).anyMatch(eResourceType -> value.contains(eResourceType.name())))
                    .map(this::fromJsonToR)
                    .filter(Objects::nonNull)
                    .forEach(r -> job.add(r.getRealType(), r.getAmount()));

            values.stream()
                    .filter(value -> Arrays.stream(EEducationType.values()).anyMatch(eResourceType -> value.contains(eResourceType.name())))
                    .map(this::fromJsonToHR)
                    .filter(Objects::nonNull)
                    .forEach(r -> job.add(r.getRealType(), r.getAmount()));
        }

        for (final Map.Entry<String, List<String>> entry : transportJobCache.entrySet()) {
            final String key = entry.getKey();
            final List<String> values = entry.getValue();

            final TransportJob job = getTodayTransportJob(
                    tickMap.get(getTickId(key)),
                    planetMap.get(getIdPlanetFrom(key)),
                    planetMap.get(getIdPlanetTo(key)));

            values.stream()
                    .filter(value -> Arrays.stream(EResourceType.values()).anyMatch(eResourceType -> value.contains(eResourceType.name())))
                    .map(this::fromJsonToR)
                    .filter(Objects::nonNull)
                    .forEach(r -> job.add(r.getRealType(), r.getAmount()));

            values.stream()
                    .filter(value -> Arrays.stream(EEducationType.values()).anyMatch(eResourceType -> value.contains(eResourceType.name())))
                    .map(this::fromJsonToHR)
                    .filter(Objects::nonNull)
                    .forEach(r -> job.add(r.getRealType(), r.getAmount()));
        }
        LOGGER.info("Done loading cache into heap.");
    }

    @Nonnull
    private Set<Integer> getPlanetIDsFromKey(@Nonnull final String key) {
        Preconditions.checkNotNull(key, "key must not be empty");

        return Arrays.stream(key.split("\\|")).skip(1).map(Integer::parseInt).collect(Collectors.toSet());
    }

    private int getTickId(@Nonnull final String key) {
        return Integer.parseInt(key.split("\\|")[0]);
    }

    private int getFleetId(@Nonnull final String key) {
        return Integer.parseInt(key.split("\\|")[2]);
    }

    @Nonnull
    private ETransportType getTransportType(@Nonnull final String key) {
        return ETransportType.valueOf(ETransportType.class, key.split("\\|")[3]);
    }

    private int getIdPlanetFrom(@Nonnull final String key) {
        return Integer.parseInt(key.split("\\|")[1]);
    }

    private int getIdPlanetTo(@Nonnull final String key) {
        return Integer.parseInt(key.split("\\|")[2]);
    }

    public void add(@Nonnull final Tick today,
                    @Nonnull final Planet from,
                    @Nonnull final Planet to,
                    @Nonnull final EResourceType what,
                    final long amount) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(from, "from must not be empty");
        Preconditions.checkNotNull(to, "to must not be empty");
        Preconditions.checkNotNull(what, "what must not be empty");

        final TransportJob job = getTodayTransportJob(today, from, to);
        job.add(what, amount);
        final String value = toJson(new ResourceAmount(what, amount));
        cacheFileWriter.writeToFile(TransportJob.class, getKey(today, from, to), value);
    }

    public void add(@Nonnull final Tick today,
                    @Nonnull final Planet from,
                    @Nonnull final Planet to,
                    @Nonnull final EEducationType what,
                    final long amount) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(from, "from must not be empty");
        Preconditions.checkNotNull(to, "to must not be empty");
        Preconditions.checkNotNull(what, "what must not be empty");

        final TransportJob job = getTodayTransportJob(today, from, to);
        job.add(what, amount);

        final String value = toJson(new HumanResourceAmount(what, amount));
        cacheFileWriter.writeToFile(TransportJob.class, getKey(today, from, to), value);
    }


    @Nonnull
    private String toJson(@Nonnull final HumanResourceAmount whatever) {
        Preconditions.checkNotNull(whatever, "whatever must not be empty");

        return new GsonBuilder().create().toJson(whatever);
    }

    @Nonnull
    private String toJson(@Nonnull final ResourceAmount whatever) {
        Preconditions.checkNotNull(whatever, "whatever must not be empty");

        return new GsonBuilder().create().toJson(whatever);
    }

    @Nullable
    private ResourceAmount fromJsonToR(@Nonnull final String string) {
        Preconditions.checkNotNull(string, "string must not be empty");

        try {
            return new GsonBuilder().create().fromJson(string, ResourceAmount.class);
        } catch (final JsonSyntaxException ignore) {
        }
        return null;
    }

    @Nullable
    private HumanResourceAmount fromJsonToHR(@Nonnull final String string) {
        Preconditions.checkNotNull(string, "string must not be empty");

        try {
            return new GsonBuilder().create().fromJson(string, HumanResourceAmount.class);
        } catch (final JsonSyntaxException ignore) {
        }
        return null;
    }

    @Nonnull
    private TransportJob getTodayTransportJob(@Nonnull final Tick today,
                                              @Nonnull final Planet from,
                                              @Nonnull final Planet to) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(from, "from must not be empty");
        Preconditions.checkNotNull(to, "to must not be empty");
        //noinspection ConstantConditions
        Preconditions.checkState(from.getOwner().equals(to.getOwner()), "The id should match");

        Set<TransportJob> transportJobs = transportCache.get(from.getOwner().getId());
        if (transportJobs == null) {
            transportJobs = new HashSet<>();
            transportCache.put(from.getOwner().getId(), transportJobs);
        }
        final TransportJob transportJob = new TransportJob(today, from, to);
        final TransportJob job = transportJobs.stream().filter(t -> t.equals(transportJob)).findFirst().orElse(transportJob);
        transportJobs.add(job);
        return job;
    }

    public void add(@Nonnull final Tick today,
                    @Nonnull final Fleet fleet,
                    @Nonnull final Planet planet,
                    @Nonnull final ResourceDeposit transferred,
                    @Nonnull final ETransportType transportType) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(transferred, "transferred must not be empty");
        Preconditions.checkNotNull(transportType, "transportType must not be empty");

        if (planet.isColonizable()) {
            return;
        }


        final OrbitalTransportJob job = getTodayOrbitalTransportJob(today, planet, fleet, transportType);
        transferred.getResources().forEach((resourceType, amount) -> {
            final long current = job.getResources().getOrDefault(resourceType, 0L);
            job.add(resourceType, amount + current);
            final String value = toJson(new ResourceAmount(resourceType, amount));
            cacheFileWriter.writeToFile(OrbitalTransportJob.class, getKey(today, planet, fleet, transportType), value);
        });
        transferred.getHumanResources().forEach((educationType, amount) -> {
            final long current = job.getHumanResources().getOrDefault(educationType, 0L);
            job.add(educationType, amount + current);
            final String value = toJson(new HumanResourceAmount(educationType, amount));
            cacheFileWriter.writeToFile(OrbitalTransportJob.class, getKey(today, planet, fleet, transportType), value);
        });
    }

    @Nonnull
    private String getKey(@Nonnull final Tick today,
                          @Nonnull final Planet planet,
                          @Nonnull final Fleet fleet,
                          @Nonnull final ETransportType transportType) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(transportType, "transportType must not be empty");

        return today.getNo() + "|" + planet.getId() + "|" + fleet.getId() + "|" + transportType;
    }

    @Nonnull
    private OrbitalTransportJob getTodayOrbitalTransportJob(@Nonnull final Tick today,
                                                            @Nonnull final Planet planet,
                                                            @Nonnull final Fleet fleet,
                                                            @Nonnull final ETransportType transportType) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(transportType, "transportType must not be empty");

        //noinspection DataFlowIssue
        Set<OrbitalTransportJob> transportJobs = orbitalTransportCache.get(planet.getOwner().getId());
        if (transportJobs == null) {
            transportJobs = new HashSet<>();
            orbitalTransportCache.put(planet.getOwner().getId(), transportJobs);
        }
        final OrbitalTransportJob transportJob = new OrbitalTransportJob(today, planet, fleet, transportType);
        final OrbitalTransportJob job = transportJobs.stream().filter(t -> t.equals(transportJob)).findFirst().orElse(transportJob);
        transportJobs.add(job);
        return job;
    }

    @Nonnull
    public Set<TransportJob> getTransports(@Nonnull final Tick today,
                                           final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return Objects.requireNonNullElse(transportCache.get(idUser), new HashSet<TransportJob>()).stream().filter(t -> t.isToday(today)).collect(Collectors.toSet());
    }

    @Nonnull
    public Set<OrbitalTransportJob> getOrbitalTransports(@Nonnull final Tick today,
                                                         final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return Objects.requireNonNullElse(orbitalTransportCache.get(idUser), new HashSet<OrbitalTransportJob>()).stream().filter(t -> t.isToday(today)).collect(Collectors.toSet());
    }

    @Nonnull
    private String getKey(@Nonnull final Tick today, final int idPlanetFrom, final int idPlanetTo) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return today.getNo() + "|" + idPlanetFrom + "|" + idPlanetTo;
    }

    @Nonnull
    private String getKey(@Nonnull final Tick today, @Nonnull final Planet from, @Nonnull final Planet to) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(from, "from must not be empty");
        Preconditions.checkNotNull(to, "to must not be empty");

        return getKey(today, from.getId(), to.getId());
    }
}
