package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import org.springframework.data.repository.CrudRepository;

public interface SidewallRepository extends CrudRepository<Sidewall, Integer>, CustomSidewallRepository {
}
