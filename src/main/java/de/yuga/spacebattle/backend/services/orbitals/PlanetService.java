package de.yuga.spacebattle.backend.services.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.Starsystem;
import de.yuga.spacebattle.backend.repositories.orbitals.PlanetRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class PlanetService {

    @Nonnull
    private final PlanetRepository planetRepository;

    public PlanetService(@Nonnull final PlanetRepository planetRepository) {
        Preconditions.checkNotNull(planetRepository, "planetRepository shouldn't be null!");

        this.planetRepository = planetRepository;
    }

    @Nonnull
    public List<Planet> findAll() {
        return planetRepository.findAllPlanets();
    }

    @Nonnull
    public List<Planet> findAllColonized() {
        return planetRepository.findAllOwnedPlanets();
    }

    @Nullable
    public Planet find(@Nonnull final Integer idPlanet) {
        Preconditions.checkNotNull(idPlanet, "idPlanet shouldn't be null!");
        return planetRepository.findById(idPlanet).orElse(null);
    }

    /**
     * Creates a new {@link Planet}.
     *
     * @param name        the name of the planet
     * @param owner       the guy who colonized this planet - or even not
     * @param system      the system where the planet lives in
     * @param xCoordinate the x coordinate for the map in the star system
     * @param yCoordinate the y coordinate for the map in the star system
     * @return the new planet
     */
    @Nonnull
    public Planet createPlanet(@Nonnull final String name,
                               @Nullable final User owner,
                               @Nonnull final Starsystem system,
                               @Nonnull final Integer xCoordinate,
                               @Nonnull final Integer yCoordinate) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");
        Preconditions.checkNotNull(system, "system shouldn't be null!");
        Preconditions.checkNotNull(xCoordinate, "xCoordinate shouldn't be null!");
        Preconditions.checkNotNull(yCoordinate, "yCoordinate shouldn't be null!");

        Planet entity = new Planet(owner, name, system, new Orbit(xCoordinate, yCoordinate));
        return planetRepository.save(entity);
    }

    public Planet save(@Nonnull final Planet entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return planetRepository.save(entity);
    }
}
