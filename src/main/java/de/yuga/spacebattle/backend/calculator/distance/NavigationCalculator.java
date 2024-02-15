package de.yuga.spacebattle.backend.calculator.distance;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.MathHelper;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.combat.dto.AccelerationProfile;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Time;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EStarClassType;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator.MC_HU;

public class NavigationCalculator {

    private NavigationCalculator() {
    }

    @Nonnull
    public static Orbit getPositionOnHyperlimit(@Nonnull final FleetOrbit destination) {
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkArgument(destination.getSystem() != null, "destination system shouldn't be null!");
        Preconditions.checkArgument(destination.getInterplanetaryResultingOrbit() != null, "destination resulting orbit shouldn't be null!");

        final Quadrant quadrant = Quadrant.getByOrbit(destination.getInterplanetaryResultingOrbit());
        final EStarClassType starClassType = destination.getSystem().getStarClassType();
        final double radiusOfHyperLimit = starClassType.getLightMinutesToHyperLimit();

        return DistanceCalculator.createByRadiusAndQuadrant(new Distance(radiusOfHyperLimit, EDistanceMetric.LM), quadrant, Planet.PLANET_STANDARD_METRIC);
    }

    /**
     * Calculates the needed time to lay back the given distance.<br>
     * The speed at the end of the journey is defined the given percentage value of the top speed.<br>
     * <br>
     * The calculation method is as easy as it seems:<br>
     * The mass point will accelerate at its best to the half of the distance.<br>
     * If it reaches the top speed before halfway, it will travel at top speed to the half of the distance.<br>
     * Then the mass point will slow down to the given percentage of top speed.<br>
     * <br>
     * The term 'halfway' refers to the half of the distance which has to be travelled.<br>
     *
     * @param propulsionType               the prop type
     * @param targetedPercentageOfTopSpeed the percentage of top speed which should be left over at the end of the journey
     * @param distance                     the distance which has to be laid back
     * @return the amount of time which are needed for this distance, perhaps in seconds
     */
    public static int getDurationForTargetedEndSpeed(@Nonnull final EModuleType propulsionType,
                                                     final int targetedPercentageOfTopSpeed,
                                                     @Nonnull final ETechnologyType restrictingTechnologyType,
                                                     @Nonnull final Acceleration acceleration,
                                                     @Nonnull final Distance distance) {
        Preconditions.checkNotNull(propulsionType, "propulsionType shouldn't be null!");
        Preconditions.checkNotNull(restrictingTechnologyType, "restrictingTechnologyType must not be empty");
        Preconditions.checkNotNull(acceleration, "acceleration must not be empty");
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");
        Preconditions.checkArgument(propulsionType == EModuleType.PROPULSION || propulsionType == EModuleType.FTLPROPULSION, "propulsionType must be a propulsion!");
        Preconditions.checkArgument(!(targetedPercentageOfTopSpeed > 100 || targetedPercentageOfTopSpeed < 0), "haha, good try. But no!");

        // Stage 1: preparation
        // v = s / t
        // s = 0,5 · a · t²
        // v = a · t
        // s = 0,5 · v · t
        final EHyperBand hyperBand = acceleration.getHyperBand();
        final BigDecimal vesselTopSpeed = hyperBand.getEffectiveTopSpeed(restrictingTechnologyType);

        // calculate acceleration to top speed
        final BigDecimal accelerationValue = MathHelper.getOrEpsilon(acceleration.convertToMetric(EAccelerationMetric.MS2));
        final BigDecimal timeToTopSpeed = vesselTopSpeed.divide(accelerationValue, MC_HU);

        // Stage 2: calculate time to halfway distance
        // s = 0,5 · a · t²
        // t = sqrt(  s / (0,5 * a) )
        final EDistanceMetric distanceMetric = EDistanceMetric.M;
        final BigDecimal halfwayDistanceInMetric = distance.getCoordinateInMetric(distanceMetric).divide(BigDecimal.valueOf(2), MC_HU);
        final BigDecimal timeToHalfwaySquared = halfwayDistanceInMetric.divide(new BigDecimal("0.5").multiply(accelerationValue), MC_HU);
        final BigDecimal timeToHalfwayDistance = timeToHalfwaySquared.sqrt(MC_HU);

        final BigDecimal timeToHalfway;
        final BigDecimal speedAtHalfway;
        if (timeToHalfwayDistance.compareTo(timeToTopSpeed) > 0) {
            // timeToHalfwayDistance > timeToTopSpeed - by avoiding speed of light issues
            // accelerate only to top speed time and travel
            final Distance distanceToTopSpeed = acceleration.getDistanceByTime(new Time(timeToTopSpeed, ETimeMetric.SECOND), Velocity.ZERO, EDistanceMetric.M);
            final Distance halfway = new Distance(halfwayDistanceInMetric, distanceMetric);
            final Distance distanceToTravelWithTopSpeed = halfway.subtract(distanceToTopSpeed);
            // calc time to travel at top speed
            // t = s / v
            final BigDecimal travelTimeAtTopSpeed = distanceToTravelWithTopSpeed.getCoordinateInMetric(distanceMetric).divide(vesselTopSpeed, MC_HU);
            speedAtHalfway = vesselTopSpeed;
            timeToHalfway = travelTimeAtTopSpeed.add(timeToTopSpeed);
        } else {
            // effective case: timeToHalfwayDistance <= timeToTopSpeed
            // accelerate only to half distance time
            // v = a * t
            speedAtHalfway = accelerationValue.multiply(timeToHalfwayDistance, MC_HU);
            timeToHalfway = timeToHalfwayDistance;
        }

        // Stage 3: Slow down to targeted speed
        final String percentageString = ((double) targetedPercentageOfTopSpeed / 100) + "";
        final BigDecimal targetedVesselEndSpeed = vesselTopSpeed.multiply(new BigDecimal(percentageString), MC_HU);
        final BigDecimal speedToSlowDownFrom = speedAtHalfway.compareTo(targetedVesselEndSpeed) > 0 ? speedAtHalfway.subtract(targetedVesselEndSpeed) : speedAtHalfway;
        // t = v / a
        final BigDecimal timeToSlowDown = speedToSlowDownFrom.divide(accelerationValue, MC_HU);

        // must be in seconds
        final BigDecimal travelTime = timeToHalfway.add(timeToSlowDown);
        // guessing that the duration will not exceed 85 years
        if (travelTime.compareTo(new BigDecimal(Integer.MAX_VALUE)) > 0) {
            throw new NotifyWebUserException("Oh dear, this journey is to hard for us.");
        }
        return travelTime.intValue();
    }

