package de.yuga.spacebattle.backend.services;

import de.yuga.spacebattle.rest.dto.misc.Coords;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceServiceTest {

    @Test
    void getRandomWarshipName() {
        final String result = new ResourceService().getRandomWarshipName();
        assertTrue(StringUtils.isNotBlank(result));

        final List<String> results = new ResourceService().getRandomWarshipName(6);
        results.forEach(name -> assertTrue(StringUtils.isNotBlank(name)));
    }

    @Test
    void getRandomPlanetName() {
        final String result = new ResourceService().getRandomPlanetName();
        assertTrue(StringUtils.isNotBlank(result));

        final List<String> results = new ResourceService().getRandomPlanetName(6);
        results.forEach(name -> assertTrue(StringUtils.isNotBlank(name)));
    }

    @Test
    void getCoords() {
        final List<Coords> result = new ResourceService().readStarSystems();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
