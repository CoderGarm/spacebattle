package de.yuga.spacebattle.repositories.combined.spacecraft;

import de.yuga.spacebattle.entities.combined.spacecrafts.Fleet;
import org.springframework.data.repository.CrudRepository;

public interface FleetRepository extends CrudRepository<Fleet, Integer>, CustomFleetRepository {
}
