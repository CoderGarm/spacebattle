package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import org.springframework.data.repository.CrudRepository;

public interface StarSystemRepository extends CrudRepository<StarSystem, Integer>, CustomStarSystemRepository {
}
