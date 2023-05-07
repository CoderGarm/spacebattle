package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.AbstractId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FleetRepository extends CrudRepository<Fleet, Integer>, CustomFleetRepository {

    /**
     * Strange necessity while no valid option to delete {@link Fleet} has left.
     *
     * @param ids the IDs of the fleets to delete
     */
    @Modifying
    @Query("DELETE FROM Fleet f WHERE f.id in ?1")
    @Transactional
    void deleteAll(Set<Integer> ids);

    /**
     * compare {@link #deleteAll(Set)}
     */
    @Modifying
    @Query("DELETE FROM Fleet f WHERE f.id = ?1")
    @Transactional
    void delete(Integer id);

    @Nullable
    @Query("SELECT f FROM Fleet f WHERE f.isDeleted = false")
    List<Fleet> findAllAliveFleets();

    @Nullable
    @Query("SELECT DISTINCT f FROM Fleet f WHERE f.isDeleted = false AND f.orbit.system.id IN (:systemIds)")
    List<Fleet> findAllAliveFleetsInSystems(@Param("systemIds") @Nonnull final Collection<Integer> systemIds);

    @Nullable
    @Query("SELECT new de.yuga.spacebattle.rest.dto.AbstractId(f.id, f.name) FROM Fleet f WHERE f.isDeleted = false AND f.owner.id = :idUser")
    List<AbstractId> findAllAliveFleetsBy(@Param("idUser") final int idUser);
}
