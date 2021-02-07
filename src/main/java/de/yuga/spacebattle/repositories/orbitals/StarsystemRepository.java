package de.yuga.spacebattle.repositories.orbitals;

import de.yuga.spacebattle.entities.orbitals.Starsystem;
import org.springframework.data.repository.CrudRepository;

public interface StarsystemRepository extends CrudRepository<Starsystem, Integer>, CustomStarsystemRepository {
}
