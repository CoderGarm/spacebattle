package de.yuga.spacebattle.backend.services.constructables;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.JobCostsCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.turn.Commissioning;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.Operationable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Constructable;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.repositories.turn.JobRepository;
import de.yuga.spacebattle.backend.services.caches.OperationalCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OperationalService {

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final ConstructionService constructionService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final OperationalCache operationalCache;

    // todo at a given time separate responsibilities
    @Nonnull
    private final JobRepository jobRepository;

    @Autowired
    public OperationalService(@Nonnull final WarShipService warShipService,
                              @Nonnull final ConstructionService constructionService,
                              @Nonnull final FleetService fleetService,
                              @Nonnull final PlanetService planetService,
                              @Nonnull final ColonizationService colonizationService,
                              @Nonnull final OperationalCache operationalCache,
                              @Nonnull final JobRepository jobRepository) {
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.colonizationService = Preconditions.checkNotNull(colonizationService, "colonizationService must not be empty");
        this.operationalCache = Preconditions.checkNotNull(operationalCache, "operationalCache must not be empty");
        this.jobRepository = Preconditions.checkNotNull(jobRepository, "jobService must not be empty");
    }

    @Nonnull
    public ResourceDeposit getUtilizedPopulationForPlanet(final int idPlanet) {
        final ResourceDeposit resourceDemand = new ResourceDeposit(EDepositType.UTILIZATION);

        warShipService.findAliveOperationalForPlanet(idPlanet).stream()
                .map(WarShip::getShipClass)
                .map(ShipClass::getCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));

        constructionService.findAllConstructionsOnPlanet(idPlanet).stream()
                .map(c -> {
                    final int operationalLevel = c.getOperationalLevel();
                    if (operationalLevel == 0) {
                        return new ResourceDeposit(EDepositType.UTILIZATION);
                    }
                    return new Constructable(c.getBuilding(), operationalLevel).getJobCosts();
                })
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));
        return resourceDemand;
    }

    @Nonnull
    public ResourceDeposit getUtilizedPopulationForUser(final int idUser) {
        final ResourceDeposit resourceDemand = new ResourceDeposit(EDepositType.UTILIZATION);
        final List<WarShip> operationalForPlanet = warShipService.findAliveOperationalForUser(idUser);
        operationalForPlanet.stream()
                .map(WarShip::getShipClass)
                .map(ShipClass::getCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));

        final List<Construction> constructions = constructionService.findAllConstructionsForUser(idUser);
        constructions.stream()
                .map(c -> {
                    final int operationalLevel = c.getOperationalLevel();
                    if (operationalLevel == 0) {
                        return new ResourceDeposit(EDepositType.UTILIZATION);
                    }
                    return new Constructable(c.getBuilding(), operationalLevel).getJobCosts();
                })
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));
        return resourceDemand;
    }

    @Nonnull
    public ResourceDeposit getPopulationDemandForUser(final int idUser) {
        final ResourceDeposit resourceDemand = new ResourceDeposit(EDepositType.DEMAND);

        final List<Job> constructions = Objects.requireNonNullElse(jobRepository.findAllConstructionJobsForUser(idUser), List.of());
        constructions.stream()
                .map(Job::getConstructable)
                .map(Constructable::getFleet)
                .filter(Objects::nonNull)
                .map(Fleet::getAllShips)
                .flatMap(Collection::stream)
                .map(WarShip::getShipClass)
                .map(ShipClass::getCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));
        constructions.stream()
                .filter(c -> c.getConstructable().getBuilding() != null)
                .filter(c -> c.getConstructable().getTargetLevel() != null)
                .map(c -> new Construction(c.getFacility().getPlanet(), c.getConstructable().getBuilding(), c.getConstructable().getTargetLevel()))
                .map(OperationalService::sumUpCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));

        final List<WarShip> warShips = warShipService.findAliveInoperationalForUser(idUser);

        final Set<WarShip> inUpgrade = warShips.stream()
                .map(WarShip::getFleet)
                .filter(Objects::nonNull)
                .filter(fleet -> fleet.getJobs().stream().map(Job::getConstructable).anyMatch(Constructable::isUpgradeJob))
                .map(Fleet::getAliveShips)
                .flatMap(Collection::stream)
                .filter(w -> w.getShipClass().hasSuccessor())
                .collect(Collectors.toSet());
        warShips.removeAll(inUpgrade);

        inUpgrade.stream()
                .map(WarShip::getShipClass)
                .map(ShipClass::getLatestSuccessor)
                .filter(Objects::nonNull)
                .map(ShipClass::getCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));

        warShips.stream()
                .map(WarShip::getShipClass)
                .map(ShipClass::getCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));
        constructionService.findInoperationalForUser(idUser).stream()
                .map(OperationalService::sumUpCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));

        colonizationService.findAllPlannedForUser(idUser).stream().map(Colonization::getCosts).forEach(costs -> {
            resourceDemand.updateCrew(costs.getCrewRequirement(), ECalculationType.ADD);
        });
        return resourceDemand;
    }

    @Nonnull
    public Map<Planet, ResourceDeposit> getPopulationDemandForUserByPlanet(final int idUser) {

        final Map<Planet, ResourceDeposit> result = new HashMap<>();

        final List<Job> constructions = Objects.requireNonNullElse(jobRepository.findAllConstructionJobsForUser(idUser), List.of());
        final Set<WarShip> shipsFromJobs = constructions.stream()
                .map(Job::getConstructable)
                .map(Constructable::getFleet)
                .filter(Objects::nonNull)
                .map(Fleet::getAllShips)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        constructions.stream()
                .filter(c -> c.getConstructable().getBuilding() != null)
                .filter(c -> c.getConstructable().getTargetLevel() != null)
                .forEach(job -> {
                    final Planet planet = job.getFacility().getPlanet();
                    final ResourceDeposit demand = result.getOrDefault(planet, new ResourceDeposit(EDepositType.DEMAND));
                    final Construction c = new Construction(job.getFacility().getPlanet(), job.getConstructable().getBuilding(), job.getConstructable().getTargetLevel());
                    demand.updateCrew(sumUpCosts(c).getCrewRequirement(), ECalculationType.ADD);
                    result.put(planet, demand);
                });

        final List<WarShip> warShips = warShipService.findAliveInoperationalForUser(idUser);
        warShips.addAll(shipsFromJobs);

        final Set<WarShip> inUpgrade = warShips.stream()
                .map(WarShip::getFleet)
                .filter(Objects::nonNull)
                .filter(fleet -> fleet.getJobs().stream().map(Job::getConstructable).anyMatch(Constructable::isUpgradeJob))
                .map(Fleet::getAliveShips)
                .flatMap(Collection::stream)
                .filter(w -> w.getShipClass().hasSuccessor())
                .collect(Collectors.toSet());
        warShips.removeAll(inUpgrade);

        inUpgrade.forEach(warShip -> {
            final ShipClass shipClass = warShip.getShipClass().getLatestSuccessor();
            final Planet planet = warShip.getShipyard();
            final ResourceDeposit demand = result.getOrDefault(planet, new ResourceDeposit(EDepositType.DEMAND));
            demand.updateCrew(Objects.requireNonNull(shipClass).getCosts().getCrewRequirement(), ECalculationType.ADD);
            result.put(planet, demand);
        });

        warShips.forEach(warShip -> {
            ShipClass shipClass = warShip.getShipClass();
            final Planet planet = warShip.getShipyard();
            final ResourceDeposit demand = result.getOrDefault(planet, new ResourceDeposit(EDepositType.DEMAND));
            demand.updateCrew(shipClass.getCosts().getCrewRequirement(), ECalculationType.ADD);
            result.put(planet, demand);
        });
        constructionService.findInoperationalForUser(idUser).forEach(c -> {
            final Planet planet = c.getPlanet();
            final ResourceDeposit demand = result.getOrDefault(planet, new ResourceDeposit(EDepositType.DEMAND));
            demand.updateCrew(sumUpCosts(c).getCrewRequirement(), ECalculationType.ADD);
            result.put(planet, demand);
        });
        final List<Colonization> allPlannedForUser = colonizationService.findAllPlannedForUser(idUser);
        if (!allPlannedForUser.isEmpty()) {
            Planet main = result.keySet().stream().filter(Planet::isMain).findFirst().orElse(null);
            if (main == null) {
                main = planetService.findMainPlanet(idUser);
            }
            final ResourceDeposit demand = result.getOrDefault(main, new ResourceDeposit(EDepositType.DEMAND));
            allPlannedForUser.stream().map(Colonization::getCosts).forEach(costs -> {
                demand.updateCrew(costs.getCrewRequirement(), ECalculationType.ADD);
            });
            result.put(main, demand);
        }
        return result;
    }


    @Nonnull
    public ResourceDeposit getPopulationDemandForPlanet(final int idPlanet) {
        final ResourceDeposit resourceDemand = new ResourceDeposit(EDepositType.DEMAND);

        final List<Job> constructions = Objects.requireNonNullElse(jobRepository.findAllConstructionJobsByPlanet(idPlanet), List.of());
        constructions.stream()
                .map(Job::getConstructable)
                .map(Constructable::getFleet)
                .filter(Objects::nonNull)
                .map(Fleet::getAllShips)
                .flatMap(Collection::stream)
                .map(WarShip::getShipClass)
                .map(ShipClass::getCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));
        constructions.stream()
                .filter(c -> c.getConstructable().getBuilding() != null)
                .filter(c -> c.getConstructable().getTargetLevel() != null)
                .map(c -> new Construction(c.getFacility().getPlanet(), c.getConstructable().getBuilding(), c.getConstructable().getTargetLevel()))
                .map(OperationalService::sumUpCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));

        final List<WarShip> warShips = warShipService.findAliveInoperationalForPlanet(idPlanet);

        final Set<WarShip> inUpgrade = warShips.stream()
                .map(WarShip::getFleet)
                .filter(Objects::nonNull)
                .filter(fleet -> fleet.getJobs().stream().map(Job::getConstructable).anyMatch(Constructable::isUpgradeJob))
                .map(Fleet::getAliveShips)
                .flatMap(Collection::stream)
                .filter(w -> w.getShipClass().hasSuccessor())
                .collect(Collectors.toSet());
        warShips.removeAll(inUpgrade);

        inUpgrade.stream()
                .map(WarShip::getShipClass)
                .map(ShipClass::getLatestSuccessor)
                .filter(Objects::nonNull)
                .map(ShipClass::getCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));
        warShips.stream()
                .map(WarShip::getShipClass)
                .map(ShipClass::getCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));

        final List<Construction> inoperationalForPlanet = constructionService.findInoperationalForPlanet(idPlanet);
        inoperationalForPlanet.stream().map(OperationalService::sumUpCosts)
                .map(ResourceDeposit::getCrewRequirement)
                .forEach(crewRequirement -> resourceDemand.updateCrew(crewRequirement, ECalculationType.ADD));

        final Integer idUser = planetService.getIdUserWhenMain(idPlanet);
        if (idUser != null) {
            colonizationService.findAllPlannedForUser(idUser).stream().map(Colonization::getCosts).forEach(costs -> {
                resourceDemand.updateCrew(costs.getCrewRequirement(), ECalculationType.ADD);
            });
        }

        return resourceDemand;
    }

    @Nonnull
    private static ResourceDeposit sumUpCosts(@Nonnull final Construction construction) {
        Preconditions.checkNotNull(construction, "construction must not be empty");

        final ResourceDeposit fullCosts = new Constructable(construction.getBuilding(), construction.getLevel()).getJobCosts();
        if (construction.getOperationalLevel() == 0) {
            return fullCosts;
        }
        final ResourceDeposit paidCosts = new Constructable(construction.getBuilding(), construction.getOperationalLevel()).getJobCosts();
        fullCosts.pay(paidCosts);
        return fullCosts;
    }

    @Nonnull
    public Map<Planet, Commissioning> getCommissioningForUser(@Nonnull final Tick today, final int idUser) {
        Preconditions.checkNotNull(today, "today must not be empty");

        final Map<Planet, de.yuga.spacebattle.backend.dto.turn.Commissioning> commissioning = new HashMap<>();

        final Map<Planet, Set<Construction>> pendingConstructionsByPlanet = constructionService.findInoperationalForUser(idUser).stream()
                .collect(Collectors.groupingBy(Construction::getPlanet,
                        Collectors.mapping(Function.identity(), Collectors.toSet())));
        final Map<Planet, List<WarShip>> pendingShipsByYard = warShipService.findAliveInoperationalForUser(idUser).stream()
                .collect(Collectors.groupingBy(WarShip::getShipyard,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        pendingConstructionsByPlanet.forEach((planet, constructions) -> {
            de.yuga.spacebattle.backend.dto.turn.Commissioning orDefault = commissioning.get(planet);
            if (orDefault == null) {
                orDefault = new de.yuga.spacebattle.backend.dto.turn.Commissioning(today, planet, constructions);
            } else {
                orDefault.addConstructions(constructions);
            }
            commissioning.put(planet, orDefault);
        });

        pendingShipsByYard.forEach((planet, warShips) -> {
            de.yuga.spacebattle.backend.dto.turn.Commissioning orDefault = commissioning.get(planet);
            if (orDefault == null) {
                orDefault = new de.yuga.spacebattle.backend.dto.turn.Commissioning(today, planet, warShips);
            } else {
                orDefault.setWarships(warShips);
            }
            commissioning.put(planet, orDefault);
        });
        return commissioning;
    }

    @Nonnull
    private Set<Construction> activateConstructions(@Nonnull final Tick today,
                                                    @Nonnull final Planet planet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final ResourceDeposit deposit = planet.getResourceDeposit();

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

                    inoperational.setOperationalLevel(i);
                    ops.add(inoperational);
                }
            }
        }
        planetService.save(planet);
        return constructionService.saveAll(ops);
    }

    @Nonnull
    private List<WarShip> activateWarships(@Nonnull final Tick today,
                                           @Nonnull final Planet planet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final ResourceDeposit deposit = planet.getResourceDeposit();
        final List<WarShip> operationals = new ArrayList<>();
        final List<WarShip> inoperationals = warShipService.findAliveInoperationalForPlanet(planet.getId());
        for (final WarShip inoperational : inoperationals) {
            final CrewRequirement costs = inoperational.getShipClass().getCosts().getCrewRequirement();
            final PayingPossibleResult result = deposit.isPayingPossible(costs);
            if (result.isValidForPops()) {
                deposit.updateCrew(costs, ECalculationType.SUBTRACT);

                inoperational.setOperational();
                operationals.add(inoperational);
            }
        }
        if (!operationals.isEmpty()) {
            warShipService.saveAll(operationals);
            final Set<Fleet> fleets = operationals.stream().map(WarShip::getFleet).filter(Objects::nonNull).collect(Collectors.toSet()).stream()
                    .filter(f -> f.getAliveShips().stream().allMatch(Operationable::isOperational))
                    .collect(Collectors.toSet());
            fleets.forEach(Fleet::setOperational);
            fleetService.saveAll(fleets);
        }
        planetService.save(planet);
        return operationals;
    }

    @Nonnull
    public Set<Construction> operateInoperationals(@Nonnull final Tick today, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final List<WarShip> activated = activateWarships(today, planet);
        if (!activated.isEmpty()) {
            operationalCache.activateWarships(today, planet, activated);
        }

        final Set<Construction> alsoActivated = activateConstructions(today, planet);
        if (!alsoActivated.isEmpty()) {
            operationalCache.activateConstructions(today, planet, alsoActivated);
        }
        return alsoActivated;
    }

    public void retire(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip must not be empty");

        warShip.delete();
        final CrewRequirement crewRequirement = warShip.getShipClass().getCosts().getCrewRequirement();
        final Planet shipyard = warShip.getShipyard();
        shipyard.getResourceDeposit().updateCrew(crewRequirement, ECalculationType.ADD);
        planetService.save(shipyard);
        warShipService.save(warShip);
    }

    public void disableFleet(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        fleetService.markAsInoperational(fleet);
        final Set<WarShip> ships = fleet.getAliveShips();
        warShipService.markAsInoperational(ships);
    }

    public void transferCrewToPlanet(@Nonnull final Fleet fleet, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final ResourceDeposit costs = new ResourceDeposit(EDepositType.COSTS);
        fleet.getAliveShips().stream().map(WarShip::getShipClass).map(ShipClass::getCosts).forEach(rd -> costs.updateCrew(rd.getCrewRequirement(), ECalculationType.ADD));
        planet.getResourceDeposit().updateCrew(costs.getCrewRequirement(), ECalculationType.ADD);
        planetService.save(planet);
    }
}
