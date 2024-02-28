package de.yuga.spacebattle.backend.entities.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.dto.physics.*;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByOwn;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

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
     * @return the distance which will be covered under drive
     */
    @Nonnull
    public Distance getMaximumMissileRange() {

        final int endurance = missileMotor.getEndurance();
        final Acceleration acceleration = missileMotor.getAcceleration();
        return acceleration.getDistanceByTime(new Time(endurance, ETimeMetric.SECOND), Velocity.ZERO, EDistanceMetric.LS);
    }

    /**
     * Returns the range over the complete endurance.<br>
     * The base velocity must be the resulting velocity towards the target.
     *
     * @return the distance which will be covered under drive
     */
    @Nonnull
    public Distance getMaximumMissileRange(@Nonnull final Velocity baseVelocity) {
        Preconditions.checkNotNull(baseVelocity, "baseVelocity must not be empty");

        final int endurance = missileMotor.getEndurance();
        final Acceleration acceleration = missileMotor.getAcceleration();
        return acceleration.getDistanceByTime(new Time(endurance, ETimeMetric.SECOND), baseVelocity, EDistanceMetric.LS);
    }

    @Nonnull
    public Distance getRangeOverEndurance(@Nonnull final Velocity initialVelocity, final int endurance) {
        Preconditions.checkNotNull(initialVelocity, "initialVelocity must not be empty");

        final Acceleration acceleration = missileMotor.getAcceleration();
        return acceleration.getDistanceByTime(new Time(endurance, ETimeMetric.SECOND), initialVelocity, EDistanceMetric.LS);
    }

    @Nonnull
    public Distance getMaximumMissileRange(@Nonnull final FleetRoundState actorsState, @Nonnull final FleetRoundState targetsState) {
        Preconditions.checkNotNull(actorsState, "actorsState must not be empty");
        Preconditions.checkNotNull(targetsState, "targetsState must not be empty");


        /*
            reichweite besteht aus

                1. reichweite = endurance und beschleuniung
                2. geschwindigkeitsvektor der plattform
                3. geschwindigkeitvektor des ziels

                - geschwindigkeitsvektoren zerlegen → geschwindigkeitsskalar in resultierende richtung
                - effektive reichweite = geschwindigkeitsskalar + reichweite
         */

        final Velocity velocity = actorsState.getVelocity();
        final Direction actorsDirection = actorsState.getDirection();
        final Direction targetsDirection = targetsState.getDirection();

        final double angleBetween = actorsDirection.getAngleBetween(targetsDirection); // todo unbedingt abtesten
        final double cos = Math.cos(Math.toRadians(angleBetween));

        final BigDecimal c = velocity.getValue();
        final BigDecimal b = targetsState.getVelocity().getCoordinateInMetric(velocity.getDistanceMetric(), velocity.getTimeMetric());

        // KOSINUSSATZ
        final BigDecimal vectorVelocity = b.pow(2)
                .add(c.pow(2))
                .subtract(BigDecimal.valueOf(2)
                        .multiply(b)
                        .multiply(c)
                        .multiply(BigDecimal.valueOf(cos)))
                .sqrt(DistanceCalculator.MC_HU);
        final Velocity resultingVelocity = new Velocity(vectorVelocity, velocity.getDistanceMetric(), velocity.getTimeMetric());

        Distance missileRange = getMaximumMissileRange(resultingVelocity);

        final Time time = missileRange.calculateTimeToPass(missileMotor.getAcceleration(), Velocity.SOL);
        final int roundsToHit = time.getCoordinateInMetric(ETimeMetric.SECOND)
                .divide(CombatRound.COMBAT_ROUND.getCoordinateInMetric(ETimeMetric.SECOND), DistanceCalculator.MC_HU).intValue();

        final CombatRound hitTime = actorsState.getCage().getCurrentCombatRound().add(roundsToHit);

        Distance targetMovement = Distance.ZERO;
        final CourseOrderElement courseElement = targetsState.getCoursePlot().getCourseElement(hitTime);
        if (courseElement != null) {
            targetMovement = courseElement.getPosition().getDistance(targetsState.getPosition());
            missileRange = missileRange.add(targetMovement);
        }


        final Distance rangeUnderDrive = getMaximumMissileRange();

        actorsState.getCage().logMessage("MISSILE RANGE CALC: combined fleet velocity '"
                + resultingVelocity + "' KM/S on angle '" + angleBetween + "'"
                + " with effective missile range of '" + missileRange + "' from range under drive with '" + rangeUnderDrive + "'"
                + " with target movement of '" + targetMovement + "'.");

        return missileRange;
    }

    @Nonnull
    public Distance getRearMissileRange(@Nonnull final FleetRoundState actorsState) {
        Preconditions.checkNotNull(actorsState, "actorsState must not be empty");

        final Velocity velocity = actorsState.getVelocity();

        final double angleBetween = -180;
        final double cos = Math.cos(Math.toRadians(angleBetween));

        final BigDecimal c = velocity.getValue();

        // KOSINUSSATZ
        final BigDecimal vectorVelocity = c.pow(2)
                .subtract(BigDecimal.valueOf(2)
                        .multiply(c)
                        .multiply(BigDecimal.valueOf(cos)))
                .sqrt(DistanceCalculator.MC_HU);
        final Velocity resultingVelocity = new Velocity(vectorVelocity, velocity.getDistanceMetric(), velocity.getTimeMetric());

        final Distance missileRange = getMaximumMissileRange(resultingVelocity);

        //actorsState.getCage().logMessage("MISSILE RANGE CALC: combined fleet velocity '" + resultingVelocity + "' KM/S on angle '" + angleBetween + "' with missile range of '" + missileRange + "'.");

        // todo create segmented information: 'range under drive' with 'additional range by base movements'

        return missileRange;
    }
}
