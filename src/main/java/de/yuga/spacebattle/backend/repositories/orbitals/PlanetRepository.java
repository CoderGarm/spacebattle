package de.yuga.spacebattle.backend.repositories.orbitals;


import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;

public interface PlanetRepository extends JpaRepository<Planet, Integer>, CustomPlanetRepository {

    @Nullable
    @Query("SELECT p.owner.id FROM Planet p WHERE p.id = :idPlanet AND p.isMain = true")
    Integer findAllById(@Param("idPlanet") final int idPlanet);
}
