package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.PopulationControlCalculator;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.calculator.resource.TickOutputCalculator;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.misc.HasOwner;
import de.yuga.spacebattle.backend.entities.turn.resources.MiningFactors;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "Planet.getAll", query = "SELECT p FROM Planet p"),
        @NamedQuery(name = "Planet.getAllOwned", query = "SELECT p FROM Planet p WHERE p.owner IS NOT NULL AND p.owner.dType = de.yuga.spacebattle.backend.enums.OwnerType.USER"),
        @NamedQuery(name = "Planet.getAllOwnedBy", query = "SELECT p FROM Planet p WHERE p.owner.id = :idOwner ORDER BY p.colonizedAt"),
        @NamedQuery(name = "Planet.getPlanetsWithBuildingsForResourceType",
                query = "SELECT p FROM Planet p LEFT JOIN FETCH p.constructions c WHERE p.owner.id = :idUser AND c.building.productionType.productionTarget = :resourceType"),
        @NamedQuery(name = "Planet.getMainPlanet", query = "SELECT p FROM Planet p WHERE p.owner.id = :idUser AND p.isMain = true"),
        @NamedQuery(name = "Planet.getByCoordinates", query = "SELECT p FROM Planet p WHERE p.system.id = :idStarSystem AND p.orbit.xCoordinate = :xCoordinate AND p.orbit.yCoordinate = :yCoordinate"),
})
@Entity
@Table(name = "planet",
        uniqueConstraints =
        @UniqueConstraint(name = "PLANET_UK", columnNames = {"idStarSystem", "idPlanet", "xCoordinate", "yCoordinate"}))
@AttributeOverride(name = "id", column = @Column(name = "idPlanet"))
public class Planet extends AbstractEntityKey implements HasOwner {

    @Nonnull
    @Transient
    public static final EDistanceMetric PLANET_STANDARD_METRIC = EDistanceMetric.LS;

