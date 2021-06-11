package de.yuga.spacebattle.backend.repositories.spacecraft.modules;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import org.springframework.data.repository.CrudRepository;

public interface ElectronicWarfareRepository extends CrudRepository<ElectronicWarfare, Integer>, CustomElectronicWarfareRepository {
}
