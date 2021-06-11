package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import org.springframework.data.repository.CrudRepository;

public interface ShipClassRepository extends CrudRepository<ShipClass, Integer>, CustomShipClassRepository {
}
