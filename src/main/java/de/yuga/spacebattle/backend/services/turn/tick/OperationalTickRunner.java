package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.Completable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.battle.combat.WarshipHealthStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OperationalTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationalTickRunner.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final WarshipHealthStateService warshipHealthStateService;

    @Nonnull
    private final OperationalService operationalService;

    @Autowired
    public OperationalTickRunner(@Nonnull final JobService jobService,
                                 @Nonnull final PlanetService planetService,
                                 @Nonnull final FleetService fleetService,
                                 @Nonnull final WarshipHealthStateService warshipHealthStateService,
                                 @Nonnull final OperationalService operationalService) {
        this.jobService = Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.warshipHealthStateService = Preconditions.checkNotNull(warshipHealthStateService, "warshipHealthStateService must not be empty");
        this.operationalService = Preconditions.checkNotNull(operationalService, "operationalService must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Tick planets");
        tickPlanets();
    }


    private void log(@Nonnull final Planet planet, @Nonnull final String msg) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(msg, "msg must not be empty");

        LOGGER.info("[Planet #{}] {}", planet.getId(), msg);
    }

    /**
     * Runs the tick for all planets.
     */
    private void tickPlanets() {
        final List<Planet> planets = planetService.findAllColonized();
        final Set<Construction> oldLaboratories = new HashSet<>();
        for (final Planet p : planets) {
            log(p, "Start ticking planet");
            final Construction construction = tickPlanet(p);
            oldLaboratories.add(construction);
            tickFleetsAtStarbase(p);
            tickFleetsAtStarbase(p);
        }
        updateResearchJob(oldLaboratories.stream().filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    /**
     * Reduces the ticksLeft if necessary by new operational level.
     */
    private void updateResearchJob(@Nonnull final Set<Construction> oldLaboratories) {
        Preconditions.checkNotNull(oldLaboratories, "oldLaboratories must not be empty");

        final List<Job> toStore = new ArrayList<>();
        final List<Job> researchJobs = jobService.findResearchJobs();
        for (final Job job : researchJobs) {
            final Map<Construction, Integer> formerLaboratoryOperationalLevel = oldLaboratories.stream().collect(Collectors.toMap(Function.identity(), Construction::getOperationalLevel));
            final int idUser = Objects.requireNonNull(job.getFacility().getPlanet().getOwner()).getId();
            final BigDecimal formerEmpireWideResearchPoints = planetService.getEmpireWideResearchPoints(idUser, formerLaboratoryOperationalLevel);
            final BigDecimal empireWideResearchPoints = planetService.getEmpireWideResearchPoints(idUser);
            if (empireWideResearchPoints.compareTo(formerEmpireWideResearchPoints) > 0) {
                job.reduceRemainingTicksByLevelUpgrade(empireWideResearchPoints.divide(formerEmpireWideResearchPoints, Completable.MATH_CONTEXT));
                toStore.add(job);
            }
        }
        jobService.saveAll(toStore);
    }

    /**
     * Activates all constructions when possible.
     */
    @Nullable
    private Construction tickPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkState(planet.getOwner() != null, "The owner must be set, otherwise there is nothing to do.");

        log(planet, "Start activating operationals.");

        final Construction laboratory = planet.getConstructionByResource(EResourceType.RESEARCH).stream().findFirst().orElse(null);
        final Set<Construction> constructions = operationalService.operateInoperationals(today, planet);
        final Construction labWithNewLevel = constructions.stream().filter(c -> c.getBuilding().getProductionType().getProductionTarget() == EResourceType.RESEARCH).findFirst().orElse(null);

        updateJobs(constructions);
        log(planet, "Done activating operationals.");
        return laboratory != null && labWithNewLevel != null && laboratory.getOperationalLevel() < labWithNewLevel.getOperationalLevel() ? laboratory : null;
    }

    private void updateJobs(@Nonnull final Set<Construction> operatedConstructions) {
        Preconditions.checkNotNull(operatedConstructions, "operatedConstructions must not be empty");

        final Construction upgradedShipyard = operatedConstructions.stream().filter(c -> c.getBuilding().getProductionTarget() == EResourceType.ORBITAL_CONSTRUCTION && c.getBuilding().getProductionType().getProductionCategory() == EProductionCategory.PRODUCE).findFirst().orElse(null);
        if (upgradedShipyard != null) {
            final List<Job> jobsByPlanet = jobService.findAllJobsByPlanet(upgradedShipyard.getPlanet().getId()).stream()
                    .filter(j -> j.getConstructable().getFleet() != null)
                    .collect(Collectors.toList());

            jobsByPlanet.forEach(j -> j.reduceRemainingTicksByLevelUpgrade(j.getFacility().getBuilding().getIncreasingFactorPerLevel()));
            jobService.saveAll(jobsByPlanet);
        }
    }

    /**
     * Refresh all ammunition for a fleet in a starbase orbit.
     */
    private void tickFleetsAtStarbase(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        assert planet.getOwner() != null : "Please be colonized!";
        final Set<Fleet> anchoredFleets = fleetService.findAllAnchoredForPlanet(planet);
        final Set<WarshipHealthState> healthStates = anchoredFleets.stream()
                .filter(f -> f.getOwner().getId() == planet.getOwner().getId())
                .map(Fleet::getAliveShips)
                .flatMap(Collection::stream)
                .map(WarShip::getWarshipHealthState)
                .collect(Collectors.toSet());
        healthStates.forEach(WarshipHealthState::ammoUp);
        warshipHealthStateService.saveAll(healthStates);
    }
}
