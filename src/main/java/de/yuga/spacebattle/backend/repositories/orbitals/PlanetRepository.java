package de.yuga.spacebattle.backend.repositories.orbitals;


import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanetRepository extends JpaRepository<Planet, Integer>, CustomPlanetRepository {
}
