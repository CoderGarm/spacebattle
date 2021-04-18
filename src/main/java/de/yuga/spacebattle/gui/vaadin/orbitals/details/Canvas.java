package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.SvgElement;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * <b>ATTENTION:</b> currently not fully implemented! Do not use!
 * <p>
 * The display portion of the Vaadin Component Factory Svg component.
 * This is the component that is used to display one or more {@link SvgElement}
 * elements as well as listen to events on said elements.
 */
//@Tag("vcf-svg-canvas")
//@JsModule("./canvas/canvas.js")
//@JsModule("@svgdotjs/svg.js")
//@JsModule("d3")
public class Canvas extends Svg {

    private final static Logger LOGGER = LoggerFactory.getLogger(Canvas.class);

    private DomListenerRegistration clickDomRegistration;

    public Canvas() {
    }

    /**
     * Adds a click event listener to this {@link Canvas} component that will be triggered when a clickable component
     * has clicked on the client-side.
     *
     * @param listener the listener to add
     * @return the registration for managing the listener
     */
    public Registration addClickListener(SvgClickListener listener) {
        //In general we don't want the client-side to send events to the server unless we're actually listening to them.

        ensureDomClickEventListenerRegistered();
        return addListener(SvgClickListener.ClickEvent.class, listener);
    }

    /**
     * This method will add ClickEventListener if not already added.
     */
    protected void ensureDomClickEventListenerRegistered() {
        if (clickDomRegistration == null) {
            clickDomRegistration = getElement()
                    .addEventListener("clicked",
                            e -> onClickStartEvent(e.getEventData().getString("event.detail.handler.el.node.id"), e.getEventData())
                    );
        }
    }

    /**
     * Fires a drag start event if an svgElement is found in this Svg component based on the elementId provided.
     * Note that if an svgElement is not found, no events will be fired.
     *
     * @param elementId    the element id to look for.
     * @param rawEventData the raw event data for extended use
     */
    protected void onClickStartEvent(String elementId, JsonObject rawEventData) {
        Optional<SvgElement> element = findElementForId(elementId);
        if (!element.isPresent()) {
            LOGGER.warn("onClickStartEvent fired but no element found in internal list for id: "
                    + elementId + " suppressing event as mapping cannot be done.");
        }
        element.ifPresent(svgElement -> fireEvent(new SvgClickListener.ClickEvent(this, true, svgElement, rawEventData)));
    }
}
