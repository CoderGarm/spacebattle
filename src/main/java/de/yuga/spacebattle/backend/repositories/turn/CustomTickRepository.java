package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Tick;

import java.util.List;

public interface CustomTickRepository {

    List<Tick> findAllTicks();
}
