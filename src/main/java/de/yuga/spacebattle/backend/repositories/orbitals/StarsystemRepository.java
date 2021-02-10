package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.Starsystem;
import org.springframework.data.repository.CrudRepository;

public interface StarsystemRepository extends CrudRepository<Starsystem, Integer>, CustomStarsystemRepository {
}
