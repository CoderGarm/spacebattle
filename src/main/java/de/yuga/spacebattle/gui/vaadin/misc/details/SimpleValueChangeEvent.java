package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.vaadin.flow.component.HasValue;

/**
 * Just to fire an event without payload and don't have this inside a method.
 */
public class SimpleValueChangeEvent implements HasValue.ValueChangeEvent<SimpleValueChangeEvent> {

    @Override
    public HasValue<?, SimpleValueChangeEvent> getHasValue() {
        return null;
    }

    @Override
    public boolean isFromClient() {
        return false;
    }

    @Override
    public SimpleValueChangeEvent getOldValue() {
        return null;
    }

    @Override
    public SimpleValueChangeEvent getValue() {
        return null;
    }
}