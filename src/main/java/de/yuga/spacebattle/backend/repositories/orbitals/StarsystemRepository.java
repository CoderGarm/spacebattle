package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import org.springframework.data.repository.CrudRepository;

public interface StarsystemRepository extends CrudRepository<StarSystem, Integer>, CustomStarsystemRepository {
}
