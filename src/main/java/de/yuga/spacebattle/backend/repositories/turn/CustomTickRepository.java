package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Tick;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CustomTickRepository {

    @Nonnull
    List<Tick> findAllTicks();

    @Nullable
    Tick getLatest();
}
