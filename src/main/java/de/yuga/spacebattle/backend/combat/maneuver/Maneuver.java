package de.yuga.spacebattle.backend.combat.maneuver;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.combat.dto.MovementAction;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static de.yuga.spacebattle.backend.combat.enums.EMovementType.REDUCE_DISTANCE;

/**
 * fixme macht das sinn?
 * - kurs wird berechnet aus
 *      - anfangszustand
 *      - endzustand
 *      - kursvorschrift
 *      - maximalgeschwindigkeit
 *      - todo add beharrungskräfte like impuls
 * - gibt es eine abbruchsvorschrift? z.b. verluste
 */
public abstract class Maneuver implements Cloneable {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(Maneuver.class);

    @Nonnull
    private final Cage cage;

    @Nonnull
    private CombatRound start;

    @Nullable
    private CombatRound designatedEnd;

    @Nullable
    private CombatRound end;

    @Nonnull
    private final Fleet agent;

    @Nonnull
    private KinematicInfo agentsKinematicInitial;

    @Nonnull
    private KinematicInfo agentsKinematicDesignated;

    @Nonnull
    private final Fleet target;

    @Nonnull
    private final List<CourseOrderElement> courseOrderElements = new ArrayList<>();

    @Nonnull
    private final List<MovementAction> movementActions = new ArrayList<>();

    @Nonnull
    private final ManeuverElements maneuverElements;

    protected Maneuver(@Nonnull final Cage cage,
                       @Nonnull final CombatRound start,
                       @Nonnull final Fleet agent,
                       @Nonnull final KinematicInfo agentsKinematicInitial,
                       @Nonnull final KinematicInfo agentsKinematicDesignated,
                       @Nonnull final Fleet target) {
        this.cage = Preconditions.checkNotNull(cage, "cage must not be empty");
        this.start = Preconditions.checkNotNull(start, "start must not be empty").clone();
        this.agent = Preconditions.checkNotNull(agent, "agent must not be empty");
        this.agentsKinematicInitial = Preconditions.checkNotNull(agentsKinematicInitial, "agentsKinematicInitial must not be empty").clone();
        this.agentsKinematicDesignated = Preconditions.checkNotNull(agentsKinematicDesignated, "agentsKinematicDesignated must not be empty").clone();
        this.target = Preconditions.checkNotNull(target, "target must not be empty");

        this.maneuverElements = this.calculateCourse();
    }

    abstract public ManeuverElements calculateCourse();

    @Nonnull
    public Velocity getAgentsTopSpeed() {
        return Velocity.SOL.multiply(BigDecimal.valueOf(agent.getRestrictingTechnologyType().getMaxVelocitySOL()));
    }

    @Nonnull
    public Cage getCage() {
        return cage;
    }

    @Nonnull
    public CombatRound getStart() {
        return start;
    }

    @Nonnull
    public CombatRound getDesignatedEnd() {
        return Preconditions.checkNotNull(designatedEnd, "designatedEnd must not be empty");
    }

    public void setDesignatedEnd(@Nonnull final CombatRound designatedEnd) {
        this.designatedEnd = Preconditions.checkNotNull(designatedEnd, "designatedEnd must not be empty").clone();
    }

    public void setEnd(@Nonnull final CombatRound end) {
        this.end = Preconditions.checkNotNull(end, "end must not be empty").clone();
    }

    @Nonnull
    public CombatRound getEnd() {
        return Preconditions.checkNotNull(end, "end must not be empty");
    }

    public boolean isValid() {
        return end != null;
    }

    @Nonnull
    public Fleet getAgent() {
        return agent;
    }

    @Nonnull
    public KinematicInfo getAgentsKinematicInitial() {
        return agentsKinematicInitial;
    }

    @Nonnull
    public KinematicInfo getAgentsKinematicDesignated() {
        return agentsKinematicDesignated;
    }

    @Nonnull
    public Fleet getTarget() {
        return target;
    }

    @Nonnull
    public List<CourseOrderElement> getCourseOrderElements() {
        return courseOrderElements;
    }

    @Nonnull
    public List<MovementAction> getMovementActions() {
        return movementActions;
    }

    public void addMovementAction(@Nonnull final MovementAction movementAction) {
        Preconditions.checkNotNull(movementAction, "movementAction must not be empty");

        movementActions.add(movementAction);
    }

    @Nonnull
    public ManeuverElements getCourseItems() {
        return maneuverElements;
    }

