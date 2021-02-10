package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import org.springframework.data.repository.CrudRepository;

public interface FleetRepository extends CrudRepository<Fleet, Integer>, CustomFleetRepository {
}
