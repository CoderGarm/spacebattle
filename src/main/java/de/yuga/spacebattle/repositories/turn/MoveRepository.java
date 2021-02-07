package de.yuga.spacebattle.repositories.turn;

import de.yuga.spacebattle.entities.turn.Move;
import org.springframework.data.repository.CrudRepository;

public interface MoveRepository extends CrudRepository<Move, Integer>, CustomMoveRepository {
}
