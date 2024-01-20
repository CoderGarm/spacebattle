package de.yuga.spacebattle.backend.services.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.entities.turn.battle.SharedBattleReport;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.repositories.turn.battle.BattleReportRepository;
import de.yuga.spacebattle.backend.repositories.turn.battle.SharedBattleReportRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.account.AllianceService;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReportStatistics;
import de.yuga.spacebattle.rest.dto.turn.battle.ChangeSharedBattleReport;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class BattleReportService {

    @Nonnull
    private final BattleReportRepository battleReportRepository;

    @Nonnull
    private final SharedBattleReportRepository sharedBattleReportRepository;
    private final AllianceService allianceService;
    private final UserService userService;

    public BattleReportService(@Nonnull final BattleReportRepository battleReportRepository,
                               @Nonnull final SharedBattleReportRepository sharedBattleReportRepository, final AllianceService allianceService, final UserService userService) {
        this.battleReportRepository = Preconditions.checkNotNull(battleReportRepository, "fightingReportRepository shouldn't be null!");
        this.sharedBattleReportRepository = Preconditions.checkNotNull(sharedBattleReportRepository, "sharedBattleReportRepository must not be empty");
        this.allianceService = allianceService;
        this.userService = userService;
    }

    @Nonnull
    public List<BattleReport> findAll() {
        final Iterable<BattleReport> fightingReports = battleReportRepository.findAll();
        return StreamSupport.stream(fightingReports.spliterator(), false).collect(Collectors.toList());
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

    @Nonnull
    public SharedBattleReport save(final SharedBattleReport sharedBattleReport) {
        Preconditions.checkNotNull(sharedBattleReport, "sharedBattleReport must not be empty");

        return sharedBattleReportRepository.save(sharedBattleReport);
    }

    public void delete(@Nonnull final BattleReport entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        battleReportRepository.delete(entity);
    }

    @Nonnull
    public List<BattleReport> saveAll(@Nonnull final Collection<BattleReport> reports) {
        Preconditions.checkNotNull(reports, "reports shouldn't be null!");

        final Iterable<BattleReport> saveAll = battleReportRepository.saveAll(reports);
        return StreamSupport.stream(saveAll.spliterator(), false).collect(Collectors.toList());
    }

    @Nonnull
    public List<SharedBattleReport> saveAll(@Nonnull final List<SharedBattleReport> sharedBattleReports) {
        Preconditions.checkNotNull(sharedBattleReports, "sharedBattleReports must not be empty");

        final Iterable<SharedBattleReport> saveAll = sharedBattleReportRepository.saveAll(sharedBattleReports);
        return StreamSupport.stream(saveAll.spliterator(), false).collect(Collectors.toList());
    }

    public int countAllWithUser(final int idUser) {
        return sharedBattleReportRepository.countAllWithUser(idUser);
    }

    @Nullable
    public BattleReport findByIdWithAllData(final int idBattleReport) {
        return battleReportRepository.findByIdWithAllData(idBattleReport);
    }

    @Nonnull
    public Collection<BattleReportStatistics> findAllReportsBasicInformationForUser(final int idUser) {
        return Objects.requireNonNullElse(sharedBattleReportRepository.findAllReportsBasicInformationForUser(idUser), new ArrayList<>());
    }

    public boolean hasNewReportsSince(final int idUser, final int since) {
        return sharedBattleReportRepository.hasNewReportsSince(idUser, since);
    }

    @Nonnull
    public List<BattleReport> forDeletionFindAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        return Objects.requireNonNullElse(sharedBattleReportRepository.findAllForUser(user), new ArrayList<>());
    }

    @Nonnull
    public Set<BattleReport> findAllBetweenTick(final int fromTick, final int toTick) {
        return Objects.requireNonNullElse(battleReportRepository.findAllBetweenTick(fromTick, toTick), new HashSet<>());
    }

    @Nonnull
    public List<SharedBattleReport> findByReports(@Nonnull final List<BattleReport> battleReports) {
        Preconditions.checkNotNull(battleReports, "battleReports must not be empty");

        return Objects.requireNonNullElse(sharedBattleReportRepository.findByReports(battleReports), new ArrayList<>());
    }

    @Nullable
    public de.yuga.spacebattle.rest.dto.turn.battle.SharedBattleReport findSharedReport(final int idBattleReport) {
        return sharedBattleReportRepository.findByIdBattleReport(idBattleReport);
    }

    public void changeReportSharings(@Nonnull final ChangeSharedBattleReport change) {
        Preconditions.checkNotNull(change, "change must not be empty");

        final SharedBattleReport sharedBattleReport = sharedBattleReportRepository.findWithoutReportByIdBattleReport(change.getIdBattleReport());
        if (sharedBattleReport == null) {
            return;
        }

        setChangeWithEveryone(change, sharedBattleReport);
        setChangeWithAlliances(change, sharedBattleReport);
        setChangeWithUsers(change, sharedBattleReport);

        sharedBattleReportRepository.save(sharedBattleReport);
    }

    private void setChangeWithUsers(@Nonnull final ChangeSharedBattleReport change,
                                    @Nonnull final SharedBattleReport sharedBattleReport) {
        Preconditions.checkNotNull(change, "change must not be empty");
        Preconditions.checkNotNull(sharedBattleReport, "sharedBattleReport must not be empty");

        final ECalculationType calculationType = change.getCalculationType();
        final Integer sharedWithUser = change.getSharedWithUser();
        if (sharedWithUser != null) {
            final User user = userService.find(sharedWithUser);
            switch (calculationType) {
                case ADD:
                    sharedBattleReport.getSharedWithUsers().add(user);
                    break;
                case SUBTRACT:
                    sharedBattleReport.getSharedWithUsers().remove(user);
                    break;
            }
        }
    }

    private void setChangeWithAlliances(@Nonnull final ChangeSharedBattleReport change,
                                        @Nonnull final SharedBattleReport sharedBattleReport) {
        Preconditions.checkNotNull(change, "change must not be empty");
        Preconditions.checkNotNull(sharedBattleReport, "sharedBattleReport must not be empty");

        final ECalculationType calculationType = change.getCalculationType();
        final Integer sharedWithAlliance = change.getSharedWithAlliance();
        if (sharedWithAlliance != null) {
            final Alliance alliance = allianceService.find(sharedWithAlliance);
            switch (calculationType) {
                case ADD:
                    sharedBattleReport.getSharedWithAlliances().add(alliance);
                    break;
                case SUBTRACT:
                    sharedBattleReport.getSharedWithAlliances().remove(alliance);
                    break;
            }
        }
    }

    private void setChangeWithEveryone(@Nonnull final ChangeSharedBattleReport change,
                                       @Nonnull final SharedBattleReport sharedBattleReport) {
        Preconditions.checkNotNull(change, "change must not be empty");
        Preconditions.checkNotNull(sharedBattleReport, "sharedBattleReport must not be empty");

        final ECalculationType calculationType = change.getCalculationType();
        final Boolean shareWithEveryone = change.getShareWithEveryone();
        if (shareWithEveryone != null) {
            switch (calculationType) {
                case ADD:
                case SUBTRACT:
                    sharedBattleReport.getSharedWithAlliances().clear();
                    sharedBattleReport.getSharedWithUsers().clear();
                    break;
            }
            sharedBattleReport.setShareWithEveryone(shareWithEveryone);
        }
    }
}
