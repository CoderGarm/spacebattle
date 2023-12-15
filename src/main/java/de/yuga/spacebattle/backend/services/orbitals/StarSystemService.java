package de.yuga.spacebattle.backend.services.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.OrbitalDistanceMarker;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.repositories.orbitals.StarSystemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StarSystemService {

    @Nonnull
    private final StarSystemRepository starsystemRepository;

    @Autowired
    public StarSystemService(@Nonnull final StarSystemRepository starsystemRepository) {
        Preconditions.checkNotNull(starsystemRepository, "starsystemRepository shouldn't be null!");

        this.starsystemRepository = starsystemRepository;
    }

    @Nonnull
    public List<StarSystem> findAll() {
        return starsystemRepository.findAllStarSystems();
    }

    @Nonnull
    public List<StarSystem> findAll(@Nonnull final Collection<Integer> ids) {
        Preconditions.checkNotNull(ids, "ids must not be empty");

        return starsystemRepository.findAllById(ids);
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

    @Deprecated(since = "productive environment")
    public void saveAll(@Nonnull final List<StarSystem> modified) {
        Preconditions.checkNotNull(modified, "modified must not be empty");

        starsystemRepository.saveAll(modified);
    }

    @Nullable
    public StarSystem findByName(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name must not be empty");

        return starsystemRepository.findByName(name);
    }

    @Nonnull
    public Set<StarSystem> findByNames(@Nonnull final Set<String> systemNames) {
        Preconditions.checkNotNull(systemNames, "systemNames must not be empty");

        return Objects.requireNonNullElse(starsystemRepository.findByNames(systemNames), new HashSet<>());
    }

    @Nonnull
    public Set<Planet> findNeighbourPlanets(@Nonnull final StarSystem system) {
        Preconditions.checkNotNull(system, "system must not be empty");

        final Set<StarSystem> neighbours = findNeighbours(system);
        return neighbours.stream()
                .map(StarSystem::getPlanets)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Nonnull
    public Set<StarSystem> findNeighbours(@Nonnull final StarSystem system) {
        Preconditions.checkNotNull(system, "system must not be empty");

        final List<StarSystem> allColonizable = findAllColonizable();

        final List<OrbitalDistanceMarker> marker = allColonizable.stream()
                .map(s -> new OrbitalDistanceMarker(system.getOrbit(), s.getOrbit()))
                .sorted(Comparator.comparing(OrbitalDistanceMarker::getDistance))
                .collect(Collectors.toList());
        marker.removeIf(s -> marker.indexOf(s) > 3);

        final Set<Orbit> neighbourOrbits = marker.stream()
                .map(OrbitalDistanceMarker::getSecond)
                .collect(Collectors.toSet());

        return allColonizable.stream()
                .filter(s -> neighbourOrbits.contains(s.getOrbit()))
                .collect(Collectors.toSet());
    }
}
