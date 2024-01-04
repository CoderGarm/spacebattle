package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FleetSnapshotRepository extends JpaRepository<FleetSnapshot, Integer> {

}
