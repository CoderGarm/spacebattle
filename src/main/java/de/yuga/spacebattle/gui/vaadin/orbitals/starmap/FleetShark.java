package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import com.vaadin.flow.component.svg.elements.Polygon;

import javax.annotation.Nonnull;
import java.util.List;

public class FleetShark extends Polygon {

    /**
     * Holds the id of the fleet which is represented by this.
     */
    private final int idFleet;

    /**
     * Creates a new Polygon element with the given id and initial points
     *
     * @param id     the id for this element
     * @param points the initial points for this element
     */
    public FleetShark(final int idFleet, @Nonnull final String id, @Nonnull final List<PolyCoordinatePair> points) {
        super(id, points);

        this.idFleet = idFleet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FleetShark)) return false;

        FleetShark that = (FleetShark) o;

        return idFleet == that.idFleet;
    }

    @Override
    public int hashCode() {
        return idFleet;
    }
}
