package de.yuga.spacebattle.gui.vaadin.turn;


import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.backend.entities.turn.Tick;

import javax.annotation.Nonnull;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class TickDisplay extends HorizontalLayout {

    @Nonnull
    public final static DateTimeFormatter tickFormatter = new DateTimeFormatterBuilder().appendPattern("yyyy MM dd").toFormatter();

    public TickDisplay(@Nonnull final Tick tick) {
        Preconditions.checkNotNull(tick, "tick shouldn't be null!");

        String text = tick.getTickStarts().format(tickFormatter);

        Label tickL = new Label("Tick " + tick.getId() + " at " + text);

        add(tickL);
    }
}
