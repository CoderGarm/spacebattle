package de.yuga.spacebattle.backend.distance;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ERaceType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class DistanceCalculatorTest {

    @DataProvider
    private Object[][] testCalculateDistanceData() {
        final Orbit orbitStart = new Orbit(0, 0);
        final Planet planetStart = createPlanet(orbitStart);

        final Orbit orbitTarget1 = new Orbit(10, 10);
        final Planet planetTarget1 = createPlanet(orbitTarget1);
        final Orbit orbitTarget2 = new Orbit(20, 20);
        final Planet planetTarget2 = createPlanet(orbitTarget2);
        final Orbit orbitTarget3 = new Orbit(30, 30);
        final Planet planetTarget3 = createPlanet(orbitTarget3);

        return new Object[][]{
                {createFleet(10, planetStart), planetTarget1, 1},
                {createFleet(15, planetStart), planetTarget1, 1},

                {createFleet(10, planetStart), planetTarget2, 2},
                {createFleet(14, planetStart), planetTarget2, 2},

                {createFleet(11, planetStart), planetTarget3, 3},
                {createFleet(14, planetStart), planetTarget3, 3},

        };
    }

    @Test(dataProvider = "testCalculateDistanceData")
    public void testCalculateDistance(final Fleet fleet, final Planet planet, final int expectation) {
        int calculateDistance = DistanceCalculator.calculateDistance(fleet, planet);
        assertEquals(calculateDistance, expectation);
    }

    private Fleet createFleet(final int effectFTLValue, final Planet planetStart) {
        final User user = new User();
        user.setRaceType(ERaceType.HUMAN);
        final Fleet fleet = new Fleet();
        fleet.setOwner(user);
        final ShipClass shipClass = new ShipClass();
        final Map<Module, Integer> modules = new HashMap<>();
        final Module module = new Module("module1", EModuleType.FTLPROPULSION, "description",
                1, effectFTLValue, 1, new Research());
        modules.put(module, 1);
        shipClass.setModules(modules);
        fleet.updateShips(shipClass, 1);
        fleet.setOrbit(new FleetOrbit(planetStart.getSystem(), planetStart));
        return fleet;
    }

    private Planet createPlanet(final Orbit orbitTarget) {
        final Orbit random = new Orbit(0, 0);
        final StarSystem starSystemTarget = new StarSystem("random", orbitTarget);
        return new Planet(null, "name1", starSystemTarget, random);
    }


}