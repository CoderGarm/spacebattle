package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public interface CustomFleetRepository {

    @Nonnull
    List<Fleet> findAllFleets();

    @Nonnull
    Set<Fleet> findAllFleetsWithoutInterstellarMovement(final int idUser);

    @Nonnull
    List<Fleet> findAllFleetsWithoutMovement();

    @Nonnull
    List<Fleet> findAllFleetsWithMovement(final int idUser);

    @Nonnull
    List<Fleet> findAllFleetsWithInterstellarMovement();

    @Nonnull
    List<Fleet> findAllFleetsBy(User user);

    @Nonnull
    List<Fleet> findAllFleetsByStarSystemAndOwner(int idStarSystem, int idOwner);

    Fleet saveAndFlush(@Nonnull Fleet shipClass);

    /**
     * Fetches all fleets which are in orbit around the planet, which are moving from or to the planet.
     *
     * @param planet the planet
     * @return all found fleets in relation to the planet
     */
    @Nonnull
    Set<Fleet> findAllFleetsByPlanet(@Nonnull Planet planet);

    @Nonnull
    Set<Fleet> findAllDamagedFleetsByPlanetAndOwner(@Nonnull Planet planet);

    /**
     * Checks if a ship class in part of a fleet and therefore 'in use'.
     *
     * @param idShipClass the id of the class
     * @return <code>true</code> if the ship class is part of a fleet, <code>false</code> otherwise
     */
    boolean isShipClassInUse(final int idShipClass);

    /**
     * Returns all fleets sorted by the owner for every system in which
     *
     * @return all pending fleet clashes
     */
    @Nonnull
    List<FleetClash> findAllFleetClashes();
}
