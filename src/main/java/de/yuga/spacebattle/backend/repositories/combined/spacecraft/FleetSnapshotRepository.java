package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nullable;
import java.util.List;

public interface FleetSnapshotRepository extends JpaRepository<FleetSnapshot, Integer> {

    @Nullable
    @Query("SELECT f FROM FleetSnapshot f WHERE f.owner.id = :idUser")
    List<FleetSnapshot> forDeletionFindAllFleetsByUser(final int idUser);
}
