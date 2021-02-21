package de.yuga.spacebattle.gui.vaadin.turn;


import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.entities.turn.Tick;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TickDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<Tick> binder = new Binder<>(Tick.class);

    public TickDisplay() {
        final Label tickTextLabel = new Label();
        final ReadOnlyHasValue<String> tickText = new ReadOnlyHasValue<>(tickTextLabel::setText);
        binder.forField(tickText).bind(Tick::convertTickToText, null);
        add(tickTextLabel);
    }

    public void updateTick(@Nullable Tick tick) {
        binder.readBean(tick);
    }
}
