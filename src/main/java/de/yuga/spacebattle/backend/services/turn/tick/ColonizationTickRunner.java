package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.JobCostsCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.Operationable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.services.caches.ColonizationCache;
import de.yuga.spacebattle.backend.services.caches.OperationalCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
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
    private final ConstructionService constructionService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final FleetService fleetService;


    @Autowired
    public ColonizationTickRunner(@Nonnull final ColonizationService colonizationService,
                                  @Nonnull final PlanetService planetService,
                                  @Nonnull final ColonizationCache colonizationCache,
                                  @Nonnull final OperationalCache operationalCache,
                                  @Nonnull final ConstructionService constructionService,
                                  @Nonnull final WarShipService warShipService,
                                  @Nonnull final FleetService fleetService) {
        this.colonizationService = Preconditions.checkNotNull(colonizationService, "colonizationService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.colonizationCache = Preconditions.checkNotNull(colonizationCache, "colonizationCache must not be empty");
        this.operationalCache = Preconditions.checkNotNull(operationalCache, "operationalCache must not be empty");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService must not be empty");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
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
                operateInoperationals(planet);
                colonizationService.delete(colonization);
                colonizationCache.add(today, planet);
            } else {
                colonizationService.save(colonization);
            }
        }
    }

    public void operateInoperationals(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final ResourceDeposit deposit = planet.getResourceDeposit();
        final ResourceDeposit demand = planet.getResourceDemand();
        final ResourceDeposit utilization = planet.getResourceUtilization();

        activateWarships(planet, deposit, demand, utilization);
        activateConstructions(planet, deposit, demand, utilization);

        planetService.save(planet);
    }


    public void activateConstructions(@Nonnull final Planet planet,
                                      @Nonnull final ResourceDeposit deposit,
                                      @Nonnull final ResourceDeposit demand,
                                      @Nonnull final ResourceDeposit utilization) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(deposit, "deposit must not be empty");
        Preconditions.checkNotNull(demand, "demand must not be empty");
        Preconditions.checkNotNull(utilization, "utilization must not be empty");

        // prio 1: military stuff, prio 2: higher tech level
        final List<Construction> supplyNeeded = planet.getConstructions().stream()
                .filter(c -> c.getOperationalLevel() < c.getLevel())
                .sorted((o1, o2) -> {
                    final ERefinementSequence o1RS = o1.getBuilding().getProductionType().getRefinementSequence();
                    final ERefinementSequence o2RS = o2.getBuilding().getProductionType().getRefinementSequence();
                    if (o1RS != null && o2RS != null) {
                        return Integer.compare(o1RS.getEducationPriority(), o2RS.getEducationPriority());
                    }
                    final ERefinementSequence valid = o1RS != null ? o1RS : o2RS;
                    if (valid != null) {
                        return valid.getEducationPriority() == 2 ? 1 : -1;
                    }
                    return Integer.compare(o1.getBuilding().getTechLevel().ordinal(), o2.getBuilding().getTechLevel().ordinal());
                })
                .collect(Collectors.toList());

        Collections.reverse(supplyNeeded);

        final List<Construction> ops = new ArrayList<>();
        for (final Construction inoperational : supplyNeeded) {
            final ResourceDeposit costs = inoperational.getBuilding().getCosts();
            final int activeLevel = inoperational.getOperationalLevel();
            final int level = inoperational.getLevel();
            for (int i = activeLevel + 1; i <= level; i++) {
                final CrewRequirement costsForLevel = JobCostsCalculator.getCostsForLevel(costs, i).getCrewRequirement();
                final PayingPossibleResult result = deposit.isPayingPossible(costsForLevel);
                if (result.isValidForPops()) {
                    deposit.updateCrew(costsForLevel, ECalculationType.SUBTRACT);
                    demand.updateCrew(costsForLevel, ECalculationType.SUBTRACT);
                    utilization.updateCrew(costsForLevel, ECalculationType.ADD);

                    inoperational.setOperationalLevel(i);
                    ops.add(inoperational);
                }
            }
        }
        if (!ops.isEmpty()) {
            operationalCache.activateConstructions(today, planet, constructionService.saveAll(ops));
        }
    }

    public void activateWarships(@Nonnull final Planet planet,
                                 @Nonnull final ResourceDeposit deposit,
                                 @Nonnull final ResourceDeposit demand,
                                 @Nonnull final ResourceDeposit utilization) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(deposit, "deposit must not be empty");
        Preconditions.checkNotNull(demand, "demand must not be empty");
        Preconditions.checkNotNull(utilization, "utilization must not be empty");

        final List<WarShip> operationals = new ArrayList<>();
        final List<WarShip> inoperationals = warShipService.findAliveInoperationalForPlanet(planet.getId());
        for (final WarShip inoperational : inoperationals) {
            final CrewRequirement costs = inoperational.getShipClass().getCosts().getCrewRequirement();
            final PayingPossibleResult result = deposit.isPayingPossible(costs);
            if (result.isValidForPops()) {
                deposit.updateCrew(costs, ECalculationType.SUBTRACT);
                demand.updateCrew(costs, ECalculationType.SUBTRACT);
                utilization.updateCrew(costs, ECalculationType.ADD);

                inoperational.setOperational();
                operationals.add(inoperational);
            }
        }
        if (!operationals.isEmpty()) {
            warShipService.saveAll(operationals);
            Set<Fleet> fleets = operationals.stream().map(WarShip::getFleet).collect(Collectors.toSet());
            fleets = fleets.stream().filter(f -> f.getAliveShips().stream().allMatch(Operationable::isOperational)).collect(Collectors.toSet());
            fleets.forEach(Fleet::setOperational);
            fleetService.saveAll(fleets);
            operationalCache.activateWarships(today, planet, operationals);
        }
    }

    public void operateInoperationals(@Nonnull final Tick today, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        this.today = today;
        operateInoperationals(planet);
    }
}