    @Nullable
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "idOwner")
    private Owner owner;

    @Nonnull
    @NotNull
    @Size(min = 1, max = 30)
    private String name;

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idStarSystem", updatable = false)
    private StarSystem system;

    @Embedded
    private Orbit orbit;

    /**
     * Describes the mining factors for every resource.
     */
    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idMiningFactors", nullable = false)
    private final MiningFactors miningFactors = new MiningFactors();

    /**
     * The amount of resources at this planet.
     */
    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idResourceDeposit", updatable = false)
    private final ResourceDeposit resourceDeposit = ResourceDepositInitializerCalculator.initializeResourceDeposit();

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idResourceTransportationDemand", updatable = false)
    private final ResourceDeposit resourceTransportationDemand = new ResourceDeposit(EDepositType.TRANSPORTATION_DEMAND);

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idResourceTransportationDelivery", updatable = false)
    private final ResourceDeposit resourceTransportationDelivery = new ResourceDeposit(EDepositType.TRANSPORTATION_DELIVERY);

    @Nonnull /* fixme remove eager and replace by direct fetching */
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, mappedBy = "planet")
    private final Set<Construction> constructions = new HashSet<>();

    @Nullable
    private LocalDateTime colonizedAt;

    /**
     * Marks if the planet is the main of the owner.
     */
    @Column(columnDefinition = "boolean not null default false")
    private boolean isMain;

    @Nonnull
    @Transient
    private final EPlanetClassType planetType = EPlanetClassType.PLANET;

    public Planet() {
    }

    public Planet(@Nonnull final String name,
                  @Nonnull final StarSystem system,
                  @Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(system, "system shouldn't be null!");
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.colonizedAt = owner != null ? LocalDateTime.now() : null;
        this.name = name;
        this.system = system;
        this.orbit = orbit;
    }

    @Nullable
    @Override
    public Owner getOwner() {
        return owner;
    }

    @Nullable
    @Override
    public User getHumanOwner() {
        if (!(owner instanceof User)) {
            return null;
        }
        return (User) owner;
    }

    @Nullable
    @Override
    public NonPlayerCharacter getNpcOwner() {
        if (!(owner instanceof NonPlayerCharacter)) {
            return null;
        }
        return (NonPlayerCharacter) owner;
    }

    public void setOwner(@Nonnull final Owner owner) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");

        colonizedAt = LocalDateTime.now();
        this.owner = owner;
    }

    @Nonnull
    public ResourceDeposit getResourceDeposit() {
        return resourceDeposit;
    }

    @Nonnull
    public Set<Construction> getConstructions() {
        return constructions;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull final String name) {
        this.name = name;
    }

    @Nonnull
    public StarSystem getSystem() {
        return system;
    }

    @Nonnull
    public MiningFactors getMiningFactors() {
        return miningFactors;
    }

    @Nonnull
    public Orbit getOrbit() {
        return orbit;
    }

    @Nullable
    public LocalDateTime getColonizedAt() {
        return colonizedAt;
    }

    public boolean isMain() {
        return isMain;
    }

    public void toggleMain() {
        isMain = true;
    }

    @Nonnull
    public EPlanetClassType getPlanetType() {
        return planetType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Planet)) return false;

        Planet planet = (Planet) o;

        return id == planet.id;
    }

    @Override
    public int hashCode() {
        return id * 7;
    }

    /**
     * Returns the facility which produces the resource type.
     *
     * @param resourceType the requested resource type
     * @return the facility
     */
    @Nonnull
    public Set<Construction> getConstructionByResource(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        return getConstructions().stream()
                .filter(construction -> construction.getBuilding().getProductionTarget() == resourceType)
                .collect(Collectors.toSet());
    }

    /**
     * Checks if the planet is colonizable.
     *
     * @return <code>true</code> if the planet is colonizable, <code>false</code> otherwise
     */
    public boolean isColonizable() {
        return owner == null;
    }

    /**
     * Returns the capacity of {@link ECollectableType#COLLECTABLE} or {@link ECollectableType#VIABLE} which can be placed in the storages of the planet.<br>
     * <br>
     * If a resource type isn't present, it means that there is no capacity restriction.
     */
    public ResourceDeposit getResourceCapacity() {
        final ResourceDeposit cap = new ResourceDeposit(EDepositType.CAPACITY);
        final Set<Construction> capacityBuildings = getConstructions().stream()
                .filter(construction -> EProductionCategory.CAPACITY == construction.getBuilding().getProductionType().getProductionCategory())
                .collect(Collectors.toSet());

        for (final EResourceType resourceType : EResourceType.values()) {
            final Set<Construction> forResource = capacityBuildings.stream().filter(c -> resourceType == c.getBuilding().getProductionTarget()).collect(Collectors.toSet());
            if (!forResource.isEmpty()) {
                final BigDecimal capacityValue = TickOutputCalculator.getTickOutput(forResource);
                cap.updateResource(resourceType, capacityValue.longValue());
            }
        }
        return cap;
    }

    /**
     * There are some strange rules running:<br>
     * <br>
     * All resources except {@link EResourceType#POPULATION} are normal, as always.<br>
     * Applies to the pop:
     * <ul>
     *     <li>the amount of population is directly applied as resource type and ...
     *     <ul>
     *         <li>if it's below zero ... someone dies per tick</li>
     *         <li>if it's above zero, it will be the amount of newborns per tick</li>
     *     </ul>
     *     </li>
     *     <li>the education types represents a transition which is defined by the refinement type</li>
     *     <li><b>except</b> if it's about {@link EEducationType#NONE} ... then it a a transition from the universe to population</li>
     * </ul>
     *
     * @return the income by tick
     */
    @Nonnull
    public ResourceDeposit getTicklyIncome(@Nonnull final ResourceDeposit utilization) {
        Preconditions.checkNotNull(utilization, "utilization must not be empty");

        final ResourceDeposit income = new ResourceDeposit(EDepositType.INCOME);
        final Map<EResourceType, List<Construction>> resourceConstructionsByType = getConstructions().stream()
                .collect(Collectors.groupingBy(c -> c.getBuilding().getProductionTarget(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final List<Construction> populationConstruction = resourceConstructionsByType.getOrDefault(EResourceType.POPULATION, new ArrayList<>());
        //noinspection DataFlowIssue
        final Map<ERefinementSequence, List<Construction>> constructionsByRefinementSequence = populationConstruction.stream()
                .filter(c -> c.getBuilding().getProductionType().getProductionCategory() == EProductionCategory.REFINEMENT)
                .collect(Collectors.groupingBy(c -> c.getBuilding().getProductionType().getRefinementSequence(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        for (final EResourceType eResourceType : EResourceType.valuesWithoutPopulation()) {
            final List<Construction> constructions = resourceConstructionsByType.getOrDefault(eResourceType, new ArrayList<>());
            final BigDecimal ticklyIncome = TickOutputCalculator.getTickOutput(constructions);
            income.setAbsoluteResourceValue(eResourceType, ticklyIncome.longValue());
        }

        constructionsByRefinementSequence.forEach((eRefinementSequence, refinementConstructions) -> {
            final EEducationType educationType = eRefinementSequence.getProduct();
            final BigDecimal ticklyIncome = TickOutputCalculator.getTickOutput(refinementConstructions);
            income.setAbsolutePopulation(educationType, ticklyIncome.longValue());
        });

        final long tickOutputForPopulation = PopulationControlCalculator.getTickOutputForPopulation(this, utilization);
        income.setAbsolutePopulationValue(tickOutputForPopulation);
        income.setAbsolutePopulation(EEducationType.NONE, tickOutputForPopulation);

        return income;
    }

    /**
     * Indicates a ground construction job can be started.
     *
     * @return <code>true</code> if a job can be started, <code>false</code> otherwise
     */
    public boolean isConstructionPossible() {
        return getConstructionByResource(EResourceType.CONSTRUCTION).stream().anyMatch(c -> c.getJobs().isEmpty());
    }


    public int calculateTicksToCollect(@Nonnull final ResourceDeposit costs, @Nonnull final ResourceDeposit utilization) {
        Preconditions.checkNotNull(costs, "costs must not be empty");
        Preconditions.checkNotNull(utilization, "utilization must not be empty");
        Preconditions.checkArgument(costs.getSubType() == EDepositType.COSTS, "costs must not be costs");

        final ResourceDeposit cloneOfCosts = new ResourceDeposit(costs);
        final ResourceDeposit ticklyIncome = getTicklyIncome(utilization);
        cloneOfCosts.subtract(resourceDeposit);

        // check collectable resources
        int tickCounterCollectable = 0;
        final PayingPossibleResult payingPossible = getResourceDeposit().isPayingPossible(cloneOfCosts);
        if (!payingPossible.isValid()) {
            boolean isPayingPossible = false;
            while (!isPayingPossible && tickCounterCollectable != 999) {
                cloneOfCosts.subtract(ticklyIncome);
                isPayingPossible = getResourceDeposit().isPayingPossible(cloneOfCosts).isValid();
                tickCounterCollectable++;
            }
            if (tickCounterCollectable == 356) {
                // stop if a year of game time is reached
                final PayingPossibleResult result = getResourceDeposit().isPayingPossible(costs);
                throw new NotifyWebUserException(result.getMessage(), result);
            }
        }

        // check resources which cannot be collected
        final PayingPossibleResult result = new PayingPossibleResult();
        int tickCounterForfeitable = 0;
        for (final EResourceType eResourceType : EResourceType.valuesWhichForfeits()) {
            final long forfeitableIncome = ticklyIncome.getResourceAmountByType(eResourceType);
            final long forfeitableCost = cloneOfCosts.getResourceAmountByType(eResourceType);
            if (forfeitableCost == 0) {
                continue;
            }
            if (forfeitableIncome == 0) {
                result.addProblem(eResourceType);
            } else {
                final int ticksForForfeitable = (int) ((int) forfeitableCost / forfeitableIncome);
                tickCounterForfeitable = Integer.max(ticksForForfeitable, tickCounterForfeitable);
            }
        }
        if (!result.isValid()) {
            throw new NotifyWebUserException(result.getMessage(), result);
        }

        return Integer.max(tickCounterCollectable, tickCounterForfeitable);
    }

    @Nonnull
    public ResourceDeposit getResourceTransportationDemand() {
        return resourceTransportationDemand;
    }

    @Nonnull
    public ResourceDeposit getResourceTransportationDelivery() {
        return resourceTransportationDelivery;
    }
}
