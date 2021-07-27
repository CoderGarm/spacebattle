package de.yuga.spacebattle.backend.repositories.spacecraft.modules;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomArmorRepository;
import org.springframework.data.repository.CrudRepository;

public interface ArmorRepository extends CrudRepository<Armor, Integer>, CustomArmorRepository {
}
