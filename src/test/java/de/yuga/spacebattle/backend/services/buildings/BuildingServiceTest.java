package de.yuga.spacebattle.backend.services.buildings;

import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.buildings.BuildingRepository;
import de.yuga.spacebattle.backend.test.MocksNotUsedTestListener;
import de.yuga.spacebattle.backend.test.SBEasyMockSupport;
import org.easymock.Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Listeners({MocksNotUsedTestListener.class})
public class BuildingServiceTest extends SBEasyMockSupport {

    @Mock
    private BuildingRepository buildingRepositoryMock;

    private BuildingService testObject;

    @BeforeClass
    public void beforeClass() {
        injectMocks(this);
        testObject = new BuildingService(buildingRepositoryMock);
    }

    @AfterClass
    public void afterClass() {
        buildingRepositoryMock = null;
        testObject = null;
    }

    @Test
    public void testFindAll() {
        // prepare stuff
        final ArrayList<Building> userList = new ArrayList<>();
        // prepare mocks
        expect(buildingRepositoryMock.findAllBuildings()).andReturn(userList);
        // replay mocks
        replayAll();
        // test method
        final List<Building> result = testObject.findAll();
        // verify mocks
        verifyAll();
        // check expectation
        assertNotNull(result);
        assertEquals(result, userList);
    }

    @Test
    public void testFind() {
        // prepare stuff
        final int idBuilding = 1;
        final Building expectation = new Building();
        expectation.setId(idBuilding);
        final Optional<Building> optionalBuilding = Optional.of(expectation);
        // prepare mocks
        expect(buildingRepositoryMock.findById(idBuilding)).andReturn(optionalBuilding);
        // replay mocks
        replayAll();
        // test method
        final Building result = testObject.find(idBuilding);
        // verify mocks
        verifyAll();
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
        crewRequirement.put(EEducationType.CIVIL_MK_III, 100L);
        CrewRequirementDTO militaryCrew = new CrewRequirementDTO(crewRequirement, EDepositType.COSTS);
        final Building expectation = new Building(name, description, baseValue, new ProductionType(researchType, EProductionCategory.PRODUCE, null), militaryCrew, unlockedThrough);
        // prepare mocks
        expect(buildingRepositoryMock.save(expectation)).andReturn(expectation);
        // replay mocks
        replayAll();
        // test method
        final Building result = testObject.createBuilding(name, description, baseValue, new ProductionType(researchType, EProductionCategory.PRODUCE, null), EEducationType.CIVIL_MK_III, 100L, unlockedThrough);
        // verify mocks
        verifyAll();
        // check expectation
        assertNotNull(result);
        assertEquals(result, expectation);
    }
}