package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.springframework.data.repository.CrudRepository;

public interface TickRepository extends CrudRepository<Tick, Integer>, CustomTickRepository {
}
