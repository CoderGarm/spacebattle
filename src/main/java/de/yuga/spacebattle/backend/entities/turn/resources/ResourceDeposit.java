package de.yuga.spacebattle.backend.entities.turn.resources;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.enums.ECollectableType;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static de.yuga.spacebattle.backend.enums.EResourceType.POPULATION;

/**
 * The resource deposit itself represents a various uses, compare {@link EDepositType}.
 */
@NamedQueries({
        @NamedQuery(name = "ResourceDeposit.getAll", query = "SELECT p FROM ResourceDeposit p")
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
    @NotNull(message = "SubType must be defined.")
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
     * In case of {@link EResourceType#POPULATION} it will return the total available amount of every {@link EEducationType}.
     *
     * @param resourceType the resource type
     * @return the amount
     */
    public long getResourceAmountByType(@Nullable final EResourceType resourceType) {
        if (POPULATION == resourceType) {
            // just sum up the total of all kinds
            return humanResources.values().stream()
                    .filter(Objects::nonNull)
                    .mapToLong(Long::longValue).sum();

        }
        if (resources.containsKey(resourceType)) {
            return this.resources.get(resourceType);
        }
        return 0;
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
     * @return <code>true</code> if it can be payed, <code>false</code> otherwise
     */
    public boolean isPayingPossible(@Nonnull final ResourceDeposit costs) {
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");
        Preconditions.checkArgument(EDepositType.COSTS == costs.getSubType(), "costs must be flagged as costs!");

        for (final EResourceType resourceType : EResourceType.values()) {
            if (EResourceType.POPULATION == resourceType) {
                final CrewRequirementDTO crewRequirement = costs.getCrewRequirement();
                if (!getCrewRequirement().isReducingPopulationPossible(crewRequirement)) {
                    return false;
                }
            } else if (ECollectableType.COLLECTABLE == resourceType.getCollectableType()) {
                final long debit = costs.getResourceAmountByType(resourceType);
                if (!isReducingResourcePossible(resourceType, debit)) {
                    return false;
                }
            }
        }
        return true;
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

        if (POPULATION == resourceType) {
            return;
        }
        final long value;
        if (resources.containsKey(resourceType)) {
            value = this.resources.get(resourceType) + amount;
        } else {
            value = amount;
        }
        if (value < 0) {
            throw new NotifySBUserException("No, you cannot do that.");
        }
        this.resources.put(resourceType, value);
    }


    /**
     * Sets an absolute value to the resource type. There is no validation inside so it can be below zero.<br>
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
        this.resources.put(resourceType, amount);
    }

    @Nonnull
    public CrewRequirementDTO getCrewRequirement() {
        return new CrewRequirementDTO(humanResources, subType);
    }

    @Deprecated(since = "productive")
    public void setCrewRequirement(@Nonnull final CrewRequirementDTO crewRequirement) {
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
        Preconditions.checkArgument(totalAmount > 0, "totalAmount shouldn't be negative!");

        this.humanResources.put(educationType, totalAmount);
    }


    /**
     * Updates the amount of available and used people on this.<br>
     * On {@link EDepositType#DEPOSITS} it will be added to the available people.<br>
     * On {@link EDepositType#COSTS} it will be reduced from the available people.
     *
     * @param updateByThis the given crew to update or subtract
     * @return if it was successful or not
     */
    public boolean updatePopulation(@Nonnull final CrewRequirementDTO updateByThis) {
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
                if (!getCrewRequirement().isReducingPopulationPossible(updateByThis)) {
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
}
