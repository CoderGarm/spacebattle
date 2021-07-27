package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead;
import org.springframework.data.repository.CrudRepository;

public interface WarheadRepository extends CrudRepository<Warhead, Integer> {
}
