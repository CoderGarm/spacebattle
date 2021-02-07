package de.yuga.spacebattle.repositories.constructables.buildings;

import de.yuga.spacebattle.entities.constructables.buildings.Construction;
import org.springframework.data.repository.CrudRepository;

public interface ConstructionRepository extends CrudRepository<Construction, Integer>, CustomConstructionRepository {
}