    @Nonnull
    public static List<AccelerationProfile> createAccelerationProfile(@Nonnull final KinematicInfo startConditions,
                                                                      @Nonnull final KinematicInfo endConditions,
                                                                      @Nonnull final Distance lengthOnCurve,
                                                                      @Nonnull final Velocity topSpeed) {
        Preconditions.checkNotNull(startConditions, "startConditions must not be empty");
        Preconditions.checkNotNull(endConditions, "endConditions must not be empty");
        Preconditions.checkNotNull(lengthOnCurve, "lengthOnCurve must not be empty");
        Preconditions.checkNotNull(topSpeed, "topSpeed must not be empty");

        final Acceleration constantAcceleration = startConditions.getAcceleration();

        final Velocity startConditionsVelocity = startConditions.getVelocity();
        final Velocity endConditionsVelocity = endConditions.getVelocity();
        final Distance toAccelerate = calculateDistanceToAccelerate(startConditionsVelocity, endConditionsVelocity, lengthOnCurve);
        final Distance toDecelerate = lengthOnCurve.subtract(toAccelerate);

        final Time timeToAccelerate = toAccelerate.calculateTimeToPass(constantAcceleration);
        final Time timeToAccelerate1 = toAccelerate.calculateTimeToPass(constantAcceleration, topSpeed); // todo sharpen the acceleration profile in the name of Newton
        final Time timeToDecelerate = toDecelerate.calculateTimeToPass(constantAcceleration);

        final List<AccelerationProfile> result = createAccelerationProfile(new CombatRound(), topSpeed, timeToAccelerate, constantAcceleration, startConditionsVelocity, Distance.ZERO);
        final AccelerationProfile latest = Collections.max(result);
        final Velocity max = latest.getDynamicInfo().getVelocity();
        final CombatRound combatRound = latest.getCombatRound().clone();
        combatRound.next();
        result.addAll(createAccelerationProfile(combatRound, Velocity.ZERO, timeToDecelerate, constantAcceleration.negate(), max, latest.getDynamicInfo().getDistance()));

        // fixme workaround to don't overrun distance on rounding issues
        result.stream()
                .filter(ap -> ap.getDynamicInfo().getDistance().compareTo(lengthOnCurve) > 0)
                .forEach(ap -> ap.getDynamicInfo().with(lengthOnCurve));

        return result;
    }

