package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import org.springframework.data.repository.CrudRepository;

public interface ConstructionRepository extends CrudRepository<Construction, Integer>, CustomConstructionRepository {
}
