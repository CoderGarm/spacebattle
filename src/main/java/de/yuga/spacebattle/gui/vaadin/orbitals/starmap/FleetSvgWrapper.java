package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.elements.Text;

import javax.annotation.Nonnull;

public class FleetSvgWrapper {

    @Nonnull
    private final FleetShark fleetShark;

    @Nonnull
    private final Text fleetText;

    public FleetSvgWrapper(@Nonnull final FleetShark fleetShark, @Nonnull final Text fleetText) {
        Preconditions.checkNotNull(fleetShark, "fleetShark shouldn't be null!");
        Preconditions.checkNotNull(fleetText, "fleetText shouldn't be null!");

        this.fleetShark = fleetShark;
        this.fleetText = fleetText;
    }

    @Nonnull
    public FleetShark getFleetShark() {
        return fleetShark;
    }

    @Nonnull
    public Text getFleetText() {
        return fleetText;
    }
}
