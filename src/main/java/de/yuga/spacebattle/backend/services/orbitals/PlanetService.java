package de.yuga.spacebattle.backend.services.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.repositories.orbitals.PlanetRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class PlanetService {

    @Nonnull
    private final PlanetRepository planetRepository;

    @Nonnull
    private final UserService userService;

    public PlanetService(@Nonnull final PlanetRepository planetRepository, @Nonnull final UserService userService) {
        Preconditions.checkNotNull(planetRepository, "planetRepository shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.planetRepository = planetRepository;
        this.userService = userService;
    }

    @Nonnull
    public List<Planet> findAll() {
        return planetRepository.findAllPlanets();
    }

    @Nonnull
    public List<Planet> findAllColonized() {
        return planetRepository.findAllOwnedPlanets();
    }

    @Nonnull
    public List<Planet> findAllColonizedBy(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return planetRepository.findAllPlanetsColonizedBy(user);
    }

    @Nullable
    public Planet find(@Nonnull final Integer idPlanet) {
        Preconditions.checkNotNull(idPlanet, "idPlanet shouldn't be null!");

        return planetRepository.findById(idPlanet).orElse(null);
    }

    @Nullable
    public Planet find(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return planetRepository.findById(planet.getId()).orElse(null);
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
    @Transactional(rollbackFor = Exception.class)
    @Deprecated(since = "productive environment")
    public Planet createPlanet(@Nonnull final String name,
                               @Nullable final User owner,
                               @Nonnull final StarSystem system,
                               @Nonnull final Integer xCoordinate,
                               @Nonnull final Integer yCoordinate) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(system, "system shouldn't be null!");
        Preconditions.checkNotNull(xCoordinate, "xCoordinate shouldn't be null!");
        Preconditions.checkNotNull(yCoordinate, "yCoordinate shouldn't be null!");

        if (owner != null) {
            owner.getKnownStarSystems().add(system);
            userService.save(owner);
        }
        return planetRepository.save(new Planet(owner, name, system, new Orbit(xCoordinate, yCoordinate)));
    }

    @Nonnull
    @Transactional(rollbackFor = Exception.class)
    @Deprecated(since = "productive environment")
    public Planet createPlanet(@Nonnull final String name,
                               @Nullable final User owner,
                               @Nonnull final StarSystem system,
                               @Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(system, "system shouldn't be null!");
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        if (owner != null) {
            owner.addKnownStarSystems(system);
            userService.save(owner);
        }
        return planetRepository.save(new Planet(owner, name, system, orbit));
    }

    public Planet save(@Nonnull final Planet entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return planetRepository.save(entity);
    }

    /**
     * Colonizes a planet for a owner.
     * Currently this implies that the new owner will get all information about the system without buying it especially.
     *
     * @param owner  the new owner
     * @param planet the planet to colonize
     * @return the colonized planet
     */
    @Transactional(rollbackFor = Exception.class)
    public Planet colonizePlanet(@Nonnull final User owner, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        planet.setOwner(owner);
        owner.addKnownStarSystems(planet.getSystem());
        userService.save(owner);
        return save(planet);
    }
}
