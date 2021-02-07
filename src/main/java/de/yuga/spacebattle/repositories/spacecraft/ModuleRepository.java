package de.yuga.spacebattle.repositories.spacecraft;

import de.yuga.spacebattle.entities.spacecrafts.Module;
import org.springframework.data.repository.CrudRepository;

public interface ModuleRepository extends CrudRepository<Module, Integer>, CustomModuleRepository {
}
