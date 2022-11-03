package de.yuga.spacebattle.backend.entities.turn.resources;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.enums.EDepositType.CAPACITY;
import static de.yuga.spacebattle.backend.enums.EDepositType.COSTS;
import static de.yuga.spacebattle.backend.enums.EResourceType.POPULATION;

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
// todo @Check(constraints = "humanResources.availableAmount >= humanResources.usedAmount")
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

    public boolean isPopulationSet() {
        return resources.containsKey(POPULATION);
    }

    /**
     * Checks if paying with the parameters is possible.
     *
     * @param resourceType the resource type
     * @param amount       the amount
     * @return <code>true</code> if it can be payed, <code>false</code> otherwise
     */
    public boolean isReducingResourcePossible(@Nonnull final EResourceType resourceType, final long amount) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        return getResourceAmountByType(resourceType) >= amount;
    }

    /**
     * Checks if paying the costs is possible.
     *
     * @param costs the costs
     * @return <code>true</code> if it can be paid, <code>false</code> otherwise
     */
    public PayingPossibleResult isPayingPossible(@Nonnull final ResourceDeposit costs) {
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");
        Preconditions.checkArgument(COSTS == costs.getSubType(), "costs must be flagged as costs!");

        PayingPossibleResult result = new PayingPossibleResult();
        for (final EResourceType resourceType : EResourceType.values()) {
            if (EResourceType.POPULATION == resourceType) {
                final CrewRequirement crewRequirement = costs.getCrewRequirement();
                if (!isReducingPopulationPossible(crewRequirement)) {
                    result.addProblem(resourceType);
                }
            } else if (ECollectableType.COLLECTABLE == resourceType.getCollectableType()) {
                final long debit = costs.getResourceAmountByType(resourceType);
                if (debit > 0 && !isReducingResourcePossible(resourceType, debit)) {
                    result.addProblem(resourceType);
                }
            }
        }
        return result;
    }

    /**
     * Reduces the amount of this by the costs in order to pay a job.
     *
     * @param costs the costs
     */
    public void pay(@Nonnull final ResourceDeposit costs) {
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");
        Preconditions.checkArgument(COSTS == costs.getSubType(), "costs must be flagged as costs!");

        for (final EResourceType resourceType : EResourceType.values()) {
            if (EResourceType.POPULATION == resourceType) {
                // pay crew as normal from deposit
                updateCrew(costs.getCrewRequirement());
            } else if (ECollectableType.COLLECTABLE == resourceType.getCollectableType()) {
                final long debit = costs.getResourceAmountByType(resourceType) * -1;
                updateResource(resourceType, debit);
            }
        }
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
            throw new NotifyWebUserException("No, you cannot do that.");
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

    @Nonnull
    public CrewRequirement getCrewRequirement() {
        return new CrewRequirement(humanResources, subType);
    }

    public void setCrewRequirement(@Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        for (final EEducationType educationType : EEducationType.values()) {
            final long crewAmountByType = crewRequirement.getCrewAmountByType(educationType);
            if (crewAmountByType != 0) {
                humanResources.put(educationType, crewAmountByType);
            }
        }
    }

    /**
     * Will update the population.<br>
     * Will add the given amount to the current amount.<br>
     *
     * @param educationType the education
     * @param totalAmount   the total amount
     */
    public void setAbsolutePopulation(@Nonnull final EEducationType educationType,
                                      final long totalAmount) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        humanResources.put(educationType, totalAmount);
    }


    /**
     * Updates the amount of available and used people on this.<br>
     * On {@link EDepositType#DEPOSITS} it will be added to the available people.<br>
     * On {@link EDepositType#COSTS} it will be reduced from the available people.
     *
     * @param updateByThis the given crew to update or subtract
     * @return if it was successful or not
     */
    public boolean updatePopulation(@Nonnull final CrewRequirement updateByThis) {
        Preconditions.checkNotNull(updateByThis, "updateByThis shouldn't be null!");

        switch (updateByThis.getSubType()) {
            default:
            case DEPOSITS:
                Arrays.stream(EEducationType.values()).forEach(educationType -> {
                    final Long currentAmount = humanResources.get(educationType);
                    final long toAdd = updateByThis.getCrewAmountByType(educationType);
                    if (toAdd != 0) {
                        final long newValue = currentAmount != null ? currentAmount + toAdd : toAdd;
                        humanResources.put(educationType, newValue);
                    }
                });
                return true;
            case COSTS:
                if (!isReducingPopulationPossible(updateByThis)) {
                    return false;
                }
                Arrays.stream(EEducationType.values()).forEach(educationType -> {
                    final Long currentAmount = humanResources.get(educationType);
                    final long toSubtract = updateByThis.getCrewAmountByType(educationType);
                    if (toSubtract != 0) {
                        final long newValue = currentAmount != null ? currentAmount - toSubtract : toSubtract;
                        humanResources.put(educationType, newValue);
                    }
                });
                return true;
        }
    }


    public void setAbsoluteCrewRequirement(@Nonnull final EEducationType educationType, final long amount) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        if (amount < 0) {
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
            setAbsoluteCrewRequirement(educationType, currentAmount + amount);
        }
    }

    /**
     * Updates the cloned crew amount with the given addition.
     *
     * @param crewToAdd the new crew
     */
    public void updateCrew(@Nonnull final CrewRequirement crewToAdd) {
        Preconditions.checkNotNull(crewToAdd, "crewToAdd shouldn't be null!");

        final ECalculationType calculationType = crewToAdd.getSubType().getCalculationType();
        Arrays.stream(EEducationType.values()).forEach(educationType -> {
            final long amountToAdd = crewToAdd.getCrewAmountByType(educationType);
            if (amountToAdd == 0) {
                return;
            }
            final long currentAmount = getCrewAmountByType(educationType);
            final long newAmount = currentAmount + (calculationType.getMultiplier() * amountToAdd);
            this.setAbsoluteCrewRequirement(educationType, newAmount);
        });
    }

    public long getCrewAmountByType(@Nonnull final EEducationType educationType) {
        Preconditions.checkNotNull(educationType, "educationType shouldn't be null!");

        final Long amount = humanResources.get(educationType);
        return amount != null ? amount : 0;
    }

    /**
     * Checks if it is possible to reduce this amount of people in that way.
     *
     * @param reducingBy the amount of people to check
     * @return <code>true</code> if the reduction is possible, <code>false</code> otherwise
     */
    public boolean isReducingPopulationPossible(@Nonnull final CrewRequirement reducingBy) {
        Preconditions.checkNotNull(reducingBy, "reducingBy shouldn't be null!");

        return Arrays.stream(EEducationType.values()).noneMatch(educationType -> {
            final Long currentAmount = humanResources.get(educationType);
            final long reducingByThisAmount = reducingBy.getCrewAmountByType(educationType);
            if (reducingByThisAmount == 0) {
                // no update needed
                return false;
            }
            if (currentAmount == null) {
                // no update possible
                return true;
            }
            // fine if false
            return currentAmount < reducingByThisAmount;
        });
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
    public void equalize() {
        Arrays.stream(EResourceType.valuesWithoutPopulation())
                .forEach(eResourceType -> {
                    if (eResourceType.getCollectableType() == ECollectableType.COLLECTABLE && !ETechLevel.TECH_I.getExcludedResources().contains(eResourceType)) {
                        resources.put(eResourceType, 1000L);
                    } else {
                        resources.put(eResourceType, 0L);
                    }
                });
        Arrays.stream(EEducationType.values()).forEach(eEducationType -> humanResources.put(eEducationType, 0L));
    }
}
