package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.repositories.spacecraft.custom.CustomHullRepository;
import org.springframework.data.repository.CrudRepository;

public interface HullRepository extends CrudRepository<Hull, Integer>, CustomHullRepository {
}
