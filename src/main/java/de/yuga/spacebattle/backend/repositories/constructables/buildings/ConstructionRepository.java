package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

public interface ConstructionRepository extends CrudRepository<Construction, Integer>, CustomConstructionRepository {

    @Nullable
    @Query("SELECT c FROM Construction c WHERE c.planet.owner.id = :idUser AND c.level > c.operationalLevel")
    List<Construction> findInoperationalForUser(@Param("idUser") final int idUser);
}
