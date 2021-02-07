package de.yuga.spacebattle.logic.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.orbitals.Orbit;
import de.yuga.spacebattle.entities.orbitals.Starsystem;
import de.yuga.spacebattle.repositories.orbitals.StarsystemRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class StarsystemService {

    @Nonnull
    private final StarsystemRepository starsystemRepository;

    public StarsystemService(@Nonnull final StarsystemRepository starsystemRepository) {
        Preconditions.checkNotNull(starsystemRepository, "starsystemRepository shouldn't be null!");

        this.starsystemRepository = starsystemRepository;
    }

    @Nonnull
    public List<Starsystem> findAll() {
        return starsystemRepository.findAllStarsystems();
    }

    @Nullable
    public Starsystem find(@Nonnull final Integer idStarsystem) {
        Preconditions.checkNotNull(idStarsystem, "idStarsystem shouldn't be null!");
        return starsystemRepository.findById(idStarsystem).orElse(null);
    }

    /**
     * Creates a new {@link Starsystem}.
     *
     * @param xCoordinate the x coordinate for the star map
     * @param yCoordinate the y coordinate for the star map
     * @return the new system
     */
    @Nonnull
    public Starsystem createStarsystem(@Nonnull final String name,
                                       @Nonnull final Integer xCoordinate,
                                       @Nonnull final Integer yCoordinate) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(xCoordinate, "xCoordinate shouldn't be null!");
        Preconditions.checkNotNull(yCoordinate, "yCoordinate shouldn't be null!");

        return starsystemRepository.save(new Starsystem(name, new Orbit(xCoordinate, yCoordinate)));
    }
}
