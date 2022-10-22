package de.yuga.spacebattle.backend.repositories.turn.battle.combat;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface WarshipHealthStateRepository extends CrudRepository<WarshipHealthState, Integer> {

    @Nullable
    @Query("SELECT w FROM WarshipHealthState w WHERE w.warShip IN (:warShips)")
    List<WarshipHealthState> findByWarships(@Param("warShips") @Nonnull List<WarShip> warShips);
}
