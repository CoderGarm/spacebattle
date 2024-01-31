package de.yuga.spacebattle.backend.combat.maneuver;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private ManeuverElements maneuverElements;

    protected Maneuver(@Nonnull final Cage cage,
                    @Nonnull final CombatRound start,
                    @Nonnull final Fleet agent,
                    @Nonnull final KinematicInfo agentsKinematicInitial,
                    @Nonnull final KinematicInfo agentsKinematicDesignated,
                    @Nonnull final Fleet target) {
        this.cage = Preconditions.checkNotNull(cage, "cage must not be empty");
        this.start = Preconditions.checkNotNull(start, "start must not be empty").clone();
        this.agent = Preconditions.checkNotNull(agent, "agent must not be empty");
        this.agentsKinematicInitial = Preconditions.checkNotNull(agentsKinematicInitial, "agentsKinematicInitial must not be empty");
        this.agentsKinematicDesignated = Preconditions.checkNotNull(agentsKinematicDesignated, "agentsKinematicDesignated must not be empty");
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

    @Nullable
    public CombatRound getDesignatedEnd() {
        return designatedEnd;
    }

    @Nullable
    public CombatRound getEnd() {
        return end;
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
    public ManeuverElements getCourseItems() {
        return maneuverElements;
    }

    public void setDesignatedEnd(@Nonnull final CombatRound designatedEnd) {
        this.designatedEnd = Preconditions.checkNotNull(designatedEnd, "designatedEnd must not be empty").clone();
    }

    public void setEnd(@Nonnull final CombatRound end) {
        this.end = Preconditions.checkNotNull(end, "end must not be empty").clone();
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
            clone.maneuverElements = maneuverElements.clone();
            clone.courseOrderElements.addAll(courseOrderElements.stream().map(CourseOrderElement::clone).collect(Collectors.toList()));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Nonnull
    public String getManeuverName() {
        return this.getClass().getSimpleName();
    }

    @Nonnull
    public Maneuver createCoursePlot() {
        courseOrderElements.clear();
        final CombatRound combatRound = getStart().clone();

        final double totalLength = maneuverElements.getTotalLength();
        final Velocity maxVelocity = getAgentsTopSpeed();

        final int timeToPassTotalLength = getLengthInCombatRounds(totalLength, maxVelocity);
        LOGGER.info("Maneuver for '{}' with a total length of '{}' passed in '{}' rounds.", getAgent().getOwner().getUsername(), totalLength, timeToPassTotalLength);

        for (int i = 1; i <= timeToPassTotalLength; i++) {

            final int percentOfTrack = (int) (((double) i / timeToPassTotalLength) * 100);
            final double lengthOnTrack = totalLength * percentOfTrack / 100;
            final ManeuverElement maneuverElement = maneuverElements.getManeuverForPart(percentOfTrack);
            final double[] pointAtLength = maneuverElement.getCurve().getPointAtLength(lengthOnTrack);
            addCourseOrder(
                    combatRound.clone(),
                    REDUCE_DISTANCE,
                    maxVelocity.multiply(0.01).clone(),
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

    public void addCourseOrder(@Nonnull final CombatRound combatRound,
                               @Nonnull final EMovementType movementType,
                               @Nonnull final Velocity velocity,
                               @Nonnull final Orbit destination) {
        Preconditions.checkNotNull(combatRound, "combatRound shouldn't be null!");
        Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");
        Preconditions.checkNotNull(velocity, "velocity shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        if (hasViolatedTopSpeed(velocity)) {
            cage.logWarning("VELOCITY VIOLATED from fleet " + agent.getId());
        }

        courseOrderElements.add(new CourseOrderElement(combatRound, movementType, velocity, destination));
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
