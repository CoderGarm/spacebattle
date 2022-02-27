package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Entity
@Table(name = "missile")
@AttributeOverride(name = "id", column = @Column(name = "idMissile"))
public class Missile extends AbstractEntityKey {

    @Nonnull
    @NotNull
    private String typeName;

    @Column(nullable = false)
    private int warheadCapacity; // todo validate warhead capacity

    @Column(nullable = false)
    private int motorCapacity; // todo validate motor capacity

    /**
     * The resistance against electronic counter measures.
     */
    @Column(nullable = false)
    private int elokaResistance;

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idWarhead")
    private Warhead warhead;

    @Nonnull
    @Size(min = 1, max = 3)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "missileMotors",
            joinColumns = @JoinColumn(name = "idMissile", referencedColumnName = "idMissile"),
            inverseJoinColumns = @JoinColumn(name = "idMissileMotor", referencedColumnName = "idMissileMotor")
    )
    private List<MissileMotor> missileMotors;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = ResourceDepositInitializerCalculator.initializeResourceDeposit(Missile.class, EDepositType.COSTS);

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    /**
     * An empty ammunition module means that the weapon needs no ammunition.
     */
    @Nonnull
    @NotNull
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "idAmmunitionModule")
    private AmmunitionModule ammunitionModule;

    /**
     * Quick performance
     */
    @Nullable
    @Transient
    private Distance maxRange = null;

    public Missile() {
    }

    public Missile(@Nonnull final String typeName,
                   final int warheadCapacity,
                   final int motorCapacity,
                   final int elokaResistance,
                   @Nonnull final Warhead warhead,
                   @Nonnull final List<MissileMotor> missileMotors,
                   @Nonnull final Research unlockedThrough,
                   @Nonnull final AmmunitionModule ammunitionModule) {
        Preconditions.checkNotNull(typeName, "typeName shouldn't be null!");
        Preconditions.checkNotNull(warhead, "warhead shouldn't be null!");
        Preconditions.checkNotNull(missileMotors, "missileMotors shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");

        this.typeName = typeName;
        this.warheadCapacity = warheadCapacity;
        this.motorCapacity = motorCapacity;
        this.elokaResistance = elokaResistance;
        this.warhead = warhead;
        this.missileMotors = missileMotors;
        this.unlockedThrough = unlockedThrough;
        this.ammunitionModule = ammunitionModule;
    }

    @Nonnull
    public String getTypeName() {
        return typeName;
    }

    public int getWarheadCapacity() {
        return warheadCapacity;
    }

    public int getMotorCapacity() {
        return motorCapacity;
    }

    public int getElokaResistance() {
        return elokaResistance;
    }

    @Nonnull
    public Warhead getWarhead() {
        return warhead;
    }

    @Nonnull
    public List<MissileMotor> getMissileMotors() {
        return missileMotors;
    }

    /**
     * Returns the costs of the pure missile body. Neither the motors nor the warhead are included.
     *
     * @return the costs
     */
    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    /**
     * Will calculate and return the full costs of this missile.
     *
     * @return the total costs
     */
    @Nonnull
    public ResourceDeposit getCostsOverall() {
        final ResourceDeposit clonedDeposit = new ResourceDeposit(EDepositType.COSTS);

        updateCosts(clonedDeposit, costs);
        updateCosts(clonedDeposit, warhead.getCosts());
        missileMotors.forEach(s -> updateCosts(clonedDeposit, s.getCosts()));
        return clonedDeposit;
    }

    /**
     * Calculates and sets the costs by a possible existent support module.
     *
     * @param resultingDeposit the deposit to update
     * @param costsToAdd       the resource map
     */
    private void updateCosts(@Nonnull final ResourceDeposit resultingDeposit,
                             @Nonnull final ResourceDeposit costsToAdd) {
        Preconditions.checkNotNull(resultingDeposit, "resultingDeposit shouldn't be null!");
        Preconditions.checkNotNull(costsToAdd, "costsToAdd shouldn't be null!");

        for (final EResourceType resourceType : EResourceType.values()) {
            if (resourceType == EResourceType.POPULATION) {
                resultingDeposit.updatePopulation(costsToAdd.getCrewRequirement().toggleToDepositMode());
            } else {
                resultingDeposit.updateResource(resourceType, costsToAdd.getResourceAmountByType(resourceType));
            }
        }
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }

    @Nonnull
    public AmmunitionModule getAmmunitionModule() {
        return ammunitionModule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Missile)) return false;

        Missile missile = (Missile) o;

        return new EqualsBuilder().append(id, missile.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    /**
     * Returns the range over the complete endurance.
     *
     * @return the distance which will be covered under drive, in meter
     */
    @Nonnull
    public Distance getMaximumMissileRange() {
        if (maxRange != null) {
            return maxRange;
        }
        final AtomicReference<Distance> range = new AtomicReference<>(Distance.ZERO);
        getMissileMotors().forEach(missileMotor -> {
            final int endurance = missileMotor.getEndurance();
            final Acceleration acceleration = missileMotor.getAcceleration();
            final Distance currentDistance = range.get();
            final Distance additionalRange = NavigationCalculator.getRangeByTimeAndAcceleration(endurance, acceleration);
            additionalRange.convertToMetric(currentDistance.getDistanceMetric());
            range.set(currentDistance.add(additionalRange));
        });
        this.maxRange = range.get();
        return maxRange;
    }

    /**
     * Returns the range which can be covered by this missile in a combat round.
     *
     * @return the distance which will be covered under drive, in meter
     */
    @Nonnull
    public Distance getRangePerCombatRound() {
        final AtomicReference<Distance> range = new AtomicReference<>(Distance.ZERO);
        missileMotors.forEach(missileMotor -> {
            final int endurance = CombatRound.COMBAT_ROUND_DURATION;
            final Acceleration acceleration = missileMotor.getAcceleration();
            final Distance currentRange = range.get();
            final Distance additionalRange = NavigationCalculator.getRangeByTimeAndAcceleration(endurance, acceleration);
            additionalRange.convertToMetric(currentRange.getDistanceMetric());
            range.set(currentRange.add(additionalRange));
        });
        return range.get();
    }
}
