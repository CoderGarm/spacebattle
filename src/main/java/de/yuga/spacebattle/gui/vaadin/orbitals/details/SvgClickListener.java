package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.SvgElement;
import com.vaadin.flow.component.svg.listeners.AbstractSvgEvent;
import elemental.json.JsonObject;

public interface SvgClickListener extends ComponentEventListener<SvgClickListener.ClickEvent> {

    /**
     * Represents a click event on a client-side svg element.
     */
    class ClickEvent extends AbstractSvgEvent {

        private SvgElement element;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source       the source component
         * @param fromClient   <code>true</code> if the event originated from the client
         * @param element      the element where this event happened
         * @param rawEventData the raw event data for extended use
         */
        public ClickEvent(Svg source, boolean fromClient, SvgElement element, JsonObject rawEventData) {
            super(source, fromClient, rawEventData);
            this.element = element;
        }

        /**
         * Returns the svg element where the drag end happened
         *
         * @return the {@link SvgElement} where this event happened
         */
        public SvgElement getElement() {
            return element;
        }
    }
}
