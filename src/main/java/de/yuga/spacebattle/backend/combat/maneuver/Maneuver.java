package de.yuga.spacebattle.backend.combat.maneuver;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.calculator.geometry.DynamicInfo;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.combat.dto.AccelerationProfile;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.dto.MovementAction;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public abstract class Maneuver {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(Maneuver.class);

    @Nonnull
    private final Cage cage;

    @Nonnull
    private final CombatRound start;

    @Nullable
    private CombatRound designatedEnd;

    @Nullable
    private CombatRound end;

    @Nonnull
    private final Fleet agent;

    @Nullable
    private MissileSalvo missileSalvo;

    @Nonnull
    private final KinematicInfo agentsKinematicInitial;

    @Nonnull
    private final KinematicInfo agentsKinematicDesignated;

    @Nonnull
    private final Fleet target;

    @Nonnull
    private final List<CourseOrderElement> courseOrderElements = new ArrayList<>();

    @Nonnull
    private final List<MovementAction> movementActions = new ArrayList<>();

    @Nonnull
    private final ManeuverElements maneuverElements;

    @Nullable
    private Orbit intersectionPoint;

    @Nullable
    private Distance totalLength;

    @Nonnull
    private final List<AccelerationProfile> accelerationProfile = new ArrayList<>();

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

    protected Maneuver(@Nonnull final Cage cage,
                       @Nonnull final CombatRound start,
                       @Nonnull final Fleet agent,
                       @Nonnull final KinematicInfo agentsKinematicInitial,
                       @Nonnull final KinematicInfo agentsKinematicDesignated,
                       @Nonnull final MissileSalvo missileSalvo,
                       @Nonnull final Fleet target) {
        this(cage, start, agent, agentsKinematicInitial, agentsKinematicDesignated, target);

        this.missileSalvo = Preconditions.checkNotNull(missileSalvo, "missileSalvo must not be empty");
    }

    abstract public ManeuverElements calculateCourse();

    @Nullable
    public CourseOrderElement getCourseElement(@Nonnull final CombatRound combatRound) {
        Preconditions.checkNotNull(combatRound, "combatRound shouldn't be null!");

        return getCourseOrderElements().stream().filter(elem -> elem.getCombatRound().equals(combatRound)).findFirst().orElse(null);
    }

    @Nonnull
    public Velocity getAgentsTopSpeed() {
        if (missileSalvo != null) {
            return Velocity.SOL;
        }
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

    @Nullable
    public MissileSalvo getMissileSalvo() {
        return missileSalvo;
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
        return courseOrderElements.stream()
                .sorted(Comparator.comparing(CourseOrderElement::getCombatRound))
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<MovementAction> getMovementActions() {
        return movementActions;
    }

    public void historizeMovementAction(@Nonnull final MovementAction movementAction) {
        Preconditions.checkNotNull(movementAction, "movementAction must not be empty");

        movementActions.add(movementAction);
    }

    @Nonnull
    public ManeuverElements getCourseItems() {
        return maneuverElements;
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

        final double totalLength = maneuverElements.getTotalLength();
        this.totalLength = new Distance(totalLength, EDistanceMetric.KM);

        final List<AccelerationProfile> accelerationProfile =
                NavigationCalculator.createAccelerationProfile(agentsKinematicInitial, agentsKinematicDesignated, this.totalLength, getAgentsTopSpeed());

        /*
            Plan zur Bestimmung von Ort und Zeit zu bestimmter Kampfrunde
            1. Beschleunigungs- und geschwindigkeitsprofil aufbauen - Motionprofile
                - Anfangs- und Endgeschwindigkeit, Länge der Strecke festhalten
                - Werte in Kampfrundenabständen berechnen und festhalten
                - Daraus folgt länge des Kurses und abrufbare Position zu Zeitpunkt x
            2. Aus Beschleunigngsprofil die Streckenlänge definieren
         */

        final CombatRound latestRound = Collections.max(accelerationProfile).getCombatRound();

        final String salvoName = getMissileSalvo() != null ? getMissileSalvo().getUuid().toString() : "";
        final String username = getAgent().getOwner().getUsername();
        cage.logMessage("Maneuver for '" + (StringUtils.isNotEmpty(salvoName) ? " salvo " + salvoName + " from " : "") + username + "' with a total of '" + this.totalLength + "' passed in '" + latestRound + "' rounds.");

        final CombatRound currentCombatRound = cage.getCurrentCombatRound().clone();
        // because its one, not zero-based
        currentCombatRound.previous();

        accelerationProfile.sort(AccelerationProfile::compareTo);
        for (final AccelerationProfile motionProfile : accelerationProfile) {

            final DynamicInfo dynamicInfo = motionProfile.getDynamicInfo();
            final Velocity dynamicInfoVelocity = dynamicInfo.getVelocity();
            final Distance distance = dynamicInfo.getDistance().getInMetricWithScale(EDistanceMetric.KM);

            double percentOfTrack = (distance.divide(totalLength)).multiply(100).getCoordinate().doubleValue();

            // small ugly fix due the fact that you can not accelerate shorter than a combat round
            percentOfTrack = Double.min(100, percentOfTrack);

            final double lengthOnTrack = totalLength * percentOfTrack / 100;
            final ManeuverElement maneuverElement = maneuverElements.getManeuverForPart(percentOfTrack);
            final double[] pointAtLength = maneuverElement.getCurve().getPointAtLength(lengthOnTrack);

            final Orbit position = new Orbit(pointAtLength, EDistanceMetric.KM);

            addCourseOrder(
                    currentCombatRound.add(motionProfile.getCombatRound()),
                    maneuverElement,
                    new Distance(lengthOnTrack, EDistanceMetric.KM), // fixme can be distance directly - test it
                    REDUCE_DISTANCE,
                    dynamicInfoVelocity.clone(),
                    position
            );
            motionProfile.getDynamicInfo().setPosition(position);
        }

        addAccelerationProfile(accelerationProfile);
        setDesignatedEnd(latestRound);
        return this;
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
            cage.logWarning("VELOCITY VIOLATED from fleet " + agent.getOwner().getUsername());
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

    @Nullable
    public CombatRound getIntersectionTimeFor(@Nonnull final Orbit intersectionPoint) {
        Preconditions.checkNotNull(intersectionPoint, "intersectionPoint must not be empty");

        return Collections.min(courseOrderElements, Comparator.comparing(o -> o.getPosition().getDistance(intersectionPoint))).getCombatRound();
    }

    private void addAccelerationProfile(final List<AccelerationProfile> accelerationProfile) {
        Preconditions.checkNotNull(accelerationProfile, "accelerationProfile must not be empty");

        this.accelerationProfile.addAll(accelerationProfile);
    }

    @Nonnull
    public List<AccelerationProfile> getAccelerationProfile() {
        return accelerationProfile;
    }

    public void setIntersectionPoint(@Nullable final Orbit intersectionPoint) {
        this.intersectionPoint = intersectionPoint;
    }

    @Nullable
    public Orbit getIntersectionPoint() {
        return intersectionPoint;
    }

    @Nonnull
    public Distance getTotalLength() {
        return Preconditions.checkNotNull(totalLength, "totalLength must not be empty");
    }
}
