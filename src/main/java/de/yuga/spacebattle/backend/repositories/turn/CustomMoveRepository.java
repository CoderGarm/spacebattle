package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Move;

import java.util.List;

public interface CustomMoveRepository {

    List<Move> findAllMoves();
}
