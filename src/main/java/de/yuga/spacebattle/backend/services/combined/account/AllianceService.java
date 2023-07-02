package de.yuga.spacebattle.backend.services.combined.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.account.AllianceApplication;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EApplicationState;
import de.yuga.spacebattle.backend.enums.EGameUserRole;
import de.yuga.spacebattle.backend.repositories.combined.account.AllianceApplicationRepository;
import de.yuga.spacebattle.backend.repositories.combined.account.AllianceRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AllianceService {

    @Nonnull
    private final AllianceRepository allianceRepository;

    @Nonnull
    private final AllianceApplicationRepository applicationRepository;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final TickTimeService tickService;

    @Autowired

    public AllianceService(@Nonnull final AllianceRepository allianceRepository,
                           @Nonnull final AllianceApplicationRepository applicationRepository,
                           @Nonnull final UserService userService,
                           @Nonnull final TickTimeService tickService) {
        this.allianceRepository = Preconditions.checkNotNull(allianceRepository, "allianceRepository must not be empty");
        this.applicationRepository = Preconditions.checkNotNull(applicationRepository, "applicationRepository must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
        this.tickService = Preconditions.checkNotNull(tickService, "tickService must not be empty");
    }

    @Nonnull
    public final Alliance save(@Nonnull final Alliance entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return allianceRepository.save(entity);
    }

    @Nonnull
    public List<Alliance> findAll() {
        return allianceRepository.findAllAlliances();
    }

    @Nullable
    public Alliance find(@Nonnull final Integer idAlliance) {
        Preconditions.checkNotNull(idAlliance, "idAlliance shouldn't be null!");
        return allianceRepository.findById(idAlliance).orElse(null);
    }

    @Nonnull
    public Alliance createAlliance(@Nonnull final String name, @Nonnull final String code, @Nonnull final User founder) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(code, "code shouldn't be null!");
        Preconditions.checkNotNull(founder, "founder shouldn't be null!");

        final Alliance alliance = new Alliance(name, code, founder);
        final Alliance saved = allianceRepository.save(alliance);
        founder.setAlliance(saved);
        founder.addGameUserRoles(EGameUserRole.ALLIANCE_ADMIN);
        userService.save(founder);
        return saved;
    }

    public void delete(@Nonnull final Alliance entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        final List<User> admins = userService.findAllianceAdminByAlliance(entity, EGameUserRole.ALLIANCE_ADMIN);
        admins.forEach(admin -> admin.removeGameUserRoles(EGameUserRole.ALLIANCE_ADMIN));
        userService.saveAll(admins);
        allianceRepository.delete(entity);
    }

    /**
     * Checks if the alliance name is already in use.
     *
     * @param username the username to check
     * @return <code>true</code> if the username is blocked, <code>false</code> otherwise
     */
    public boolean existsAllianceName(@Nonnull final String username) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");

        return allianceRepository.existsAllianceName(username);
    }

    /**
     * Checks if the alliance code is already in use.
     *
     * @param email the eMail to check
     * @return <code>true</code> if the eMail address is blocked, <code>false</code> otherwise
     */
    public boolean existsAllianceCode(@Nonnull final String email) {
        Preconditions.checkNotNull(email, "email shouldn't be null!");

        return allianceRepository.existsAllianceCode(email);
    }

    @Nonnull
    public List<Alliance> findAllWithMembers() {
        return Objects.requireNonNullElse(allianceRepository.findAllWithMembers(), new ArrayList<>());
    }

    @Nullable
    public Alliance findWithMembers(final int idAlliance) {
        return allianceRepository.findWithMembers(idAlliance);
    }

    @Nonnull
    public Set<Alliance> getOpenApplications(final int idUser) {
        return Objects.requireNonNullElse(applicationRepository.hasOpenApplication(idUser, EApplicationState.OPEN), Set.of());
    }

    public void denyApplication(final int idAdmin, final int idApplicant) {
        setApplicationState(idAdmin, idApplicant, EApplicationState.DENIED);
    }

    private void setApplicationState(final int idAdmin, final int idApplicant, @Nonnull final EApplicationState state) {
        Preconditions.checkNotNull(state, "state must not be empty");

        final Tick today = tickService.getToday();
        final AllianceApplication application = applicationRepository.findByIdAllianceAndIdUserAndState(idAdmin, idApplicant, EApplicationState.OPEN);
        if (application != null) {
            application.setApplicationState(state);
            application.setDecidedAt(today);
            applicationRepository.save(application);
        }
    }

    public void grantApplication(final int idAdmin, final int idApplicant) {
        final User user = userService.find(idAdmin);
        PreconditionWebHelper.checkNotNull(user, "user shouldn't be null!");

        assert user.getAlliance() != null : "An alliance admin should have an alliance.";
        final Alliance alliance = user.getAlliance();
        final User applicant = userService.find(idApplicant);
        Preconditions.checkNotNull(applicant, "applicant shouldn't be null!");

        applicant.setAlliance(alliance);
        userService.save(applicant);
        setApplicationState(idAdmin, idApplicant, EApplicationState.ACCEPTED);

        final Tick today = tickService.getToday();
        applicationRepository.closeAllOpenApplications(idApplicant, today, EApplicationState.OPEN, EApplicationState.DENIED);
    }

    public Set<User> findOpenApplicationsForAlliance(final int idAdmin) {
        return Objects.requireNonNullElse(applicationRepository.findByAllianceOfAdmin(idAdmin, EApplicationState.OPEN), Set.of());
    }

    public void applyForMembership(final int idApplicant, final int idAlliance) {
        final User applicant = userService.find(idApplicant);
        Preconditions.checkNotNull(applicant, "applicant shouldn't be null!");

        final Alliance alliance = find(idAlliance);
        Preconditions.checkNotNull(alliance, "alliance must not be empty");
        final Tick today = tickService.getToday();
        applicationRepository.save(new AllianceApplication(alliance, applicant, today));
    }

    public void withdrawApplication(final int idApplicant, final int idAlliance) {
        final Tick today = tickService.getToday();
        final AllianceApplication application = applicationRepository.findByIdAllianceAndIdUserAndState(idAlliance, idApplicant, EApplicationState.OPEN);
        if (application != null) {
            application.setApplicationState(EApplicationState.WITHDRAWN);
            application.setDecidedAt(today);
            applicationRepository.save(application);
        }
    }

    public void leave(final int idMember) {
        final User user = userService.find(idMember);
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        final Alliance alliance = user.getAlliance();
        Preconditions.checkNotNull(alliance, "alliance must not be empty");
        final Tick today = tickService.getToday();
        final AllianceApplication application = new AllianceApplication(alliance, user, today);
        application.setApplicationState(EApplicationState.TERMINATED);
        application.setDecidedAt(today);
        applicationRepository.save(application);

        user.setAlliance(null);
        userService.save(user);
    }
}
