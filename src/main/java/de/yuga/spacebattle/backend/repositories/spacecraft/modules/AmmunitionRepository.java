package de.yuga.spacebattle.backend.repositories.spacecraft.modules;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;
import org.springframework.data.repository.CrudRepository;

public interface AmmunitionRepository extends CrudRepository<AmmunitionModule, Integer>, CustomAmmunitionRepository {
}
