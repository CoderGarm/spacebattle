package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import org.springframework.data.repository.CrudRepository;

public interface WeaponRepository extends CrudRepository<Weapon, Integer>, CustomWeaponRepository {
}