    @Override
    public Maneuver clone() {
        try {
            final Maneuver clone = (Maneuver) super.clone();
            clone.agentsKinematicInitial = agentsKinematicInitial.clone();
            clone.agentsKinematicDesignated = agentsKinematicDesignated.clone();
            clone.start = start.clone();
            clone.end = end != null ? end.clone() : null;
            clone.designatedEnd = designatedEnd != null ? designatedEnd.clone() : null;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Maneuver maneuver = (Maneuver) o;

        return new EqualsBuilder().append(start, maneuver.start).append(agent, maneuver.agent).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(start).append(agent).toHashCode();
    }

    @Nonnull
    public String getManeuverName() {
        return this.getClass().getSimpleName();
    }

    @Nonnull
    public ManeuverElements getManeuverElements() {
        return maneuverElements;
    }

    @Nonnull
    public Maneuver createCoursePlot() {
        courseOrderElements.clear();
        final CombatRound combatRound = getStart().clone();

        final boolean isAggressor = cage.getAggressor().equals(agent);

        final double totalLength = maneuverElements.getTotalLength();
        Velocity velocity = Velocity.ZERO.clone();
        final Acceleration acceleration = agentsKinematicInitial.getAcceleration();


        /*
            fixme Plan zur Bestimmung von Ort und Zeit zu bestimmter Kampfrunde
            1. Beschleunigungs- und geschwindigkeitsprofil aufbauen - Motionprofile
                - Anfangs- und Endgeschwindigkeit, Länge der Strecke festhalten
                - Werte in Kampfrundenabständen berechnen und festhalten
                - Daraus folgt länge des Kurses und abrufbare Position zu Zeitpunkt x
         */


        final int timeToPassTotalLength = getLengthInCombatRounds(totalLength, acceleration);
        cage.logMessage("Maneuver for '" + getAgent().getOwner().getUsername() + "' with a total length of '" + totalLength + "' passed in '" + timeToPassTotalLength + "' rounds.");

        for (int i = 1; i <= timeToPassTotalLength; i++) {

            final double percentOfTrack = (((double) i / timeToPassTotalLength) * 100);
            velocity = velocity.getVelocityByAcceleration(acceleration, CombatRound.COMBAT_ROUND);
            cage.logMessagePlain("Maneuver for '" + getAgent().getOwner().getUsername()
                    + "' with velocity of '" + velocity.getInMetricWithScale(EDistanceMetric.M, ETimeMetric.SECOND)
                    + "' and acceleration of '" + acceleration.getCoordinateInMetric(EAccelerationMetric.G)
                    + "' in round '" + i + "'");

            final double lengthOnTrack = totalLength * percentOfTrack / 100;
            final ManeuverElement maneuverElement = maneuverElements.getManeuverForPart(percentOfTrack);
            final double[] pointAtLength = maneuverElement.getCurve().getPointAtLength(lengthOnTrack);

            addCourseOrder(
                    combatRound.clone(),
                    maneuverElement,
                    new Distance(lengthOnTrack, EDistanceMetric.KM),
                    REDUCE_DISTANCE,
                    velocity.clone(),
                    new Orbit(pointAtLength, EDistanceMetric.KM)
            );
            combatRound.next();
        }

        setDesignatedEnd(combatRound);
        return this;
    }

    private static int getLengthInCombatRounds(final double totalLength, @Nonnull final Velocity velocity) {
        Preconditions.checkNotNull(velocity, "velocity must not be empty");

        return new Distance(totalLength, EDistanceMetric.KM).calculateTimeToPass(velocity).convertToMetric(CombatRound.COMBAT_ROUND_METRIC).getCoordinate().intValue();
    }

    private static int getLengthInCombatRounds(final double totalLength, @Nonnull final Acceleration acceleration) {
        Preconditions.checkNotNull(acceleration, "acceleration must not be empty");

        return new Distance(totalLength, EDistanceMetric.KM).calculateTimeToPass(acceleration).convertToMetric(CombatRound.COMBAT_ROUND_METRIC).getCoordinate().intValue();
    }

    public void addCourseOrder(@Nonnull final CombatRound combatRound,
                               @Nonnull final ManeuverElement maneuverElement,
                               @Nonnull final Distance lengthOnTrack,
                               @Nonnull final EMovementType movementType,
                               @Nonnull final Velocity velocity,
                               @Nonnull final Orbit position) {
        Preconditions.checkNotNull(combatRound, "combatRound shouldn't be null!");
        Preconditions.checkNotNull(maneuverElement, "maneuverElement must not be empty");
        Preconditions.checkNotNull(lengthOnTrack, "lengthOnTrack must not be empty");
        Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");
        Preconditions.checkNotNull(velocity, "velocity shouldn't be null!");
        Preconditions.checkNotNull(position, "position shouldn't be null!");

        if (hasViolatedTopSpeed(velocity)) {
            cage.logWarning("VELOCITY VIOLATED from fleet " + agent.getId());
        }

        courseOrderElements.add(new CourseOrderElement(this, maneuverElement, lengthOnTrack, combatRound, movementType, velocity, position));
    }

    public boolean hasViolatedTopSpeed(@Nonnull final Velocity velocity) {
        Preconditions.checkNotNull(velocity, "velocity must not be empty");

        final Velocity topSpeed = getAgentsTopSpeed();
        return velocity.compareTo(topSpeed) > 0;
    }

    @Nonnull
    public CubicBezier getCombatElement() {
        // the idea is that the latest element is probably the one for combat - earlier elements are for positioning purposes
        return maneuverElements.getManeuverElements().stream()
                .reduce((o1, o2) -> o1.compareTo(o2) < 0 ? o1 : o2)
                .orElseThrow(() -> new NotifyWebUserException("There is no maneuver element with a sequence number."))
                .getCurve();
    }

    @Nonnull
    protected static ManeuverElements getAsList(@Nonnull final CubicBezier cubicBezier) {
        Preconditions.checkNotNull(cubicBezier, "cubicBezier must not be empty");

        final ManeuverElements result = new ManeuverElements();
        result.set(cubicBezier);
        return result;
    }

    @Nonnull
    public Maneuver withTransferCourse(@Nonnull final CubicBezier cubicBezier) {
        Preconditions.checkNotNull(cubicBezier, "cubicBezier must not be empty");

        maneuverElements.withTransferManeuver(cubicBezier);
        return this;
    }
}
