package de.yuga.spacebattle.repositories.spacecraft;

import de.yuga.spacebattle.entities.spacecrafts.Hull;
import org.springframework.data.repository.CrudRepository;

public interface HullRepository extends CrudRepository<Hull, Integer>, CustomHullRepository {
}
