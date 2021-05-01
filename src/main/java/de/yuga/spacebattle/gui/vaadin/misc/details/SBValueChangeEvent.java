package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.vaadin.flow.component.HasValue;

/**
 * Just to fire an event without payload and don't have this inside a method.
 */
public class SBValueChangeEvent implements HasValue.ValueChangeEvent<SBValueChangeEvent> {

    @Override
    public HasValue<?, SBValueChangeEvent> getHasValue() {
        return null;
    }

    @Override
    public boolean isFromClient() {
        return false;
    }

    @Override
    public SBValueChangeEvent getOldValue() {
        return null;
    }

    @Override
    public SBValueChangeEvent getValue() {
        return null;
    }
}