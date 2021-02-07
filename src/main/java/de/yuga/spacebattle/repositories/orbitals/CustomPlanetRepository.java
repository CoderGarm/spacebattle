package de.yuga.spacebattle.repositories.orbitals;

import de.yuga.spacebattle.entities.orbitals.Planet;

import java.util.List;

public interface CustomPlanetRepository {

    List<Planet> findAllPlanets();

    List<Planet> findAllOwnedPlanets();
}
