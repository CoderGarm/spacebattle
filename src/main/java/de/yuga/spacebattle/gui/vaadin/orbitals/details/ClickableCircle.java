package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.vaadin.flow.component.svg.elements.Circle;

public class ClickableCircle extends Circle {
    /**
     * Creates a Circle element with the given Radius
     *
     * @param id     the unique id of this circle
     * @param radius the radius of this circle
     */
    public ClickableCircle(String id, double radius) {
        super(id, radius);
        getAttributes().put("clickable", true);
    }
}
