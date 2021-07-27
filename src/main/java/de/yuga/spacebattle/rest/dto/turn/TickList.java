package de.yuga.spacebattle.rest.dto.turn;


import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class TickList extends ArrayList<Tick> {

    public TickList(@Nonnull final Collection<de.yuga.spacebattle.backend.entities.turn.Tick> ticks) {
        Preconditions.checkNotNull(ticks, "ticks shouldn't be null!");

        addAll(ticks.stream().map(Tick::new).collect(Collectors.toList()));
    }
}
