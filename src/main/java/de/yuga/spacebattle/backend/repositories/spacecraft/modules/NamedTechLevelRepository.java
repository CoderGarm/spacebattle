package de.yuga.spacebattle.backend.repositories.spacecraft.modules;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import org.springframework.data.repository.CrudRepository;

public interface NamedTechLevelRepository extends CrudRepository<NamedTechLevel, Integer> {
}
