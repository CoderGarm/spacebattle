package de.yuga.spacebattle.rest.dto.turn;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoveList extends ArrayList<Move> {

    public MoveList(@Nonnull final List<de.yuga.spacebattle.backend.entities.turn.Move> plannedMoves) {
        Preconditions.checkNotNull(plannedMoves, "plannedMoves shouldn't be null!");

        addAll(plannedMoves.stream().map(Move::new).collect(Collectors.toList()));
    }
}
