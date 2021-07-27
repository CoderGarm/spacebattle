package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomBattleReportRepository {

    @Nonnull
    List<BattleReport> findAllWithUser(final int idUser);

    BattleReport findLatestWithUser(final int idUser);
}
