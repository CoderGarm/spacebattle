package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.AbstractId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface FleetRepository extends JpaRepository<Fleet, Integer>, CustomFleetRepository {

    @Nullable
    @Query("SELECT f FROM Fleet f WHERE f.isDeleted = false")
    List<Fleet> findAllAliveFleets();

    @Nullable
    @Query("SELECT DISTINCT f FROM Fleet f WHERE f.isDeleted = false AND f.orbit.system.id IN (:systemIds)")
    List<Fleet> findAllAliveFleetsInSystems(@Param("systemIds") @Nonnull final Collection<Integer> systemIds);

    @Nullable
    @Query("SELECT new de.yuga.spacebattle.rest.dto.AbstractId(f.id, f.name) FROM Fleet f WHERE f.isDeleted = false AND f.owner.id = :idUser")
    List<AbstractId> findAllAliveFleetsBy(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT f FROM Fleet f WHERE f.owner.id = :idUser AND  f.isDeleted = false AND f.move IS NULL AND f.orbit IS NOT NULL AND f.orbit.orbit IS NOT NULL")
    List<Fleet> findAllFleetsWithoutMovementByUser(@Param("idUser") final int idUser);
}
