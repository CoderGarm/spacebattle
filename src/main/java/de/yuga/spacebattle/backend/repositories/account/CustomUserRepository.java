package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Colonization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CustomUserRepository {

    @Nonnull
    List<User> findAllUsers();

    @Nullable
    User login(@Nonnull String username, @Nonnull String password);

    /**
     * Check if a user with this user name <b>OR</b> this email address already exists.
     *
     * @param username a user name
     * @param email an email address
     * @return {@link User}
     */
    @Nullable
    User findByUsernameAndEmail(@Nonnull String username, @Nonnull String email);

    @Nonnull
    User findWithResearchesAndJobs(@Nonnull User user);

    @Nonnull
    User findWithKnownStarSystems(@Nonnull User user);

    @Nonnull
    Map<Research, Integer> getResearchesForUser(@Nonnull User user);

    @Nonnull
    Set<StarSystem> getKnownStarSystems(@Nonnull User user);

    @Nonnull
    Set<Colonization> getColonizations(@Nonnull User user);
}
