package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.entities.turn.battle.SharedBattleReport;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReportStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface SharedBattleReportRepository extends JpaRepository<SharedBattleReport, Integer> {

    @Query("SELECT CASE WHEN (COUNT(s) > 0) THEN TRUE ELSE FALSE END FROM SharedBattleReport s LEFT JOIN Owner u ON (u.id = :idUser) WHERE u MEMBER OF s.participatingUsers AND s.battleReport.tick.id >= :since")
    boolean hasNewReportsSince(int idUser, final int since);

    @Nullable
    @Query("SELECT DISTINCT s.battleReport FROM SharedBattleReport s WHERE :user MEMBER OF s.participatingUsers")
    List<BattleReport> findAllForUser(@Nonnull final User user);

    @Nullable
    @Query("SELECT DISTINCT new de.yuga.spacebattle.rest.dto.turn.battle.BattleReportStatistics(r.id, r.uuid, r.lastRound, r.tick, r.venue) FROM SharedBattleReport s " +
            "LEFT JOIN BattleReport r ON (r.id = s.battleReport.id) " +
            "LEFT JOIN Owner o ON (o.id = :idUser) " +
            "LEFT JOIN User u ON (o.id = :idUser) " +
            "WHERE (o MEMBER OF s.participatingUsers) " +
            "OR s.shareWithEveryone = true " +
            "OR (o MEMBER OF s.sharedWithUsers) " +
            "OR (u.alliance MEMBER OF s.sharedWithAlliances) " +
            "ORDER BY r.id DESC ")
    List<BattleReportStatistics> findAllReportsBasicInformationForUser(final int idUser);

    @Query("SELECT COUNT(s) FROM SharedBattleReport s " +
            "LEFT JOIN Owner u ON (u.id = :idUser) " +
            "WHERE u MEMBER OF s.participatingUsers ")
    int countAllWithUser(final int idUser);

    @Nullable
    @Query("SELECT s FROM SharedBattleReport s WHERE s.battleReport in (:battleReports)")
    List<SharedBattleReport> findByReports(@Nonnull final List<BattleReport> battleReports);

    @Nullable
    @Query("SELECT new de.yuga.spacebattle.rest.dto.turn.battle.SharedBattleReport(s, s.battleReport.id) FROM SharedBattleReport s WHERE s.battleReport.id = :idBattleReport")
    de.yuga.spacebattle.rest.dto.turn.battle.SharedBattleReport findByIdBattleReport(final int idBattleReport);

    @Nullable
    @Query("SELECT s FROM SharedBattleReport s WHERE s.battleReport.id = :idBattleReport")
    SharedBattleReport findWithoutReportByIdBattleReport(final int idBattleReport);

    @Nullable
    @Query("SELECT s FROM SharedBattleReport s JOIN FETCH s.battleReport WHERE s.battleReport.id = :idBattleReport")
    SharedBattleReport findWithReportByIdBattleReport(final int idBattleReport);
}
