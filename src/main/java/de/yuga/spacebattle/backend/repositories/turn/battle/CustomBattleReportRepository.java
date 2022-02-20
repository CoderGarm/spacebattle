package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CustomBattleReportRepository {

    @Nonnull
    List<BattleReport> findAllWithUser(final int idUser);

    @Nullable
    BattleReport findLatestWithUser(final int idUser);

    @Nonnull
    List<BattleReport> findReportsWithUserWithPaging(final int idUser, final int page, final int size);
}
