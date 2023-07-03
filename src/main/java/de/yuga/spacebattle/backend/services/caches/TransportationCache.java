package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.OrbitalTransportJob;
import de.yuga.spacebattle.backend.dto.turn.TransportJob;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.ETransportType;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TransportationCache {

    @Nonnull
    private final CacheStore<Integer, Set<TransportJob>> transportCache = new CacheStore<>(2, TimeUnit.DAYS);

    @Nonnull
    private final CacheStore<Integer, Set<OrbitalTransportJob>> orbitalTransportCache = new CacheStore<>(2, TimeUnit.DAYS);

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
            transportCache.add(from.getOwner().getId(), transportJobs);
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
        });
        transferred.getHumanResources().forEach((educationType, amount) -> {
            final long current = job.getHumanResources().getOrDefault(educationType, 0L);
            job.add(educationType, amount + current);
        });
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
            orbitalTransportCache.add(planet.getOwner().getId(), transportJobs);
        }
        final OrbitalTransportJob transportJob = new OrbitalTransportJob(today, planet, fleet, transportType);
        final OrbitalTransportJob job = transportJobs.stream().filter(t -> t.equals(transportJob)).findFirst().orElse(transportJob);
        transportJobs.add(job);
        return job;
    }
}