    @Nonnull
    private static List<AccelerationProfile> createAccelerationProfile(@Nonnull final CombatRound startingRound,
                                                                       @Nonnull final Velocity speedLimit,
                                                                       @Nonnull final Time timeToAccelerate,
                                                                       @Nonnull final Acceleration constantAcceleration,
                                                                       @Nonnull final Velocity initialVelocity,
                                                                       @Nonnull final Distance initialDistance) {
        Preconditions.checkNotNull(startingRound, "startingRound must not be empty");
        Preconditions.checkNotNull(speedLimit, "speedLimit must not be empty");
        Preconditions.checkNotNull(timeToAccelerate, "timeToAccelerate must not be empty");
        Preconditions.checkNotNull(constantAcceleration, "constantAcceleration must not be empty");
        Preconditions.checkNotNull(initialVelocity, "initialVelocity must not be empty");

        final boolean isNoAcceleration = constantAcceleration.compareTo(Acceleration.ZERO) == 0;
        if (isNoAcceleration) {
            return new ArrayList<>();
        }

        final boolean isBraking = constantAcceleration.compareTo(Acceleration.ZERO) < 0;

        final List<AccelerationProfile> result = new ArrayList<>();

        Time time = Time.ZERO;
        Distance distance = initialDistance.clone();
        final CombatRound round = startingRound.clone();
        while (time.compareTo(timeToAccelerate) < 0) {

            if (hasZeroVelocity(result)) {
                // we will never reach the destination without velocity
                break;
            }

            time = time.add(CombatRound.COMBAT_ROUND);
            final Velocity velocityAtRoundEnd = getVelocityRespectSpeedLimit(speedLimit, constantAcceleration, initialVelocity, time, isBraking);

            final Velocity velocityAtRoundBeginning = getVelocityRespectSpeedLimit(speedLimit, constantAcceleration, initialVelocity, time.subtract(CombatRound.COMBAT_ROUND), isBraking);
            distance = distance.getByVelocityAndConstantAccelerationOverTime(constantAcceleration, velocityAtRoundBeginning, CombatRound.COMBAT_ROUND);

            result.add(new AccelerationProfile(round.clone(), constantAcceleration, velocityAtRoundEnd, distance));
            round.next();
        }
        return result;
    }

    @Nonnull
    private static Velocity getVelocityRespectSpeedLimit(@Nonnull final Velocity speedLimit,
                                                         @Nonnull final Acceleration constantAcceleration,
                                                         @Nonnull final Velocity initialVelocity,
                                                         @Nonnull final Time time,
                                                         final boolean isBraking) {
        Preconditions.checkNotNull(speedLimit, "speedLimit must not be empty");
        Preconditions.checkNotNull(constantAcceleration, "constantAcceleration must not be empty");
        Preconditions.checkNotNull(initialVelocity, "initialVelocity must not be empty");
        Preconditions.checkNotNull(time, "time must not be empty");

        final Velocity velocity = initialVelocity.getVelocityByAcceleration(constantAcceleration, time);
        // don't exceed speed limit
        return !violatesSpeedLimit(isBraking, speedLimit, velocity) ? velocity : speedLimit;
    }

    private static boolean hasZeroVelocity(@Nonnull final List<AccelerationProfile> accelerationProfile) {
        Preconditions.checkNotNull(accelerationProfile, "accelerationProfile must not be empty");

        if (accelerationProfile.isEmpty()) {
            return false;
        }

        final Velocity lastVelocity = Collections.max(accelerationProfile).getDynamicInfo().getVelocity();
        return lastVelocity.compareTo(Velocity.ZERO) == 0;
    }


    private static boolean violatesSpeedLimit(final boolean isSlowDown, @Nonnull final Velocity speedLimit, @Nonnull final Velocity velocity) {
        Preconditions.checkNotNull(speedLimit, "speedLimit must not be empty");
        Preconditions.checkNotNull(velocity, "velocity must not be empty");

        if (isSlowDown) {
            return velocity.compareTo(speedLimit) < 0;
        }
        return velocity.compareTo(speedLimit) > 0;
    }

    @Nonnull
    private static Distance calculateDistanceToAccelerate(@Nonnull final Velocity startVelocity, @Nonnull final Velocity designatedVelocity, @Nonnull final Distance distance) {
        Preconditions.checkNotNull(startVelocity, "startVelocity must not be empty");
        Preconditions.checkNotNull(designatedVelocity, "designatedVelocity must not be empty");
        Preconditions.checkNotNull(distance, "distance must not be empty");

        if (startVelocity.compareTo(designatedVelocity) == 0) {
            // start- und endgeschwindigkeit identisch → halbe strecke
            return distance.divide(2);
        }

        // value is normalized due same metrics and only needed as scalar here
        return distance.divide(startVelocity.divide(designatedVelocity).getValue().longValue());
    }
}
