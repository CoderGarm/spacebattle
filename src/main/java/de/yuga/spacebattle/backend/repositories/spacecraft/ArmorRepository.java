package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import org.springframework.data.repository.CrudRepository;

public interface ArmorRepository extends CrudRepository<Armor, Integer>, CustomArmorRepository {
}
