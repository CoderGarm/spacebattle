package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface BattleReportRepository extends JpaRepository<BattleReport, Integer>, CustomBattleReportRepository {

    @Query("SELECT CASE WHEN (COUNT(r) > 0) THEN TRUE ELSE FALSE END FROM BattleReport r LEFT JOIN Owner u ON (u.id = :idUser) WHERE u MEMBER OF r.participatingUsers AND r.tick.id >= :since")
    boolean hasNewReportsSince(int idUser, final int since);

    @Nullable
    @Query("SELECT DISTINCT r FROM BattleReport r WHERE :user MEMBER OF r.participatingUsers")
    List<BattleReport> findAllForUser(@Nonnull final User user);
}
