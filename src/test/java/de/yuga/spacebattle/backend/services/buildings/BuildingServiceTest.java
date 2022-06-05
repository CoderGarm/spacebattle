package de.yuga.spacebattle.backend.services.buildings;

import de.yuga.spacebattle.BaseTestCase;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.repositories.buildings.BuildingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
public class BuildingServiceTest extends BaseTestCase {

    @Mock
    private BuildingRepository buildingRepositoryMock;

    private BuildingService testObject;

    @BeforeEach
    public void beforeClass() {
        testObject = new BuildingService(buildingRepositoryMock);
    }

    @Test
    public void testFindAll() {
        // prepare stuff
        final ArrayList<Building> userList = new ArrayList<>();
        // prepare mocks
        when(buildingRepositoryMock.findAllBuildings()).thenReturn(userList);
        // test method
        final List<Building> result = testObject.findAll();
        // check expectation
        assertNotNull(result);
        assertEquals(result, userList);
    }

    @Test
    public void testFind() {
        // prepare stuff
        final int idBuilding = 1;
        final Building expectation = new Building();
        ReflectionTestUtils.setField(expectation, "id", idBuilding);
        final Optional<Building> optionalBuilding = Optional.of(expectation);
        // prepare mocks
        when(buildingRepositoryMock.findById(idBuilding)).thenReturn(optionalBuilding);
        // test method
        final Building result = testObject.find(idBuilding);
        // check expectation
        assertNotNull(result);
        assertEquals(result, expectation);
    }

    @Test
    public void testCreateBuilding() {
        // prepare stuff
        final String name = "name";
        final String description = "description";
        final int baseValue = 1;
        final EResourceType researchType = EResourceType.RESEARCH;
        final Research unlockedThrough = new Research();
        Map<EEducationType, Long> crewRequirement = new HashMap<>();
        crewRequirement.put(EEducationType.UNIVERSITY, 100L);
        CrewRequirement militaryCrew = new CrewRequirement(crewRequirement, EDepositType.COSTS);
        final Building expectation = new Building(name, description, baseValue, ETechLevel.TECH_I, new ProductionType(researchType, EProductionCategory.PRODUCE, null), militaryCrew, unlockedThrough);
        // prepare mocks
        when(buildingRepositoryMock.save(expectation)).thenReturn(expectation);
        // test method
        final Building result = testObject.createBuilding(name, description, baseValue, ETechLevel.TECH_I, new ProductionType(researchType, EProductionCategory.PRODUCE, null), EEducationType.UNIVERSITY, 100L, unlockedThrough);
        // check expectation
        assertNotNull(result);
        assertEquals(result, expectation);
    }
}
