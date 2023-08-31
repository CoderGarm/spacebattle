package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

public interface ConstructionRepository extends JpaRepository<Construction, Integer>, CustomConstructionRepository {

    @Nullable
    @Query("SELECT c FROM Construction c WHERE c.planet.owner.id = :idUser AND c.level > c.operationalLevel")
    List<Construction> findInoperationalForUser(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT c FROM Construction c WHERE c.planet.owner.id = :idUser")
    List<Construction> findAllConstructionsForUser(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT c FROM Construction c WHERE c.planet.id = :idPlanet AND c.level > c.operationalLevel")
    List<Construction> findInoperationalForPlanet(@Param("idPlanet") final int idPlanet);
}
