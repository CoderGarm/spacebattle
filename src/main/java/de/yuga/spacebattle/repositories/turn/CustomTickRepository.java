package de.yuga.spacebattle.repositories.turn;

import de.yuga.spacebattle.entities.turn.Tick;

import java.util.List;

public interface CustomTickRepository {

    List<Tick> findAllTicks();
}
