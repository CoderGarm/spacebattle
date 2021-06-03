package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.repositories.turn.ColonizationRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.gui.vaadin.misc.details.EResourceAmountDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class ColonizationService {

    @Nonnull
    private final ColonizationRepository repository;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    /**
     * Holds the colonization which is designated to be displayed to the user.
     */
    @Nullable
    private Colonization colonizationToDisplay;

    public ColonizationService(@Nonnull final ColonizationRepository repository,
                               @Nonnull final UserService userService,
                               @Nonnull final PlanetService planetService) {
        Preconditions.checkNotNull(repository, "colonizationRepository shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");

        this.repository = repository;
        this.userService = userService;
        this.planetService = planetService;
    }

    @Nullable
    public Colonization getColonizationToDisplay() {
        return colonizationToDisplay;
    }

    public void setColonizationToDisplay(@Nullable final Colonization colonizationToDisplay) {
        this.colonizationToDisplay = colonizationToDisplay;
    }

    @Nonnull
    public List<Colonization> findAll() {
        return repository.findAll();
    }

    @Nonnull
    public List<Colonization> findAllForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return repository.findAllForUser(user);
    }

    @Nullable
    public Colonization find(@Nonnull final Integer idColonization) {
        Preconditions.checkNotNull(idColonization, "idColonization shouldn't be null!");

        return repository.findById(idColonization).orElse(null);
    }

    public Colonization save(@Nonnull final Colonization entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return repository.save(entity);
    }

    public void delete(@Nonnull final Colonization entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        repository.delete(entity);
    }

    /**
     * Starts a colonizing mission with the fixed duration of 10 ticks.
     * todo ticks based on distance between whatever planets
     *
     * @param user       the user who starts the colonization
     * @param toColonize the planet to colonize
     */
    @Transactional(rollbackFor = Exception.class)
    public void startColonizingPlanet(@Nonnull final User user, @Nonnull final Planet toColonize) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(toColonize, "toColonize shouldn't be null!");

        final EResourceAmountDTO costs = ColonizationCostCalculator.calculateColonizationCost(toColonize);
        final Planet mainPlanet = user.getMainPlanet();
        final ResourceDeposit resourceDeposit = mainPlanet.getResourceDeposit();
        // the costs must be validated by the instance before
        resourceDeposit.updateResource(costs.getResourceType(), costs.getAmount());
        final Colonization colonization = new Colonization(user, toColonize, 10);
        save(colonization);
        planetService.save(mainPlanet);
        userService.save(user);
    }
}
