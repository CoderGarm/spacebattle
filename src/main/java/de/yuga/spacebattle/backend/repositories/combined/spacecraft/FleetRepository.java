package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
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
}
