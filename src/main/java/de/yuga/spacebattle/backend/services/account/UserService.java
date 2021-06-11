package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.UserMessage;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.repositories.account.UserMessageRepository;
import de.yuga.spacebattle.backend.repositories.account.UserRepository;
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
    private final UserMessageRepository userMessageRepository;

    @Nullable
    private User login;

    @Autowired
    public UserService(@Nonnull final UserRepository userRepository,
                       @Nonnull final UserMessageRepository userMessageRepository) {
        Preconditions.checkNotNull(userRepository, "userRepository shouldn't be null!");
        Preconditions.checkNotNull(userMessageRepository, "userMessageRepository should not be null");

        this.userRepository = userRepository;
        this.userMessageRepository = userMessageRepository;
    }

    /**
     * Is falsely annotated as non-null but if the user is logged in then the user is logged in.
     *
     * @return the logged in user or null
     */
    @Nonnull
    public User getLoggedInUser() {
        return login;
    }

    /**
     * Refreshes the logged in user;
     *
     * @return the re-fetched logged in user
     */
    @Nonnull
    public User refresh() {
        if (login == null) {
            throw new NotifySBUserException("you should be logged in, think about that.");
        }
        return find(login).orElse(login);
    }

    public void setLogin(@Nullable final User login) {
        this.login = login;
    }

    @Nullable
    public User login(@Nonnull final String username, @Nonnull final String password) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(password, "password shouldn't be null!");

        return userRepository.login(username, password);
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
    public Optional<User> find(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return userRepository.findById(user.getId());
    }

    @Nonnull
    public User findWithResearches(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return Objects.requireNonNull(userRepository.findWithResearchesAndJobs(user));
    }

    public Map<Research, Integer> getResearchesForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return userRepository.getResearchesForUser(user);
    }

    @Nonnull
    public Set<StarSystem> getKnownStarSystems(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return userRepository.getKnownStarSystems(user);
    }

    @Nonnull
    public User getWithKnownStarSystems(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return userRepository.findWithKnownStarSystems(user);
    }

    @Nonnull
    public User save(@Nonnull final User entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return userRepository.save(entity);
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    public User addUnlockedResearch(@Nonnull final User entity, @Nonnull final Research... researches) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");
        Preconditions.checkNotNull(researches, "researches shouldn't be null!");

        final User user = findWithResearches(entity);
        for (Research research : researches) {
            Integer level = user.getResearches().get(research);
            if (level == null) {
                level = 1;
            } else {
                level++;
            }
            user.getResearches().put(research, level);
        }
        return this.save(user);
    }


    @Nullable
    public User findByUsernameAndEmail(@Nonnull final String username, @Nonnull final String email) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(email, "email shouldn't be null!");

        return userRepository.findByUsernameAndEmail(username, email);
    }

    @Nonnull
    public User createUser(@Nonnull final String username,
                           @Nonnull final String password,
                           @Nonnull final String email) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(password, "password shouldn't be null!");
        Preconditions.checkNotNull(email, "email shouldn't be null!");

        return this.save(new User(username, password, email));
    }

    public Set<Colonization> getColonizations(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return userRepository.getColonizations(user);
    }

    /**
     * Returns all sent messages of the current logged in user.
     *
     * @return empty if no user is logged in or no messages were sent.
     */
    public List<UserMessage> getAllSentMessages() {
        return userMessageRepository.findByUserSender(login);
    }

    /**
     * Returns all received messages of the current logged in user.
     *
     * @return empty if no user is logged in or no messages were received.
     */
    public List<UserMessage> getAllReceivedMessages() {
        return userMessageRepository.findByUserReceiver(login);
    }

    /**
     * Saves a {@link UserMessage}.
     *
     * @param msg the msg
     */
    public void saveUserMessage(@Nonnull UserMessage msg) {
        Preconditions.checkNotNull(msg, "UserMessage should not be null");
        Preconditions.checkNotNull(msg.getUserReceiver(), "userReceiver should not be null");
        Preconditions.checkNotNull(msg.getSubject(), "subject should not be null");
        msg.setSentAt();
        userMessageRepository.save(msg);
    }

    /**
     * Updates a {@link UserMessage}.
     *
     * @param msg the msg
     */
    public void updateUserMessage(@Nonnull UserMessage msg) {
        Preconditions.checkNotNull(msg, "UserMessage should not be null");
        userMessageRepository.save(msg);
    }
}
