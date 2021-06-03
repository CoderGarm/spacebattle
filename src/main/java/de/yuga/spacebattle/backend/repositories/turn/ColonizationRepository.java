package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Colonization;
import org.springframework.data.repository.CrudRepository;

public interface ColonizationRepository extends CrudRepository<Colonization, Integer>, CustomColonizationRepository {
}
