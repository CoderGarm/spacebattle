package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Colonization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * A dto to transport some colonizations.
 */
public class ColonizationForUserDTO {

    @Nonnull
    private final Set<Colonization> colonizations = new HashSet<>();

    @Nullable
    private StarSystem selectedForBuyingDataStarSystem;

    public ColonizationForUserDTO(@Nonnull final Set<Colonization> colonizations) {
        Preconditions.checkNotNull(colonizations, "colonizations shouldn't be null!");

        this.colonizations.addAll(colonizations);
    }

    @Nonnull
    public Set<Colonization> getColonizations() {
        return colonizations;
    }

}
