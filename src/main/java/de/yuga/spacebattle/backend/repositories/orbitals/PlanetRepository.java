package de.yuga.spacebattle.backend.repositories.orbitals;


import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

public interface PlanetRepository extends JpaRepository<Planet, Integer>, CustomPlanetRepository {

    @Nullable
    @Query("SELECT p.owner.id FROM Planet p WHERE p.id = :idPlanet AND p.isMain = true")
    Integer findAllById(@Param("idPlanet") final int idPlanet);

    @Nullable
    @Query("SELECT DISTINCT p FROM Planet p WHERE p.owner.id = :idUser " +
            "AND true = (SELECT CASE WHEN (COUNT(c) > 0) THEN TRUE ELSE FALSE END FROM Construction c WHERE c.planet = p AND c.building.productionType.productionTarget = de.yuga.spacebattle.backend.enums.EResourceType.RESEARCH) " +
            "ORDER BY p.colonizedAt")
    List<Planet> findAllColonizedByWithResearchLab(@Param("idUser") final int idUser);
}
