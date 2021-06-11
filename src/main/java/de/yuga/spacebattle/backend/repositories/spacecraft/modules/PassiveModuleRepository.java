package de.yuga.spacebattle.backend.repositories.spacecraft.modules;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule;
import org.springframework.data.repository.CrudRepository;

public interface PassiveModuleRepository extends CrudRepository<PassiveModule, Integer>, CustomPassiveModuleRepository {
}
