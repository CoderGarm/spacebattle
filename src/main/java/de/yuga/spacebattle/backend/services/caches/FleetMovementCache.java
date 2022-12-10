package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.FleetMovement;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

        final User notificationTarget;
        if (destination.getOwner() == null || fleet.getOwner().equals(destination.getOwner())) {
            notificationTarget = fleet.getOwner();
        } else {
            notificationTarget = destination.getOwner();
            notifyFleetOwner(today, fleet, new FleetMovement(today, fleet, origin, destination, move));
        }

        getTodayMovement(today, notificationTarget).add(new FleetMovement(today, fleet, origin, destination, move));
    }

    public void add(@Nonnull final Tick today,
                    @Nonnull final Fleet fleet,
                    @Nonnull final Move move,
                    @Nullable final Planet originPlanet,
                    @Nonnull final StarSystem originSystem,
                    @Nonnull final StarSystem destinationSystem) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(move, "move must not be empty");
        Preconditions.checkNotNull(destinationSystem, "destinationSystem must not be empty");

        final Set<User> owners = destinationSystem.getPlanets().stream()
                .map(Planet::getOwner)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        owners.forEach(notificationTarget -> {
            getTodayMovement(today, notificationTarget)
                    .add(new FleetMovement(today, fleet, originPlanet, originSystem, destinationSystem, move, !fleet.getOwner().equals(notificationTarget)));
        });

        if (!owners.contains(fleet.getOwner())) {
            notifyFleetOwner(today, fleet, new FleetMovement(today, fleet, originPlanet, originSystem, destinationSystem, move, false));
        }
    }

    private void notifyFleetOwner(@Nonnull final Tick today, @Nonnull final Fleet fleet, @Nonnull final FleetMovement fleetMovement) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(fleetMovement, "fleetMovement must not be empty");

        getTodayMovement(today, fleet.getOwner()).add(fleetMovement);
    }

    @Nonnull
    private Set<FleetMovement> getTodayMovement(@Nonnull final Tick today,
                                                @Nonnull final User user) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");

        Set<FleetMovement> movements = cache.get(user.getId());
        if (movements == null) {
            movements = new HashSet<>();
            cache.add(user.getId(), movements);
        }
        return movements;
    }
}
