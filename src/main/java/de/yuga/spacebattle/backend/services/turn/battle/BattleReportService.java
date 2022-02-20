package de.yuga.spacebattle.backend.services.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.repositories.turn.battle.BattleReportRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class BattleReportService {

    @Nonnull
    private final BattleReportRepository battleReportRepository;

    public BattleReportService(@Nonnull final BattleReportRepository battleReportRepository) {
        Preconditions.checkNotNull(battleReportRepository, "fightingReportRepository shouldn't be null!");

        this.battleReportRepository = battleReportRepository;
    }

    @Nonnull
    public List<BattleReport> findAll() {
        final Iterable<BattleReport> fightingReports = battleReportRepository.findAll();
        return StreamSupport.stream(fightingReports.spliterator(), false).collect(Collectors.toList());
    }

    @Nullable
    public BattleReport find(@Nonnull final Integer idMove) {
        Preconditions.checkNotNull(idMove, "idMove shouldn't be null!");
        return battleReportRepository.findById(idMove).orElse(null);
    }

    public BattleReport save(@Nonnull final BattleReport entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return battleReportRepository.save(entity);
    }

    public void delete(@Nonnull final BattleReport entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        battleReportRepository.delete(entity);
    }

    public List<BattleReport> saveAll(@Nonnull final List<BattleReport> reports) {
        Preconditions.checkNotNull(reports, "reports shouldn't be null!");

        final Iterable<BattleReport> fightingReports = battleReportRepository.saveAll(reports);
        return StreamSupport.stream(fightingReports.spliterator(), false).collect(Collectors.toList());
    }

    public List<BattleReport> findAllWithUser(final int idUser) {
        return battleReportRepository.findAllWithUser(idUser);
    }

    public BattleReport findLatestWithUser(final int idUser) {
        return battleReportRepository.findLatestWithUser(idUser);
    }

    public List<BattleReport> findReportsWithUserWithPaging(final int idUser, final int page, final int size) {
        return battleReportRepository.findReportsWithUserWithPaging(idUser, page, size);
    }
}
