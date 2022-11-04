package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Time;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "missile")
@AttributeOverride(name = "id", column = @Column(name = "idMissile"))
public class Missile extends HasCosts {

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
    @ManyToOne(optional = false)
    @JoinColumn(name = "idMissileMotor")
    private MissileMotor missileMotor;

    /**
     * The amounts of {@link #missileMotor} which are installed on the missile.
     */
    private int motorAmount;

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

    /**
     * Which is the targeted ship's hull class.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EHullType hullType;

    public Missile() {
    }

    public Missile(@Nonnull final String name,
                   @Nonnull final String description,
                   final int warheadCapacity,
                   final int motorCapacity,
                   final int elokaResistance,
                   @Nonnull final EHullType hullType,
                   @Nonnull final ETechLevel techLevel,
                   @Nonnull final Warhead warhead,
                   @Nonnull final List<MissileMotor> missileMotors,
                   @Nonnull final Research unlockedThrough,
                   @Nonnull final AmmunitionModule ammunitionModule) {
        super(new Translation(Translation.DEFAULT_LANGUAGE, name), new Translation(Translation.DEFAULT_LANGUAGE, description), techLevel, motorCapacity + warheadCapacity, Missile.class);
        Preconditions.checkNotNull(warhead, "warhead shouldn't be null!");
        Preconditions.checkNotNull(missileMotors, "missileMotors shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");
        Preconditions.checkNotNull(hullType, "hullType must not be empty");

        this.warheadCapacity = warheadCapacity;
        this.motorCapacity = motorCapacity;
        this.elokaResistance = elokaResistance;
        this.warhead = warhead;
        this.missileMotor = missileMotors.get(0);
        this.motorAmount = missileMotors.size(); // todo repair the methods away from list - to lazy currently
        this.unlockedThrough = unlockedThrough;
        this.ammunitionModule = ammunitionModule;
        this.hullType = hullType;
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
    public MissileMotor getMissileMotor() {
        return missileMotor;
    }

    public int getMotorAmount() {
        return motorAmount;
    }


    @Nonnull
    public EHullType getHullType() {
        return hullType;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setHullType(@Nonnull final EHullType hullType) {
        this.hullType = hullType;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setWarheadCapacity(final int warheadCapacity) {
        this.warheadCapacity = warheadCapacity;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setMotorCapacity(final int motorCapacity) {
        this.motorCapacity = motorCapacity;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setElokaResistance(final int elokaResistance) {
        this.elokaResistance = elokaResistance;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setWarhead(@Nonnull final Warhead warhead) {
        this.warhead = warhead;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setMissileMotor(@Nonnull final MissileMotor missileMotor) {
        this.missileMotor = missileMotor;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setMotorAmount(final int motorAmount) {
        this.motorAmount = motorAmount;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setAmmunitionModule(@Nonnull final AmmunitionModule ammunitionModule) {
        this.ammunitionModule = ammunitionModule;
    }

    /**
     * Will calculate and return the full costs of this missile.
     *
     * @return the total costs
     */
    @Nonnull
    public ResourceDeposit getCostsOverall() {
        final ResourceDeposit clonedDeposit = new ResourceDeposit(EDepositType.COSTS);

        updateCosts(clonedDeposit, getCosts());
        updateCosts(clonedDeposit, warhead.getCosts());
        for (int i = 1; i <= motorAmount; i++) {
            updateCosts(clonedDeposit, missileMotor.getCosts());
        }
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

        final int endurance = missileMotor.getEndurance() * motorAmount;
        final Acceleration acceleration = missileMotor.getAcceleration();
        maxRange = acceleration.getDistanceByTime(new Time(endurance, ETimeMetric.SECOND), Velocity.ZERO, EDistanceMetric.LS);
        return maxRange;
    }

    /**
     * Returns the range which can be covered by this missile in a combat round.
     *
     * @return the distance which will be covered under drive, in meter
     */
    @Nonnull
    public Distance getRangePerCombatRound() {
        final int endurance = CombatRound.COMBAT_ROUND_DURATION;
        final Acceleration acceleration = missileMotor.getAcceleration();
        return acceleration.getDistanceByTime(new Time(endurance, ETimeMetric.SECOND), Velocity.ZERO, EDistanceMetric.LS);
    }
}
