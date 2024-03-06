package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.turn.battle.SharedBattleReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedBattleReportRepository extends JpaRepository<SharedBattleReport, Integer> {

}
