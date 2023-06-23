package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;

public interface ConstructionRepository extends CrudRepository<Construction, Integer>, CustomConstructionRepository {

    @Nullable
    @Query("SELECT p FROM ResourceDeposit p LEFT JOIN FETCH p.resources LEFT JOIN FETCH p.humanResources WHERE p.id = :idCosts AND p.subType = 'COSTS'")
    ResourceDeposit getCostsForBuilding(@Param("idCosts") final int idCosts);
}
