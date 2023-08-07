package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.services.caches.ColonizationCache;
import de.yuga.spacebattle.backend.services.caches.OperationalCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ColonizationTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(ColonizationTickRunner.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final ColonizationCache colonizationCache;

    @Nonnull
    private final OperationalCache operationalCache;

    @Nonnull
    private final OperationalService operationalService;

    @Nonnull
    private final FleetService fleetService;


    @Autowired
    public ColonizationTickRunner(@Nonnull final ColonizationService colonizationService,
                                  @Nonnull final PlanetService planetService,
                                  @Nonnull final ColonizationCache colonizationCache,
                                  @Nonnull final OperationalCache operationalCache,
                                  @Nonnull final OperationalService operationalService,
                                  @Nonnull final FleetService fleetService) {
        this.colonizationService = Preconditions.checkNotNull(colonizationService, "colonizationService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.colonizationCache = Preconditions.checkNotNull(colonizationCache, "colonizationCache must not be empty");
        this.operationalCache = Preconditions.checkNotNull(operationalCache, "operationalCache must not be empty");
        this.operationalService = Preconditions.checkNotNull(operationalService, "operationalService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Colonize planets");
        tickColonizations();
    }


    /**
     * Runs the tick for all colonizations.
     */
    private void tickColonizations() {
        Preconditions.checkNotNull(today, "today must not be empty");

        final Set<Colonization> colonizations = new HashSet<>(colonizationService.findAll());

        final Set<Colonization> planned = colonizations.stream().filter(Colonization::isPlanned).collect(Collectors.toSet());
        colonizations.removeAll(planned);

        final Map<User, Planet> mains = new HashMap<>();
        planned.forEach(colonization -> {
            final User user = colonization.getUser();
            final Planet main = mains.getOrDefault(user, planetService.findMainPlanet(user));
            mains.put(user, main);

            final ResourceDeposit costs = colonization.getCosts();
            costs.setSubType(EDepositType.COSTS);
            final PayingPossibleResult payingPossible = main.getResourceDeposit().isPayingPossible(costs);
            final PayingPossibleResult payingPossibleCrew = main.getResourceDeposit().isPayingPossible(costs.getCrewRequirement());
            if (payingPossible.isValid() && payingPossibleCrew.isValid()) {
                colonization.start();
                colonizations.add(colonization);
                main.getResourceDeposit().pay(costs);
                main.getResourceDeposit().updateCrew(costs.getCrewRequirement(), ECalculationType.SUBTRACT);
            }
        });
        planetService.saveAll(mains.values());

        for (final Colonization colonization : colonizations) {
            int doneAtZero = colonization.getDoneAtZero();
            doneAtZero--;
            colonization.setDoneAtZero(doneAtZero);

            if (doneAtZero < 1) {
                final Planet planet = colonizationService.colonizePlanet(colonization);
                operationalService.operateInoperationals(today, planet);
                colonizationService.delete(colonization);
                colonizationCache.add(today, planet);
            } else {
                colonizationService.save(colonization);
            }
        }
    }
}
