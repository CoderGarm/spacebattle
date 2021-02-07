package de.yuga.spacebattle.repositories.buildings;

import de.yuga.spacebattle.entities.buildings.Building;
import org.springframework.data.repository.CrudRepository;

public interface BuildingRepository extends CrudRepository<Building, Integer>, CustomBuildingRepository {
}
