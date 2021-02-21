package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import java.util.List;

public interface CustomPlanetRepository {

    List<Planet> findAllPlanets();

    List<Planet> findAllOwnedPlanets();

    List<Planet> findAllPlanetsColonizedBy(User user);
}
