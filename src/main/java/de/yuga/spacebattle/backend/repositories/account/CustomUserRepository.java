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
     * Check if a user with this username <b>OR</b> this email address already exists.
     *
     * @param username a user name
     * @param email    an email address
     * @return {@link User}
     */
    @Nullable
    User findByUsernameAndEmail(@Nonnull String username, @Nonnull String email);

    @Nonnull
    User findWithResearchesAndJobs(int idUser);

    @Nullable
    User findWithKnownStarSystems(int idUser);

    @Nonnull
    Map<Research, Integer> getResearchesForUser(int idUser);

    @Nullable
    User getWithResearches(int idUser);

    @Nonnull
    Set<StarSystem> getKnownStarSystems(int idUser);

    @Nonnull
    Set<Colonization> getColonizations(@Nonnull User user);

    /**
     * Checks is a user has the specific research already unlocked.
     *
     * @param user     the user
     * @param research the research
     * @return <code>true</code> if the user already has this research unlocked, <code>false</code> otherwise
     */
    boolean isResearchUnlocked(@Nonnull User user, @Nonnull Research research);

    /**
     * Fetches the current level of a given research by this user.
     *
     * @param user     the user
     * @param research the research
     * @return the current level
     */
    int getLevelForResearch(@Nonnull User user, @Nonnull Research research);

    /**
     * Searches for all users which has the param as left-search-string.
     *
     * @param username the username to search
     * @return the list of found users
     */
    @Nonnull
    List<User> findLikeUsername(@Nullable String username);

    /**
     * Checks if the username is already in use.
     *
     * @param username the username to check
     * @return <code>true</code> if the username is blocked, <code>false</code> otherwise
     */
    boolean existsUsername(@Nonnull String username);

    /**
     * Checks if the eMail address is already in use.
     *
     * @param eMail the eMail to check
     * @return <code>true</code> if the eMail address is blocked, <code>false</code> otherwise
     */
    boolean existsEMail(@Nonnull String eMail);

    @Nullable
    User findByUsername(String username);
}
