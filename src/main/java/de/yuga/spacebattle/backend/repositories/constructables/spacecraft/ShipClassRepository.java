package de.yuga.spacebattle.backend.repositories.constructables.spacecraft;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import org.springframework.data.repository.CrudRepository;

public interface ShipClassRepository extends CrudRepository<ShipClass, Integer>, CustomShipClassRepository {
}
