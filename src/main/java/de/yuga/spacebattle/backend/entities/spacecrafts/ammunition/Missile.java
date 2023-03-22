package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Time;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByOwn;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "Missile.getAll", query = "SELECT a FROM Missile a"),
        @NamedQuery(name = "Missile.getAllByResearches", query = "SELECT a FROM Missile a LEFT JOIN ResearchLevel rl ON (rl.research = a.namedTechLevel.unlockedThrough AND rl.user.id = :idUser) WHERE rl IS NOT NULL AND rl.level >= a.unlockedThroughLevel")
})
@Entity
@Table(name = "missile")
@AttributeOverride(name = "id", column = @Column(name = "idMissile"))
@AttributeOverride(name = "effectValue", column = @Column(name = "elokaResistance"))
public class Missile extends HasCostsByOwn {

    @Nonnull
    @NotNull
    @Embedded
    private Warhead warhead;

    @Nonnull
    @NotNull
    @Embedded
    private MissileMotor missileMotor;

    /**
     * Quick performance
     */
    @Nullable
    @Transient
    private Distance maxRange = null;

    public Missile() {
    }

    public Missile(@Nonnull final NamedTechLevel baseModule,
                   @Nonnull final String technicalTypeName,
                   final int unlockedThroughLevel,
                   final int elokaResistance,
                   final int tonnage,
                   @Nonnull final EShipClassType shipClassType,
                   @Nonnull final Warhead warhead,
                   @Nonnull final MissileMotor missileMotor) {
        super(baseModule, technicalTypeName, unlockedThroughLevel, tonnage, elokaResistance, shipClassType);
        Preconditions.checkNotNull(warhead, "warhead shouldn't be null!");
        Preconditions.checkNotNull(missileMotor, "missileMotor shouldn't be null!");

        this.warhead = warhead;
        this.missileMotor = missileMotor;
    }

    public int getElokaResistance() {
        return getEffectValue();
    }

    public long getDamageValue() {
        return warhead.getDamageValue();
    }

    @Nonnull
    public Warhead getWarhead() {
        return warhead;
    }

    @Nonnull
    public MissileMotor getMissileMotor() {
        return missileMotor;
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

        final int endurance = missileMotor.getEndurance();
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
