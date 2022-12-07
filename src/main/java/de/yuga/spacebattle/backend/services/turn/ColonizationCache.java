package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.CacheStore;
import de.yuga.spacebattle.backend.dto.turn.TransportJob;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ColonizationCache {

    @Nonnull
    private final CacheStore<Integer, Set<TransportJob>> transportCache = new CacheStore<>(2, TimeUnit.DAYS);

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
}
