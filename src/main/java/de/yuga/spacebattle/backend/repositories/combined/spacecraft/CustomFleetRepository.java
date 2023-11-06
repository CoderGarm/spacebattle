package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public interface CustomFleetRepository {

    @Nonnull
    List<Fleet> findAllFleetsWithoutMovement();

    @Nonnull
    List<Fleet> findAllFleetsWithMovement(final int idUser);

    @Nonnull
    List<Fleet> findAllFleetsBy(final int idUser);

    @Nonnull
    List<Fleet> findAllFleetsByStarSystemAndOwner(int idStarSystem, int idOwner);

    /**
     * Fetches all fleets which are in orbit around the planet, which are moving from or to the planet.
     *
     * @param planet the planet
     * @return all found fleets in relation to the planet
     */
    @Nonnull
    Set<Fleet> findAllFleetsByPlanet(@Nonnull Planet planet);

    @Nonnull
    Set<Fleet> findAllAnchoredForPlanet(@Nonnull Planet planet);

    /**
     * Returns all fleets sorted by the owner for every system in which
     *
     * @return all pending fleet clashes
     */
    @Nonnull
    List<FleetClash> findAllFleetClashes();
}
