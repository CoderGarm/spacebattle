package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import org.springframework.data.repository.CrudRepository;

public interface ModuleRepository extends CrudRepository<Module, Integer>, CustomModuleRepository {
}
