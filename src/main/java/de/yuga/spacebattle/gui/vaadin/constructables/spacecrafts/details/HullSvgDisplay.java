package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.vaadin.flow.component.svg.elements.Path;
import com.vaadin.flow.component.svg.elements.Polygon;

import javax.annotation.Nonnull;
import java.util.List;

public class HullSvgDisplay extends Polygon {

    public static final String HULL_OUTLINE_COLOR = "white";

    /**
     * Creates a new Polygon element with the given id and initial points
     *
     * @param id     the id for this element
     * @param points the initial points for this element
     */
    public HullSvgDisplay(@Nonnull final String id, @Nonnull final List<PolyCoordinatePair> points) {
        super(id, points);

        setFillColor("transparent");
        setStroke(HULL_OUTLINE_COLOR, 2, Path.LINE_CAP.SQUARE, Path.LINE_JOIN.ARCS);
    }
}
