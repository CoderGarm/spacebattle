package de.yuga.spacebattle.backend.services.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.repositories.orbitals.StarSystemRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class StarSystemService {

    @Nonnull
    private final StarSystemRepository starsystemRepository;

    public StarSystemService(@Nonnull final StarSystemRepository starsystemRepository) {
        Preconditions.checkNotNull(starsystemRepository, "starsystemRepository shouldn't be null!");

        this.starsystemRepository = starsystemRepository;
    }

    @Nonnull
    public List<StarSystem> findAll() {
        return starsystemRepository.findAllStarSystems();
    }

    @Nonnull
    public List<StarSystem> findByIds(List<Integer> fleetIDs) {
        final Iterable<StarSystem> allById = starsystemRepository.findAllById(fleetIDs);
        return StreamSupport.stream(allById.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Returns all system which has at least one free planet.
     *
     * @return star systems with uncolonized planets
     */
    @Nonnull
    public List<StarSystem> findAllColonizable() {
        return starsystemRepository.findAllColonizable();
    }

    /**
     * Returns all system which has at least one colonized planet.
     *
     * @return star systems with uncolonized planets
     */
    @Nonnull
    public List<StarSystem> findAllColonized() {
        return starsystemRepository.findAllColonized();
    }

    @Nullable
    public StarSystem find(final int idStarSystem) {
        return starsystemRepository.findById(idStarSystem).orElse(null);
    }

    /**
     * Creates a new {@link StarSystem}.
     *
     * @param xCoordinate the x coordinate for the star map
     * @param yCoordinate the y coordinate for the star map
     * @return the new system
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public StarSystem createStarSystem(@Nonnull final String name,
                                       final int xCoordinate,
                                       final int yCoordinate) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");

        return starsystemRepository.save(new StarSystem(name, new Orbit(new Distance(xCoordinate, StarSystem.STAR_SYSTEM_STANDARD_METRIC), new Distance(yCoordinate, EDistanceMetric.LY))));
    }

    @Nonnull
    public StarSystem createStarSystem(@Nonnull final String name,
                                       @Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        return starsystemRepository.save(new StarSystem(name, orbit));
    }

    @Nullable
    public StarSystem find(@Nonnull final StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        return starsystemRepository.findById(starSystem.getId()).orElse(null);
    }
}
