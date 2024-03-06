package de.yuga.spacebattle.backend.services.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.battle.SharedBattleReport;
import de.yuga.spacebattle.backend.repositories.turn.battle.SharedBattleReportRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SharedBattleReportService {

    @Nonnull
    private final SharedBattleReportRepository sharedBattleReportRepository;

    public SharedBattleReportService(@Nonnull final SharedBattleReportRepository sharedBattleReportRepository) {
        Preconditions.checkNotNull(sharedBattleReportRepository, "sharedBattleReportRepository shouldn't be null!");

        this.sharedBattleReportRepository = sharedBattleReportRepository;
    }

    @Nonnull
    public SharedBattleReport save(@Nonnull final SharedBattleReport entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return sharedBattleReportRepository.save(entity);
    }

    public void delete(@Nonnull final SharedBattleReport entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        sharedBattleReportRepository.delete(entity);
    }

    @Nonnull
    public List<SharedBattleReport> saveAll(@Nonnull final Collection<SharedBattleReport> reports) {
        Preconditions.checkNotNull(reports, "reports shouldn't be null!");

        final Iterable<SharedBattleReport> fightingReports = sharedBattleReportRepository.saveAll(reports);
        return StreamSupport.stream(fightingReports.spliterator(), false).collect(Collectors.toList());
    }

}
