package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReportStatistics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CustomBattleReportRepository {

    int countAllWithUser(final int idUser);

    @Nullable
    BattleReport findByIdWithAllData(final int idUser, final int idBattleReport);

    @Nonnull
    List<BattleReportStatistics> findReportBasicInformationByPaging(final int idUser, final int page, final int size);
}
