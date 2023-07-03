package de.yuga.spacebattle.backend.calculator.distance;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Time;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
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

import static de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator.MC_HU;

public class NavigationCalculator {

    private NavigationCalculator() {
    }


    @Nonnull
    public static Orbit getPositionOnHyperlimit(@Nonnull final Fleet fleet, @Nonnull final FleetOrbit destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkArgument(destination.getSystem() != null, "destination system shouldn't be null!");
        Preconditions.checkArgument(destination.getOrbit() != null, "destination orbit shouldn't be null!");
        Preconditions.checkArgument(fleet.getOrbit() != null, "fleet's orbit shouldn't be null here");

        final Quadrant quadrant = Quadrant.getByOrbit(destination.getOrbit());
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
     * @param fleet                        the fleet which travels
     * @param distance                     the distance which has to be laid back
     * @return the amount of time which are needed for this distance, perhaps in seconds
     */
    public static int getDurationForTargetedEndSpeed(@Nonnull final EModuleType propulsionType,
                                                     final int targetedPercentageOfTopSpeed,
                                                     @Nonnull final Fleet fleet,
                                                     @Nonnull final Distance distance) {
        Preconditions.checkNotNull(propulsionType, "propulsionType shouldn't be null!");
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");
        Preconditions.checkArgument(propulsionType == EModuleType.PROPULSION || propulsionType == EModuleType.FTLPROPULSION, "propulsionType must be a propulsion!");
        Preconditions.checkArgument(!(targetedPercentageOfTopSpeed > 100 || targetedPercentageOfTopSpeed < 0), "haha, good try. But no!");

        // Stage 1: preparation
        // v = s / t
        // s = 0,5 · a · t²
        // v = a · t
        // s = 0,5 · v · t
        final ETechnologyType restrictingTechnologyType = fleet.getRestrictingTechnologyType();
        final Acceleration acceleration = fleet.getAccelerationFor(propulsionType);
        final EHyperBand hyperBand = acceleration.getHyperBand();
        final BigDecimal vesselTopSpeed = hyperBand.getEffectiveTopSpeed(restrictingTechnologyType);

        // calculate acceleration to top speed
        final BigDecimal accelerationValue = acceleration.convertToMetric(EAccelerationMetric.MS2);
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
}
