package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CustomPlanetRepository {

    @Nonnull
    List<Planet> findAllPlanets();

    @Nonnull
    List<Planet> findAllOwnedPlanets();

    @Nonnull
    List<Planet> findAllPlanetsColonizedByUser(@Nonnull final User owner);

    @Nonnull
    List<Planet> findAllPlanetsColonizedByID(final int idUser);

    @Nullable
    Planet findResearchPlanet(final int idUser);

    @Nonnull
    Planet findMainPlanetForUser(final int idUser);

    @Nullable
    Planet findByCoordinates(final int idStarSystem, final Distance xCoordinate, final Distance yCoordinate);
}
