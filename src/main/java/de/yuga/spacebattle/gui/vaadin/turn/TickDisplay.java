package de.yuga.spacebattle.gui.vaadin.turn;


import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.backend.entities.turn.Tick;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class TickDisplay extends HorizontalLayout {

    @Nonnull
    public final static DateTimeFormatter tickFormatter = new DateTimeFormatterBuilder().appendPattern("yyyy MM dd").toFormatter();

    public TickDisplay(@Nullable final Tick tick) {

        String text1 = "Tick ";
        if (tick != null) {
            text1 += tick.getId() + " at " + tick.getTickStarts().format(tickFormatter);
        } else {
            text1 += "zero has not passed.";
        }

        Label tickL = new Label(text1);

        add(tickL);
    }
}
