package de.yuga.spacebattle.backend.calculator.distance.dijkstra;

import de.yuga.spacebattle.SpringBootProdProfile;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.caclulator.NavigationCalculatorService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootProdProfile
@Disabled("not needed for unit or integration testing")
class NavigationCalculatorServiceTest {

    @Autowired
    private NavigationCalculatorService navigationCalculatorService;

    @Autowired
    private StarSystemService starSystemService;

    @Test
    void checkIt() {

        final List<StarSystem> byNames = new ArrayList<>(starSystemService.findByNames(Set.of("Manticore", "Sol")));

        final List<StarSystem> shortestWaypoints = navigationCalculatorService.getShortestWaypoints(byNames.get(0), byNames.get(1));
        final List<String> result = shortestWaypoints.stream().map(StarSystem::getName).collect(Collectors.toList());
        assertEquals("Manticore", result.get(0));
        assertEquals("Sigma Draconis", result.get(1));
        assertEquals("Sol", result.get(2));
    }
}
