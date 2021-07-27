package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import org.springframework.data.repository.CrudRepository;

public interface MissileMotorRepository extends CrudRepository<MissileMotor, Integer> {
}
