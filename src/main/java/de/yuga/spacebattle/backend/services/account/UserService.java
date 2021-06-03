package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.repositories.account.UserRepository;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.gui.vaadin.misc.details.EResourceAmountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Nonnull
    private final UserRepository userRepository;

    @Nonnull
    private final ResearchService researchService;

    @Nullable
    private User login;

    @Autowired
    public UserService(@Nonnull final UserRepository userRepository,
                       @Nonnull final ResearchService researchService) {
        Preconditions.checkNotNull(userRepository, "userRepository shouldn't be null!");
        Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");

        this.userRepository = userRepository;
        this.researchService = researchService;
    }

    @Nullable
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
    public User save(@Nonnull final User entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return userRepository.save(entity);
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    public User addUnlockedResearch(@Nonnull final User entity, @Nonnull final Research research) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        final User user = find(entity).orElse(null);
        Research research1 = researchService.find(research.getId());
        if (user == null || research1 == null) {
            throw new NotifySBUserException("Funny idea...");
        }
        user.addResearch(research1);
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

    /**
     * Adds a star system to the user's known systems.
     *
     * @param user       the user wh should know the new system
     * @param starSystem the star system
     */
    @Transactional(rollbackFor = Exception.class)
    public void addToKnownSystems(@Nonnull final User user, @Nonnull final StarSystem starSystem) {
        final EResourceAmountDTO costs = ColonizationCostCalculator.calculateInformationCost(starSystem);
        final Planet mainPlanet = user.getMainPlanet();
        final ResourceDeposit resourceDeposit = mainPlanet.getResourceDeposit();
        // the costs must be validated by the instance before
        resourceDeposit.updateResource(costs.getResourceType(), costs.getAmount());
        user.addKnownStarSystems(starSystem);
        save(user);
    }
}
