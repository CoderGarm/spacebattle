package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.CacheStore;
import de.yuga.spacebattle.backend.dto.turn.FleetMovement;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FleetMovementCache {

    @Nonnull
    private final CacheStore<Integer, Set<FleetMovement>> cache = new CacheStore<>(2, TimeUnit.DAYS);

    @Nonnull
    public Set<FleetMovement> getMovements(@Nonnull final Tick today,
                                           final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        return Objects.requireNonNullElse(cache.get(idUser), new HashSet<FleetMovement>()).stream().filter(t -> t.isToday(today)).collect(Collectors.toSet());
    }

    public void add(@Nonnull final Tick today,
                    @Nonnull final Fleet fleet,
                    @Nonnull final Move move,
                    @Nonnull final Planet origin,
                    @Nonnull final Planet destination) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(move, "move must not be empty");
        Preconditions.checkNotNull(origin, "origin must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        final Set<FleetMovement> job = getTodayMovement(today, fleet);
        job.add(new FleetMovement(today, fleet, origin, destination, move));
    }

    @Nonnull
    private Set<FleetMovement> getTodayMovement(@Nonnull final Tick today,
                                                @Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        Set<FleetMovement> movements = cache.get(fleet.getOwner().getId());
        if (movements == null) {
            movements = new HashSet<>();
            cache.add(fleet.getOwner().getId(), movements);
        }
        return movements;
    }
}
