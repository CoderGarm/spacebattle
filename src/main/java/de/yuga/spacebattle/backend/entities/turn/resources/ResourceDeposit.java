package de.yuga.spacebattle.backend.entities.turn.resources;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.enums.EDepositType.*;
import static de.yuga.spacebattle.backend.enums.EResourceType.*;

/**
 * The resource deposit itself represents a various uses, compare {@link EDepositType}.
 */
@NamedQueries({
        @NamedQuery(name = "ResourceDeposit.getAll",
                query = "SELECT p FROM ResourceDeposit p"),
        @NamedQuery(name = "ResourceDeposit.getCostsForBuilding",
                query = "SELECT p FROM ResourceDeposit p " +
                        "LEFT JOIN FETCH p.resources " +
                        "LEFT JOIN FETCH p.humanResources " +
                        "LEFT JOIN Building b ON (p.id = b.costs.id)" +
                        "WHERE b.id = :idBuilding " +
                        "AND p.subType = de.yuga.spacebattle.backend.enums.EDepositType.COSTS")
})
@Entity
@Table(name = "resourceDeposit")
@AttributeOverride(name = "id", column = @Column(name = "idResourceDeposit"))
public class ResourceDeposit extends AbstractEntityKey {

    /**
     * Defines the scaling of every integer - as single digit - related roundings.
     */
    public final static MathContext MATH_CONTEXT_INTEGER = new MathContext(0, RoundingMode.DOWN);

    /**
     * Defines the scaling for e.g. {@link EResourceType#POPULATION} related roundings.
     */
    public final static MathContext MATH_CONTEXT_MORE_PRECISION = new MathContext(4, RoundingMode.DOWN);

