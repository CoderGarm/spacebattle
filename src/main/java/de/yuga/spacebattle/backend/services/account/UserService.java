package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.EGameUserRolesConverter;
import de.yuga.spacebattle.backend.dto.account.UserSettings;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.UserSetting;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.EGameUserRole;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import de.yuga.spacebattle.backend.repositories.account.UserRepository;
import de.yuga.spacebattle.backend.repositories.account.UserSettingRepository;
import de.yuga.spacebattle.rest.config.security.WebUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Service
public class UserService {

    @Nonnull
    private final UserRepository userRepository;

    @Nonnull
    private final UserSettingRepository userSettingRepository;

    @Autowired
    public UserService(@Nonnull final UserRepository userRepository,
                       @Nonnull final UserSettingRepository userSettingRepository) {
        this.userRepository = Preconditions.checkNotNull(userRepository, "userRepository shouldn't be null!");
        this.userSettingRepository = Preconditions.checkNotNull(userSettingRepository, "userSettingRepository must not be empty");
    }

    @Nonnull
    public List<User> findAll() {
        return userRepository.findAllUsers();
    }

    @Nullable
    public User find(@Nonnull final Integer idUser) {
        Preconditions.checkNotNull(idUser, "idUser shouldn't be null!");

        return userRepository.findById(idUser).orElse(null);
    }

    @Nonnull
    public Set<EGameUserRole> findGameUserRoles(@Nullable final Integer idUser) {
        if (idUser == null) {
            return Set.of();
        }

        final String result = userRepository.findGameUserRoles(idUser);
        return new EGameUserRolesConverter().convertToEntityAttribute(result);
    }

    @Nonnull
    public Optional<User> find(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return userRepository.findById(user.getId());
    }


    @Nonnull
    public List<User> findAll(@Nonnull final Collection<Integer> idUsers) {
        Preconditions.checkNotNull(idUsers, "idUsers must not be empty");

        return Objects.requireNonNullElse(userRepository.findAllById(idUsers), new ArrayList<>());
    }

    @Nonnull
    public Set<StarSystem> getKnownStarSystems(final int idUser) {
        return userRepository.getKnownStarSystems(idUser);
    }

    @Nonnull
    public Set<StarSystem> getKnownStarSystems(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return userRepository.getKnownStarSystems(user.getId());
    }


    @Nullable
    public User findWithKnownStarSystems(final int idUser) {
        return userRepository.findWithKnownStarSystems(idUser);
    }

    @Nullable
    public User findWithKnownStarSystems(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return userRepository.findWithKnownStarSystems(user.getId());
    }

    @Nonnull
    public User save(@Nonnull final User entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return userRepository.save(entity);
    }

    @Nullable
    public User findByUsernameAndEmail(@Nonnull final String username, @Nonnull final String email) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(email, "email shouldn't be null!");

        return userRepository.findByUsernameAndEmail(username, email);
    }

    @Nonnull
    @Deprecated(since = "productive")
    public User createUser(@Nonnull final String username,
                           @Nonnull final String password,
                           @Nonnull final String email,
                           @Nonnull final EWebUserRole role,
                           @Nullable final EGameUserRole... gameUserRoles) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(password, "password shouldn't be null!");
        Preconditions.checkNotNull(email, "email shouldn't be null!");
        Preconditions.checkNotNull(role, "role shouldn't be null!");

        return this.save(new User(username, password, email, role, false, gameUserRoles));
    }

    @Nonnull
    public List<User> findLikeUsername(@Nullable final String username) {
        if (StringUtils.isEmpty(username)) {
            return new ArrayList<>();
        }
        return userRepository.findLikeUsername(username);
    }

    @Nullable
    public User findByUsernameOrEMail(@Nullable final String username, @Nullable final String eMail) {
        if (StringUtils.isBlank(username) && StringUtils.isBlank(eMail)) {
            return null;
        }
        return userRepository.findByUsernameOrEMail(username, eMail);
    }

    @Nonnull
    public Optional<WebUserDetails> findByUsername(@Nullable final String username) {
        if (StringUtils.isEmpty(username)) {
            return Optional.empty();
        }
        final User byUsername = userRepository.findByUsername(username);
        if (byUsername == null) {
            return Optional.empty();
        }
        return Optional.of(new WebUserDetails(byUsername));
    }

    public boolean delete(final int idUser) {
        Preconditions.checkArgument(idUser > 1, "idUser must be valid!");

        userRepository.deleteById(idUser);
        return userRepository.existsById(idUser);
    }

    /**
     * Checks if the username is already in use.
     *
     * @param username the username to check
     * @return <code>true</code> if the username is blocked, <code>false</code> otherwise
     */
    public boolean existsUsername(@Nonnull final String username) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");

        return userRepository.existsUsername(username);
    }

    /**
     * Checks if the eMail address is already in use.
     *
     * @param email the eMail to check
     * @return <code>true</code> if the eMail address is blocked, <code>false</code> otherwise
     */
    public boolean existsEMail(@Nonnull final String email) {
        Preconditions.checkNotNull(email, "email shouldn't be null!");

        return userRepository.existsEMail(email);
    }

    @Nonnull
    public List<User> findAllianceAdminByAlliance(@Nonnull final Alliance alliance, @Nonnull final EGameUserRole gameUserRole) {
        Preconditions.checkNotNull(alliance, "alliance shouldn't be null!");
        Preconditions.checkNotNull(gameUserRole, "gameUserRole shouldn't be null!");

        return Objects.requireNonNullElse(userRepository.findAllianceAdminByAlliance(alliance, gameUserRole), new ArrayList<>());
    }

    public void saveAll(@Nonnull final List<User> users) {
        Preconditions.checkNotNull(users, "users shouldn't be null!");

        userRepository.saveAll(users);
    }

    public void verifyEmail(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        user.getUserSetting().setEMailVerified(true);
        save(user);
    }

    public void changePassword(@Nonnull final User user, @Nonnull final String newPassword) {
        Preconditions.checkNotNull(user, "user must not be empty");
        Preconditions.checkNotNull(newPassword, "newPassword must not be empty");

        user.getUserSetting().setPassword(newPassword);
        save(user);
    }

    public void updateSettings(@Nonnull final UserSettings settings, final int idUser) {
        Preconditions.checkNotNull(settings, "settings must not be empty");

        final User user = find(idUser);
        Preconditions.checkNotNull(user, "user must not be empty");

        final UserSetting userSetting = user.getUserSetting();
        userSetting.setReceiveChangelogInfos(settings.isReceiveChangelogInfos());
        userSettingRepository.save(userSetting);
    }

    @Nonnull
    public UserSetting getSettings(final int idUser) {
        return Objects.requireNonNull(userSettingRepository.getForUser(idUser));
    }

    @Nonnull
    public Set<String> findReleaseRecipients() {
        return Objects.requireNonNullElse(userRepository.getEMailAddressesForReleaseRecipients(), new HashSet<>());
    }
}
