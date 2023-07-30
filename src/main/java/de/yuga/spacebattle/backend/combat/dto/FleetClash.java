package de.yuga.spacebattle.backend.combat.dto;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A dto to transfer the participating fleets and the orbit where the clash is going on.
 */
public class FleetClash {

    /**
     * The place to be.
     */
    @Nonnull
    private final FleetOrbit orbit;

    /**
     * The protagonists - and the antagonists.
     */
    @Nonnull
    private final List<Fleet> participatingFleets = new ArrayList<>();

    public FleetClash(@Nonnull final Map.Entry<FleetOrbit, List<Fleet>> entry) {
        Preconditions.checkNotNull(entry, "entry shouldn't be null!");

        this.orbit = entry.getKey();
        final Map<Owner, List<Fleet>> byOwner = entry.getValue().stream()
                .collect(Collectors.groupingBy(Fleet::getOwner,
                        Collectors.mapping(Function.identity(), Collectors.toList())));
        byOwner.forEach((owner, fleets) -> participatingFleets.add(fleets.get(0)));
    }

    @Nonnull
    public FleetOrbit getOrbit() {
        return orbit;
    }

    @Nonnull
    public List<Fleet> getParticipatingFleets() {
        return participatingFleets;
    }
}
