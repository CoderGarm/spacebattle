package de.yuga.spacebattle.backend.services.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.repositories.orbitals.StarsystemRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class StarSystemService {

    @Nonnull
    private final StarsystemRepository starsystemRepository;

    public StarSystemService(@Nonnull final StarsystemRepository starsystemRepository) {
        Preconditions.checkNotNull(starsystemRepository, "starsystemRepository shouldn't be null!");

        this.starsystemRepository = starsystemRepository;
    }

    @Nonnull
    public List<StarSystem> findAll() {
        return starsystemRepository.findAllStarsystems();
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
                                       @Nonnull final Integer xCoordinate,
                                       @Nonnull final Integer yCoordinate) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(xCoordinate, "xCoordinate shouldn't be null!");
        Preconditions.checkNotNull(yCoordinate, "yCoordinate shouldn't be null!");

        return starsystemRepository.save(new StarSystem(name, new Orbit(xCoordinate, yCoordinate)));
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
