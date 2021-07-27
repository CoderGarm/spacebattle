package de.yuga.spacebattle.backend.repositories.spacecraft.modules;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomSidewallRepository;
import org.springframework.data.repository.CrudRepository;

public interface SidewallRepository extends CrudRepository<Sidewall, Integer>, CustomSidewallRepository {
}
