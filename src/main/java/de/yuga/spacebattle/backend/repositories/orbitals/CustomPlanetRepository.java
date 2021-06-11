package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CustomPlanetRepository {

    List<Planet> findAllPlanets();

    List<Planet> findAllOwnedPlanets();

    @Nonnull
    List<Planet> findAllPlanetsColonizedBy(@Nonnull final User owner);

    @Nullable
    Planet findResearchPlanet(@Nonnull final User owner);

    @Nonnull
    Planet findMainPlanetForUser(@Nonnull final User owner);
}
