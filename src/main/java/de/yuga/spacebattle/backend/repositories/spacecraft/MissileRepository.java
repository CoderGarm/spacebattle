package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import org.springframework.data.repository.CrudRepository;

public interface MissileRepository extends CrudRepository<Missile, Integer> {
}
