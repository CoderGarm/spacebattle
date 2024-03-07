package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nullable;
import java.util.Set;

public interface BattleReportRepository extends JpaRepository<BattleReport, Integer> {

    @Nullable
    @Query("SELECT DISTINCT r FROM BattleReport r WHERE r.tick.id BETWEEN :fromTick AND :toTick ")
    Set<BattleReport> findAllBetweenTick(int fromTick, int toTick);

    @Nullable
    @Query("SELECT r FROM BattleReport r WHERE r.id = :idBattleReport")
    BattleReport findByIdWithAllData(final int idBattleReport);

}
