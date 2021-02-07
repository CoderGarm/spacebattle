package de.yuga.spacebattle.repositories.orbitals;


import de.yuga.spacebattle.entities.orbitals.Planet;
import org.springframework.data.repository.CrudRepository;

public interface PlanetRepository extends CrudRepository<Planet, Integer>, CustomPlanetRepository {
}
