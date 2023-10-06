package de.yuga.spacebattle.backend.services.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.TickOutputCalculator;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.orbitals.PlanetRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    public List<Planet> findByIds(@Nonnull final Collection<Integer> planetIDs) {
        Preconditions.checkNotNull(planetIDs, "planetIDs must not be empty");

        final Iterable<Planet> allById = planetRepository.findAllById(planetIDs);
        return StreamSupport.stream(allById.spliterator(), false).collect(Collectors.toList());
    }

    @Nonnull
    public List<Planet> findAllColonized() {
        return planetRepository.findAllOwnedPlanets();
    }

    @Nonnull
    public List<Planet> findAllColonizedBy(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return planetRepository.findAllPlanetsColonizedByUser(user);
    }

    @Nonnull
    public List<Planet> findAllColonizedBy(final int idUser) {
        return planetRepository.findAllPlanetsColonizedByID(idUser);
    }

    @Nonnull
    public BigDecimal getEmpireWideResearchPoints(final int idUser) {
        return getEmpireWideResearchPoints(idUser, Map.of());
    }

    @Nonnull
    public BigDecimal getEmpireWideResearchPoints(final int idUser, @Nonnull final Map<Construction, Integer> formerLaboratoryOperationalLevel) {
        Preconditions.checkNotNull(formerLaboratoryOperationalLevel, "formerLaboratoryOperationalLevel must not be empty");

        final List<Planet> allColonizedByWithResearchLab = findAllColonizedByWithResearchLab(idUser);
        //noinspection UnnecessaryLocalVariable
        final BigDecimal empireWideResearchPoints = allColonizedByWithResearchLab.stream()
                .map(planet -> {
                    final Construction laboratory = planet.getConstructionByResource(EResourceType.RESEARCH).stream().findFirst().orElse(null);
                    if (laboratory != null) {
                        final int opLevel = formerLaboratoryOperationalLevel.getOrDefault(laboratory, laboratory.getOperationalLevel());
                        laboratory.setOperationalLevel(opLevel);
                        return TickOutputCalculator.getTickOutput(Set.of(laboratory));
                    }
                    return BigDecimal.valueOf(0);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return empireWideResearchPoints;
    }

    @Nonnull
    List<Planet> findAllColonizedByWithResearchLab(final int idUser) {
        return Objects.requireNonNullElse(planetRepository.findAllColonizedByWithResearchLab(idUser), List.of());
    }

    @Nullable
    public Planet findResearchPlanet(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return findResearchPlanet(user.getId());
    }

    @Nullable
    public Planet findResearchPlanet(final int idUser) {
        return planetRepository.findResearchPlanet(idUser);
    }

    @Nonnull
    public Planet findMainPlanet(@Nonnull final Owner user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return planetRepository.findMainPlanetForUser(user.getId());
    }

    @Nonnull
    public Planet findMainPlanet(final int idUser) {
        return planetRepository.findMainPlanetForUser(idUser);
    }

    @Nullable
    public Planet find(final int idPlanet) {
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
     * @param system      the system where the planet lives in
     * @param xCoordinate the x coordinate for the map in the star system
     * @param yCoordinate the y coordinate for the map in the star system
     * @return the new planet
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public Planet createPlanet(@Nonnull final String name,
                               @Nonnull final StarSystem system,
                               final int xCoordinate,
                               final int yCoordinate) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(system, "system shouldn't be null!");

        return planetRepository.save(new Planet(name, system, new Orbit(new Distance(xCoordinate, Planet.PLANET_STANDARD_METRIC), new Distance(yCoordinate, Planet.PLANET_STANDARD_METRIC))));
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    public Planet createPlanet(@Nonnull final String name,
                               @Nonnull final StarSystem system,
                               @Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(system, "system shouldn't be null!");
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        return planetRepository.save(new Planet(name, system, orbit));
    }

    public Planet save(@Nonnull final Planet entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return planetRepository.save(entity);
    }

    @Nullable
    public Planet findByCoordinates(@Nonnull final FleetOrbit fleetOrbit) {
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit must not be empty");

        if (fleetOrbit.getSystem() == null || fleetOrbit.getOrbit() == null) {
            return null;
        }

        return findByCoordinates(fleetOrbit.getSystem().getId(), fleetOrbit.getOrbit().getXCoordinate(), fleetOrbit.getOrbit().getYCoordinate());
    }

    @Nullable
    public Planet findByCoordinates(final int idStarSystem, final Distance xCoordinate, final Distance yCoordinate) {

        return planetRepository.findByCoordinates(idStarSystem, xCoordinate, yCoordinate);
    }

    public void saveAll(@Nonnull final Collection<Planet> modified) {
        Preconditions.checkNotNull(modified, "modified must not be empty");

        planetRepository.saveAll(modified);
    }

    @Nonnull
    public List<Planet> findAll(@Nonnull final Collection<Integer> idPlanets) {
        Preconditions.checkNotNull(idPlanets, "idPlanets must not be empty");

        return Objects.requireNonNullElse(planetRepository.findAllById(idPlanets), new ArrayList<>());
    }

    /**
     * Obscure method born by the wish to be done.<br>
     * Returns the id of the owner if the given idPlanet points to the main planet of the owner.
     */
    @Nullable
    public Integer getIdUserWhenMain(final int idPlanet) {
        return planetRepository.findAllById(idPlanet);
    }
}
