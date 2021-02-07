package de.yuga.spacebattle.repositories.constructables.spacecraft;

import de.yuga.spacebattle.entities.constructables.spacecrafts.ShipClass;
import org.springframework.data.repository.CrudRepository;

public interface ShipClassRepository extends CrudRepository<ShipClass, Integer>, CustomShipClassRepository {
}
