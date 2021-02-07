package de.yuga.spacebattle.repositories.turn;

import de.yuga.spacebattle.entities.turn.Move;

import java.util.List;

public interface CustomMoveRepository {

    List<Move> findAllMoves();
}
