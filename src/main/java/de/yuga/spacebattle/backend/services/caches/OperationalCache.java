package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.Commissioning;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OperationalCache extends BaseCache {

    @Nonnull
    private final CacheStore<String, Set<Commissioning>> cache = new CacheStore<>(2, TimeUnit.DAYS);

    @Nonnull
    public Set<Commissioning> getOperationals(@Nonnull final Tick today,
                                              final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        final String cacheKey = getCacheKey(today, idUser);
        return Objects.requireNonNullElse(cache.get(cacheKey), new HashSet<Commissioning>()).stream().filter(t -> t.isToday(today)).collect(Collectors.toSet());
    }

    @Nonnull
    private Set<Commissioning> getTodayCommissioning(@Nonnull final Tick today,
                                                     @Nonnull final User user) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(user, "user must not be empty");

        final String cacheKey = getCacheKey(today, user);
        Set<Commissioning> commissionings = cache.get(cacheKey);
        if (commissionings == null) {
            commissionings = new HashSet<>();
            cache.add(cacheKey, commissionings);
        }
        return commissionings;
    }

    public void activateWarships(@Nonnull final Tick today,
                                 @Nonnull final Planet planet,
                                 @Nonnull final List<WarShip> operationals) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(planet.getOwner(), "planet.getOwner() must not be empty");
        Preconditions.checkNotNull(operationals, "operationals must not be empty");

        final Set<Commissioning> commissionings = getTodayCommissioning(today, planet.getOwner());
        commissionings.stream()
                .filter(c -> c.getPlanet().equals(planet))
                .findFirst()
                .ifPresentOrElse(commissioning -> commissioning.setWarships(operationals),
                        () -> commissionings.add(new Commissioning(today, planet, operationals)));
    }

    public void activateConstructions(@Nonnull final Tick today,
                                      @Nonnull final Planet planet,
                                      @Nonnull final Set<Construction> operationals) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(planet.getOwner(), "planet.getOwner() must not be empty");
        Preconditions.checkNotNull(operationals, "operationals must not be empty");

        final Set<Commissioning> commissionings = getTodayCommissioning(today, planet.getOwner());
        commissionings.stream()
                .filter(c -> c.getPlanet().equals(planet))
                .findFirst()
                .ifPresentOrElse(commissioning -> commissioning.addConstructions(operationals),
                        () -> commissionings.add(new Commissioning(today, planet, operationals)));
    }
}
