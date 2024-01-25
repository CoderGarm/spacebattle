package de.yuga.spacebattle.backend.calculator.distance;

import com.google.common.base.Preconditions;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.RotationConvention;
import org.hipparchus.geometry.euclidean.threed.RotationOrder;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orekit.attitudes.FrameAlignedProvider;
import org.orekit.attitudes.LofOffset;
import org.orekit.data.DataProvidersManager;
import org.orekit.forces.maneuvers.ImpulseManeuver;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.events.DateDetector;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.TimeStampedPVCoordinates;

import javax.annotation.Nonnull;
import java.net.URISyntaxException;

class NavigationCalculatorTest {

    @BeforeEach
    void setUp() {
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, getPath("orekit/zipped-data/multizip.zip"));
    }

    @Nonnull
    private String getPath(@Nonnull final String resourceName) {
        Preconditions.checkNotNull(resourceName, "resourceName must not be empty");

        try {
            ClassLoader loader = this.getClass().getClassLoader();
            return loader.getResource(resourceName).toURI().getPath();
        } catch (URISyntaxException e) {
            Assertions.fail(e.getLocalizedMessage());
            return null;
        }
    }

    // todo remove orekit incl resource files when not useful

    @Test
    void thrustManeuvers() {
        final double mu = 1;// todo set up gravitational attraction CelestialBodyFactory.getEarth().getGM();

        final double initialX = 7100e3;
        final double initialY = 0.0;
        final double initialZ = 1300e3;
        final double initialVx = 0;
        final double initialVy = 8000;
        final double initialVz = 1000;

        final Vector3D position = new Vector3D(initialX, initialY, initialZ);
        final Vector3D velocity = new Vector3D(initialVx, initialVy, initialVz);
        final AbsoluteDate epoch = new AbsoluteDate(2010, 1, 1, 0, 0, 0, TimeScalesFactory.getUTC());
        final TimeStampedPVCoordinates state = new TimeStampedPVCoordinates(epoch, position, velocity, Vector3D.ZERO);
        final Orbit initialOrbit = new CartesianOrbit(state, FramesFactory.getEME2000(), mu);

        final double totalPropagationTime = 0.00001;
        final double driftTimeInSec = totalPropagationTime / 2.0;
        final double deltaX = 0.01;
        final double deltaY = 0.02;
        final double deltaZ = 0.03;
        final double isp = 300;

        final Vector3D deltaV = new Vector3D(deltaX, deltaY, deltaZ);

        KeplerianPropagator propagator = new KeplerianPropagator(initialOrbit, new LofOffset(initialOrbit.getFrame(), LOFType.VNC));
        DateDetector dateDetector = new DateDetector(epoch.shiftedBy(driftTimeInSec));
        FrameAlignedProvider attitudeOverride = new FrameAlignedProvider(new Rotation(RotationOrder.XYX,
                RotationConvention.VECTOR_OPERATOR,
                0, 0, 0));
        ImpulseManeuver burnAtEpoch = new ImpulseManeuver(dateDetector, attitudeOverride, deltaV, isp).withThreshold(driftTimeInSec / 4);
        Assertions.assertEquals(0.0, Vector3D.distance(deltaV, burnAtEpoch.getDeltaVSat()), 1.0e-15);
        Assertions.assertEquals(isp, burnAtEpoch.getIsp(), 1.0e-15);
        Assertions.assertSame(dateDetector, burnAtEpoch.getTrigger());
        propagator.addEventDetector(burnAtEpoch);

        SpacecraftState finalState = propagator.propagate(epoch.shiftedBy(totalPropagationTime));

        final double finalVxExpected = initialVx + deltaX;
        final double finalVyExpected = initialVy + deltaY;
        final double finalVzExpected = initialVz + deltaZ;
        final double maneuverTolerance = 1e-4;

        final Vector3D finalVelocity = finalState.getPVCoordinates().getVelocity();
        Assertions.assertEquals(finalVxExpected, finalVelocity.getX(), maneuverTolerance);
        Assertions.assertEquals(finalVyExpected, finalVelocity.getY(), maneuverTolerance);
        Assertions.assertEquals(finalVzExpected, finalVelocity.getZ(), maneuverTolerance);
    }

}
