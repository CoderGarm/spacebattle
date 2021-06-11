package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageContainer;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetIconDTO;

import javax.annotation.Nonnull;

/**
 * Vaadin component to display the name, icon and a type of a planet.
 */
public class PlanetIconDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<PlanetIconDTO> binder = new Binder<>();

    public PlanetIconDisplay() {
        // todo planet icon by planet type - think about star types for map
        final ImageContainer imageContainer = new ImageContainer(EResolution.PX24);
        binder.forField(imageContainer).bind(w -> w, null);

        add(imageContainer);
    }

    /**
     * Updates the display with the given values.
     *
     * @param planet the input
     */
    public void update(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        binder.readBean(new PlanetIconDTO(planet));
    }
}
