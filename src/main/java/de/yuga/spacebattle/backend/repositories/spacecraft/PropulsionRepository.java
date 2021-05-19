package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import org.springframework.data.repository.CrudRepository;

public interface PropulsionRepository extends CrudRepository<Propulsion, Integer>, CustomPropulsionRepository {
}
