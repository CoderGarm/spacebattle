package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Move;
import org.springframework.data.repository.CrudRepository;

public interface MoveRepository extends CrudRepository<Move, Integer>, CustomMoveRepository {
}
