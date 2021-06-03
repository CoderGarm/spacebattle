package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;

import javax.annotation.Nonnull;

/**
 * A dto to transport a single star system and a single planet.
 */
public class ColonizationTransportStarSystemDTO extends ColonizePlanetSelectionDTO {

    @Nonnull
    private final StarSystem starSystem;

    public ColonizationTransportStarSystemDTO(@Nonnull final StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        this.starSystem = starSystem;
    }

    @Nonnull
    public StarSystem getStarSystem() {
        return starSystem;
    }


}
