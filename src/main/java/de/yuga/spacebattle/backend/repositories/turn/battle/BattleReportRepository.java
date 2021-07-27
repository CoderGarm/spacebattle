package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import org.springframework.data.repository.CrudRepository;

public interface BattleReportRepository extends CrudRepository<BattleReport, Integer>, CustomBattleReportRepository {
}
