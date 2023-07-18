package de.yuga.spacebattle.backend.services.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.repositories.turn.battle.BattleReportRepository;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReportStatistics;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    public BattleReport find(@Nonnull final Integer idBattleReport) {
        Preconditions.checkNotNull(idBattleReport, "idBattleReport shouldn't be null!");

        return battleReportRepository.findById(idBattleReport).orElse(null);
    }

    @Nonnull
    public List<BattleReport> findAll(@Nonnull final Collection<Integer> idBattleReports) {
        Preconditions.checkNotNull(idBattleReports, "idBattleReports shouldn't be null!");

        return Objects.requireNonNullElse(battleReportRepository.findAllById(idBattleReports), new ArrayList<>());
    }

    @Nonnull
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

    public int countAllWithUser(final int idUser) {
        return battleReportRepository.countAllWithUser(idUser);
    }

    @Nullable
    public BattleReport findByIdWithAllData(final int idUser, final int idBattleReport) {
        return battleReportRepository.findByIdWithAllData(idUser, idBattleReport);
    }

    public Collection<BattleReportStatistics> findReportBasicInformationByPaging(final int idUser, final int page, final int size) {
        return battleReportRepository.findReportBasicInformationByPaging(idUser, page, size);
    }

    public boolean hasNewReportsSince(final int idUser, @Nonnull final Tick since) {
        Preconditions.checkNotNull(since, "since must not be empty");

        return battleReportRepository.hasNewReportsSince(idUser, since.getNo());
    }
}
