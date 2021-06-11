package de.yuga.spacebattle.backend.calculator.distance;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        int calculateDistance = DistanceCalculator.calculateTimeToTravel(fleet, planet);
        assertEquals(calculateDistance, expectation);
    }

    private Fleet createFleet(final int effectFTLValue, final Planet planetStart) {
        final User user = new User();
        final Fleet fleet = new Fleet();
        fleet.setOwner(user);
        final ShipClass shipClass = new ShipClass();
        final Map<EEducationType, Long> crewRequirementMap = new HashMap<>();
        crewRequirementMap.put(EEducationType.MILITARY_MK_II, 10L);
        final CrewRequirementDTO crewRequirement = new CrewRequirementDTO(crewRequirementMap, EDepositType.DEPOSITS);
        final Propulsion propulsion = new Propulsion("module1", "description", new Research(),
                1, effectFTLValue, 1, true, crewRequirement);
        shipClass.setPropulsion(propulsion);
        final Set<WarShip> warShips = new HashSet<>();
        warShips.add(new WarShip("", new Planet(), fleet, shipClass));
        fleet.updateShips(warShips);
        fleet.setOrbit(new FleetOrbit(planetStart));
        return fleet;
    }

    private Planet createPlanet(final Orbit orbitTarget) {
        final Orbit random = new Orbit(0, 0);
        final StarSystem starSystemTarget = new StarSystem("random", orbitTarget);
        return new Planet(null, "name1", starSystemTarget, random);
    }


}