package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface BattleReportRepository extends CrudRepository<BattleReport, Integer>, CustomBattleReportRepository {

    @Query("SELECT CASE WHEN (COUNT(r) > 0) THEN TRUE ELSE FALSE END FROM BattleReport r LEFT JOIN User u ON (u.id = :idUser) WHERE u MEMBER OF r.participatingUsers AND r.tick.id >= :since")
    boolean hasNewReportsSince(int idUser, final int since);
}
