package de.yuga.spacebattle.backend.services.combined.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.enums.EGameUserRole;
import de.yuga.spacebattle.backend.repositories.combined.account.AllianceRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AllianceService {

    @Nonnull
    private final AllianceRepository allianceRepository;

    @Nonnull
    private final UserService userService;

    @Autowired

    public AllianceService(@Nonnull final AllianceRepository allianceRepository,
                           @Nonnull final UserService userService) {
        Preconditions.checkNotNull(allianceRepository, "allianceR shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.allianceRepository = allianceRepository;
        this.userService = userService;
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
    public Alliance createAlliance(@Nonnull final String name, @Nonnull final String code, @Nonnull final User founder, @Nullable final User... applicants) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(code, "code shouldn't be null!");
        Preconditions.checkNotNull(founder, "founder shouldn't be null!");

        final Alliance alliance = new Alliance(name, code, founder);
        userService.save(founder);
        if (applicants != null) {
            alliance.getApplications().addAll(Arrays.stream(applicants).collect(Collectors.toSet()));
        }
        return allianceRepository.save(alliance);
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

    @Nullable
    public Alliance findWithApplications(final int idAlliance) {
        return allianceRepository.findWithApplications(idAlliance);
    }

    @Nullable
    public Alliance hasOpenApplication(final int idUser) {
        return allianceRepository.hasOpenApplication(idUser);
    }
}
