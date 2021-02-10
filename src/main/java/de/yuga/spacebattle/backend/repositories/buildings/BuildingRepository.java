package de.yuga.spacebattle.backend.repositories.buildings;

import de.yuga.spacebattle.backend.entities.buildings.Building;
import org.springframework.data.repository.CrudRepository;

public interface BuildingRepository extends CrudRepository<Building, Integer>, CustomBuildingRepository {
}
