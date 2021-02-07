package de.yuga.spacebattle.repositories.turn;

import de.yuga.spacebattle.entities.turn.Tick;
import org.springframework.data.repository.CrudRepository;

public interface TickRepository extends CrudRepository<Tick, Integer>, CustomTickRepository {
}