    /**
     * The amount of resources and their type.<br>
     * <b>Attention:</b> The {@link EResourceType#POPULATION} is something special.
     */
    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "resourceType", updatable = false, length = 50)
    @MapKeyEnumerated(value = EnumType.STRING)
    @Column(name = "amount", columnDefinition = "decimal(19, 0)", nullable = false)
    @CollectionTable(name = "resourcesDepositComposition", joinColumns = @JoinColumn(name = "idResourceDeposit"))
    private Map<EResourceType, Long> resources = new HashMap<>();

    /**
     * Everything about the population.
     */
    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "educationType", updatable = false, length = 50)
    @MapKeyEnumerated(value = EnumType.STRING)
    @Column(name = "amount", columnDefinition = "decimal(19, 0)", nullable = false)
    @CollectionTable(name = "humanResources", joinColumns = @JoinColumn(name = "idResourceDeposit"))
    private Map<EEducationType, Long> humanResources = new HashMap<>();

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private EDepositType subType;

    public ResourceDeposit() {
    }

    public ResourceDeposit(@Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");

        this.subType = resourceDeposit.getSubType();
        this.resources = new HashMap<>(resourceDeposit.resources);
        this.humanResources = new HashMap<>(resourceDeposit.humanResources);
    }

    public ResourceDeposit(@Nonnull final EDepositType subType) {
        Preconditions.checkNotNull(subType, "subType shouldn't be null!");

        this.subType = subType;
    }

    public boolean hasData() {
        return !getResources().isEmpty() || !getHumanResources().isEmpty();
    }

    /**
     * Returns the amount of resources by type.<br>
     * In case of {@link EResourceType#POPULATION} it will return the total available amount of every {@link EEducationType}.<br>
     * <b>ATTENTION:</b> If an amount for the population is set, it will be returned.<br>
     * <b>SHOULD</b> only be used to display the increasing or decreasing population on a planet per tick.
     *
     * @param resourceType the resource type
     * @return the amount
     */
    public long getResourceAmountByType(@Nullable final EResourceType resourceType) {

        if (resources.containsKey(resourceType)) {
            return this.resources.get(resourceType);
        }

        if (POPULATION == resourceType) {
            // just sum up the total of all kinds
            return humanResources.values().stream()
                    .filter(Objects::nonNull)
                    .mapToLong(Long::longValue).sum();

        }
        return 0;
    }

    /**
     * Checks if paying with the parameters is possible.
     *
     * @param resourceType the resource type
     * @param amount       the amount
     * @return <code>true</code> if it can be paid, <code>false</code> otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isReducingResourcePossible(@Nonnull final EResourceType resourceType, final long amount) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        return getResourceAmountByType(resourceType) >= amount;
    }

    public PayingPossibleResult isPayingPossible(@Nonnull final ResourceDeposit costs) {
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");
        Preconditions.checkArgument(COSTS == costs.getSubType(), "costs must be flagged as costs!");

        PayingPossibleResult result = new PayingPossibleResult();
        for (final EResourceType resourceType : EResourceType.valuesWhichAreCollectable()) {
            final long debit = costs.getResourceAmountByType(resourceType);
            if (debit > 0 && !isReducingResourcePossible(resourceType, debit)) {
                result.addProblem(resourceType);
            }
        }
        return result;
    }

    public PayingPossibleResult isPayingPossible(@Nonnull final EResourceType resourceType, final long amount) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        final PayingPossibleResult result = new PayingPossibleResult();
        if (amount > 0 && !isReducingResourcePossible(resourceType, amount)) {
            result.addProblem(resourceType);
        }
        return result;
    }

    public PayingPossibleResult isPayingPossible(@Nonnull final CrewRequirement costs) {
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");
        Preconditions.checkArgument(COSTS == costs.getSubType(), "costs must be flagged as costs!");

        PayingPossibleResult result = new PayingPossibleResult();
        for (final EEducationType resourceType : EEducationType.values()) {
            final long debit = costs.getCrewAmountByType(resourceType);
            if (getCrewAmountByType(resourceType) < debit) {
                result.addProblem(resourceType);
            }
        }
        return result;
    }

    public void pay(@Nonnull final ResourceDeposit costs) {
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");
        Preconditions.checkArgument(COSTS == costs.getSubType(), "costs must be flagged as costs!");

        for (final EResourceType resourceType : EResourceType.valuesWhichAreCollectable()) {
            final long debit = costs.getResourceAmountByType(resourceType) * -1;
            pay(resourceType, debit);
        }
    }

    public void pay(@Nonnull final EResourceType resourceType, final long amount) {
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");

        final long debit = Math.min(amount, -amount);
        updateResource(resourceType, debit);
    }

    @Nonnull
    public EDepositType getSubType() {
        return subType;
    }

    public void setSubType(@Nonnull final EDepositType subType) {
        Preconditions.checkNotNull(subType, "subType shouldn't be null!");

        this.subType = subType;
    }

    /**
     * <b>Attention:</b> If it's about a {@link EResourceType#POPULATION} it will be ignored.<br>
     * Will add the given amount to the current amount.<br>
     * The result for the specific resource type cannot be below zero.<br>
     *
     * @param resourceType the resource type
     * @param amount       the amount to add
     */
    public void updateResource(@Nonnull final EResourceType resourceType, final long amount) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        if (POPULATION == resourceType && subType != CAPACITY) {
            return;
        }
        final long value;
        if (resources.containsKey(resourceType)) {
            value = this.resources.get(resourceType) + amount;
        } else {
            value = amount;
        }
        if (value < 0) {
            throw new NotifyWebUserException("No, you cannot reduce it below zero, even if it is " + resourceType + ".");
        }
        resources.put(resourceType, value);
    }

    public Set<EResourceType> getForfeitableResource() {
        return resources.entrySet().stream()
                .filter(e -> {
                    final EResourceType key = e.getKey();
                    final Long value = e.getValue();
                    return key.getCollectableType() == ECollectableType.FORFEITABLE && value != null && value > 0;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Sets an absolute value to the resource type. There is no validation inside, so it can be below zero.<br>
     * <b>Attention:</b> If it's about a {@link EResourceType#POPULATION} it will be ignored.<br>
     *
     * @param resourceType the resource type
     * @param amount       the amount to set
     */
    public void setAbsoluteResourceValue(@Nonnull final EResourceType resourceType, final long amount) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        if (POPULATION == resourceType) {
            return;
        }
        resources.put(resourceType, amount);
    }

    /**
     * Sets the amount of the resource for {@link EResourceType#POPULATION}.<br>
     * <br>
     * Please keep in mind that this should be only used to represent the tickly income
     * in order to make it clear that the population can decrease if no housing is present.
     *
     * @param amount the amount
     */
    public void setAbsolutePopulationValue(final long amount) {
        resources.put(POPULATION, amount);
    }

    /**
     * Returns a shallow copy.
     */
    @Nonnull
    public Map<EResourceType, Long> getResources() {
        final Map<EResourceType, Long> map = resources.entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new HashMap<>(map);
    }

    /**
     * Returns a shallow copy.
     */
    @Nonnull
    public Map<EEducationType, Long> getHumanResources() {
        final Map<EEducationType, Long> map = humanResources.entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new HashMap<>(map);
    }

    @Nonnull
    public CrewRequirement getCrewRequirement() {
        return new CrewRequirement(humanResources, subType);
    }

    public void setCrewRequirement(@Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        for (final EEducationType educationType : EEducationType.values()) {
            final long crewAmountByType = crewRequirement.getCrewAmountByType(educationType);
            humanResources.put(educationType, crewAmountByType);
        }
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setCrewRequirement(@Nonnull final Integer capacity, @Nonnull final Class<? extends HasCosts> clazz, @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(capacity, "capacity must not be empty");
        Preconditions.checkNotNull(clazz, "clazz must not be empty");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        for (final EEducationType educationType : EEducationType.values()) {
            final long crewAmountByType = crewRequirement.getCrewAmountByType(educationType);
            humanResources.put(educationType, crewAmountByType);
        }
        final ResourceDeposit resourceDeposit = ResourceDepositInitializerCalculator.initializeCosts(ETechLevel.TECH_I, capacity, EResourceDemand.getByClazz(clazz));
        for (final EResourceType eResourceType : EResourceType.valuesWithoutPopulation()) {
            final long amount = resourceDeposit.getResourceAmountByType(eResourceType);
            setAbsoluteResourceValue(eResourceType, amount);
        }

    }

    public void setAbsolutePopulation(@Nonnull final EEducationType educationType,
                                      final long totalAmount) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        humanResources.put(educationType, totalAmount);
    }

    public void setAbsoluteCrewRequirement(@Nonnull final EEducationType educationType, final long amount) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        if (amount < 0) {
            if (subType == DEMAND) {
                // if there is no demand to reduce, there just is nothing to reduce
                return;
            }
            throw new NotifyWebUserException("Not below zero, as I told you!");
        }
        humanResources.put(educationType, amount);
    }

    public void updateCrewRequirement(@Nonnull final EEducationType educationType, final long amount) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        final Long currentAmount = humanResources.get(educationType);
        if (currentAmount == null) {
            setAbsoluteCrewRequirement(educationType, amount);
        } else {
            long sum = currentAmount + amount;
            if (sum < 0) {
                sum = 0;
            }
            setAbsoluteCrewRequirement(educationType, sum);
        }
    }

    /**
     * Updates the cloned crew amount with the given addition.
     *
     * @param crew the new crew
     */
    public void updateCrew(@Nonnull final CrewRequirement crew, @Nonnull final ECalculationType calculationType) {
        Preconditions.checkNotNull(crew, "crew shouldn't be null!");
        Preconditions.checkNotNull(calculationType, "calculationType must not be empty");

        Arrays.stream(EEducationType.values()).forEach(educationType -> {
            final long amountToAdd = crew.getCrewAmountByType(educationType);
            if (amountToAdd == 0) {
                return;
            }
            final long currentAmount = getCrewAmountByType(educationType);
            final long newAmount = currentAmount + (calculationType.getMultiplier() * amountToAdd);
            setAbsoluteCrewRequirement(educationType, newAmount);
        });
    }

    public long getCrewAmountByType(@Nonnull final EEducationType educationType) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        final Long amount = humanResources.get(educationType);
        return amount != null ? amount : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceDeposit)) return false;

        ResourceDeposit that = (ResourceDeposit) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id * 31;
    }

    public void add(@Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit must not be empty");

        calculate(resourceDeposit, ECalculationType.ADD);
    }

    private void calculate(@Nonnull final ResourceDeposit resourceDeposit, @Nonnull final ECalculationType calculationType) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit must not be empty");
        Preconditions.checkNotNull(calculationType, "calculationType must not be empty");

        final CrewRequirement crewRequirement = resourceDeposit.getCrewRequirement();
        Arrays.stream(EEducationType.values()).forEach(educationType -> {
            final long amount = crewRequirement.getCrewAmountByType(educationType);
            final long currentAmount = getCrewAmountByType(educationType);
            final long toSet = currentAmount + calculationType.getMultiplier() * amount;
            humanResources.put(educationType, toSet >= 0 ? toSet : 0);
        });
        Arrays.stream(EResourceType.valuesWithoutPopulation()).forEach(eResourceType -> {
            final long amount = resourceDeposit.getResourceAmountByType(eResourceType);
            final long currentAmount = getResourceAmountByType(eResourceType);
            final long toSet = currentAmount + calculationType.getMultiplier() * amount;
            resources.put(eResourceType, toSet >= 0 ? toSet : 0);
        });
    }

    public void subtract(@Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit must not be empty");

        calculate(resourceDeposit, ECalculationType.SUBTRACT);
    }

    /**
     * Create constant conditions for every new player.<br>
     * <br>
     * The main planet must have the same amount of resources.
     */
    public void equalize(final boolean isMain) {
        if (isMain) {
            Arrays.stream(EResourceType.valuesWhichAreCollectable())
                    .forEach(eResourceType -> {
                        if (ETechLevel.TECH_I.getExcludedResources().contains(eResourceType)) {
                            resources.put(eResourceType, 0L);
                        } else {
                            resources.put(eResourceType, 150000L);
                        }
                    });
        }
        Arrays.stream(EResourceType.valuesWhichForfeits()).forEach(eResourceType -> resources.put(eResourceType, 100L));
        Arrays.stream(EEducationType.values()).forEach(eEducationType -> humanResources.put(eEducationType, 0L));
    }

    public boolean isDemandPresent() {
        Preconditions.checkArgument(DEMAND == subType, "I must be a represent a need!");

        final boolean demandForHumans = humanResources.values().stream().anyMatch(amount -> amount > 0);
        final boolean demandForResources = resources.values().stream().anyMatch(amount -> amount > 0);
        return demandForHumans || demandForResources;
    }

    /**
     * The fleet raids all resources which are fitting in the cargo hold.
     */
    public ResourceDeposit raid(@Nonnull final Fleet fleet, long units) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        final ResourceDeposit resourceDeposit = new ResourceDeposit();
        units = raidResource(fleet, CREDITS, resourceDeposit, units);
        units = raidResource(fleet, METALORE, resourceDeposit, units);
        units = raidResource(fleet, RARE_ELEMENTS, resourceDeposit, units);
        raidResource(fleet, HEAVY_METALS, resourceDeposit, units);
        return resourceDeposit;
    }

    private long raidResource(@Nonnull final Fleet fleet, @Nonnull final EResourceType resourceType, @Nonnull final ResourceDeposit resourceDeposit, long units) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit must not be empty");

        if (units <= 0) {
            return units;
        }

        final long amount = getResourceAmountByType(resourceType);
        long take = Long.min(units, amount);
        units -= take;
        setAbsoluteResourceValue(resourceType, amount - take);
        fleet.getResourceDeposit().updateResource(resourceType, take);
        resourceDeposit.setAbsoluteResourceValue(resourceType, take);
        return units;
    }
}
