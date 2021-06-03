package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * A dto to transport the universe, a single star system and a single planet.
 */
public class ColonizationTransportUniverseDTO extends ColonizePlanetSelectionDTO {

    @Nonnull
    private final Set<StarSystem> starSystems;

    @Nullable
    private StarSystem selectedForBuyingDataStarSystem;

    public ColonizationTransportUniverseDTO(@Nonnull final Set<StarSystem> starSystems) {
        Preconditions.checkNotNull(starSystems, "starSystems shouldn't be null!");

        this.starSystems = starSystems;
    }

    @Nonnull
    public Set<StarSystem> getStarSystems() {
        return starSystems;
    }

    @Nullable
    public StarSystem getSelectedForBuyingDataStarSystem() {
        return selectedForBuyingDataStarSystem;
    }

    public void setSelectedForBuyingDataStarSystem(@Nullable final StarSystem selectedForBuyingDataStarSystem) {
        this.selectedForBuyingDataStarSystem = selectedForBuyingDataStarSystem;
    }
}
