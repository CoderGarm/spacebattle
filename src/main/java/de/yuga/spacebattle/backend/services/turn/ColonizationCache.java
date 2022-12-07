package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.CacheStore;
import de.yuga.spacebattle.backend.dto.turn.FinishedColonization;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
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
    private final CacheStore<Integer, Set<FinishedColonization>> cache = new CacheStore<>(2, TimeUnit.DAYS);

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

        final Set<FinishedColonization> job = getTodayMovement(today, planet.getOwner());
        job.add(new FinishedColonization(today, planet));
    }

    @Nonnull
    private Set<FinishedColonization> getTodayMovement(@Nonnull final Tick today,
                                                       @Nonnull final User user) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");

        Set<FinishedColonization> movements = cache.get(user.getId());
        if (movements == null) {
            movements = new HashSet<>();
            cache.add(user.getId(), movements);
        }
        return movements;
    }
}
