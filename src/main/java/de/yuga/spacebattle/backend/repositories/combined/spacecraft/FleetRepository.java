package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.rest.dto.AbstractId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FleetRepository extends JpaRepository<Fleet, Integer>, CustomFleetRepository {

    @Nullable
    @Query("SELECT DISTINCT f FROM Fleet f WHERE f.isDeleted = false AND f.orbit.system.id IN (:systemIds)")
    List<Fleet> findAllAliveFleetsInSystems(@Param("systemIds") @Nonnull final Collection<Integer> systemIds);

    @Nullable
    @Query("SELECT new de.yuga.spacebattle.rest.dto.AbstractId(f.id, f.name) FROM Fleet f WHERE f.isDeleted = false AND f.owner.id = :idUser")
    List<AbstractId> findAllAliveFleetsBy(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT f FROM Fleet f WHERE f.owner.id = :idUser AND f.isDeleted = false AND f.move IS NULL")
    List<Fleet> findAllFleetsWithoutMovementByUser(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT f.orbit.system FROM Fleet f WHERE f.isDeleted = false AND f.orbit IS NOT NULL AND f.orbit.system IS NOT NULL")
    Set<StarSystem> findSojourns();

    @Nullable
    @Query("SELECT f.move.destinationOrbit.system FROM Fleet f WHERE f.isDeleted = false AND f.move IS NOT NULL")
    Set<StarSystem> findMovementDestinations();

    @Nullable
    @Query("SELECT DISTINCT f.orbit.system.id FROM Fleet f WHERE f.owner.id = :idUser")
    Set<Integer> findAllSystemIDsWithFleetsForUser(final int idUser);

    @Nullable
    @Query("SELECT f FROM Fleet f WHERE f.owner.id = :idUser")
    List<Fleet> forDeletionFindAllFleetsByUser(final int idUser);
}
