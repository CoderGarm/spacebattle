package de.yuga.spacebattle.backend.combat.main;

import de.yuga.spacebattle.BaseTestCase;
import de.yuga.spacebattle.TestDataProviderUtils;
import de.yuga.spacebattle.backend.combat.main.handler.CombatHandler;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetHealthState;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Disabled("broken by out of date")
class CageTest extends BaseTestCase {

    private Cage testObject;

    /**
     * Creates a fully capable cage with a minimal setup of fleets, ship classes, modules, war ships and so on, and so on.
     */
    @BeforeEach
    void setUp() {
        testObject = TestDataProviderUtils.cage();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testConstructor() {
        // prepare stuff
        final CombatRound currentCombatRound = testObject.getCurrentCombatRound();
        assertNotNull(currentCombatRound);
        assertEquals(0, currentCombatRound.getNo());
        final CombatHandler combatHandler = (CombatHandler) ReflectionTestUtils.getField(testObject, "combatHandler");
        assertNotNull(combatHandler);
        final List<FleetRoundState> roundStates = (List<FleetRoundState>) ReflectionTestUtils.getField(testObject, "roundStates");
        assertNotNull(roundStates);
        final List<Fleet> participatingFleets = (List<Fleet>) ReflectionTestUtils.getField(testObject, "participatingFleets");
        assertNotNull(participatingFleets);
        assertEquals(roundStates.size(), participatingFleets.size());
        // test method
        final boolean isDone = testObject.isDone();
        // check expectation
        assertFalse(isDone);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testIsDone() {
        // prepare stuff
        final List<Fleet> participatingFleets = (List<Fleet>) ReflectionTestUtils.getField(testObject, "participatingFleets");
        assertNotNull(participatingFleets);
        List<FleetRoundState> roundStates = (List<FleetRoundState>) ReflectionTestUtils.getField(testObject, "roundStates");
        assertNotNull(roundStates);
        // set all fleets to destroyed
        roundStates.stream().map(FleetRoundState::getFleetHealthState).map(FleetHealthState::getWarshipHealthStates).forEach(warshipHealthStateMap -> {
            warshipHealthStateMap.values().forEach(warshipHealthState -> {
                ReflectionTestUtils.setField(warshipHealthState, "hullState", 0);
            });
        });
        // test method
        final boolean result = testObject.isDone();
        // check expectation
        assertTrue(result);
    }

    /**
     * Must return the result of the future.
     */
    @Test
    void testGet() {
        final Cage result;
        try {
            result = testObject.get();
            assertNotNull(result);
            assertSame(testObject, result);
        } catch (InterruptedException | ExecutionException e) {
            fail("Strange things happens when asking for the future result.");
        }
    }

    @Test
    void testHandleCombatPhases() {
        // prepare stuff
        final Cage testObject = mock(Cage.class);
        // mock methods
        when(testObject.isDone()).thenReturn(false).thenReturn(true);
        doCallRealMethod().when(testObject).handleCombatPhases();
        // test method
        testObject.handleCombatPhases();
        // check expectation
        // second invocation of isDone() is to end the mocked battle
        verify(testObject, times(2)).isDone();
        verify(testObject, times(1)).executeCombatRound();
    }

    @Test
    void testExecuteCombatRound() {
        // mock methods
        final CombatHandler combatHandlerMock = mock(CombatHandler.class);
        ReflectionTestUtils.setField(testObject, "combatHandler", combatHandlerMock);
        doNothing().when(combatHandlerMock).handleMovementPhase();
        doNothing().when(combatHandlerMock).handleMissilePhase();
        doNothing().when(combatHandlerMock).handleIncomingWeaponFirePhase();
        doNothing().when(combatHandlerMock).handleFireWeaponPhase();
        // test method
        testObject.executeCombatRound();
        // check expectation
        verify(combatHandlerMock, times(1)).handleMovementPhase();
        verify(combatHandlerMock, times(1)).handleMissilePhase();
        verify(combatHandlerMock, times(1)).handleIncomingWeaponFirePhase();
        verify(combatHandlerMock, times(1)).handleFireWeaponPhase();
    }

    @Test
    void testGetCurrentCombatRound() {
        // prepare stuff
        CombatRound combatRound = testObject.getCurrentCombatRound();
        assertNotNull(combatRound);
        assertEquals(0, combatRound.getNo());

        // mock methods
        final CombatHandler combatHandlerMock = mock(CombatHandler.class);
        ReflectionTestUtils.setField(testObject, "combatHandler", combatHandlerMock);
        doNothing().when(combatHandlerMock).handleMovementPhase();
        doNothing().when(combatHandlerMock).handleMissilePhase();
        doNothing().when(combatHandlerMock).handleIncomingWeaponFirePhase();
        doNothing().when(combatHandlerMock).handleFireWeaponPhase();
        // mock to run the round
        testObject.executeCombatRound();
        // test method
        combatRound = testObject.getCurrentCombatRound();
        // check expectation
        verify(combatHandlerMock, times(1)).handleMovementPhase();
        verify(combatHandlerMock, times(1)).handleMissilePhase();
        verify(combatHandlerMock, times(1)).handleIncomingWeaponFirePhase();
        verify(combatHandlerMock, times(1)).handleFireWeaponPhase();
        assertNotNull(combatRound);
        assertEquals(1, combatRound.getNo());
    }

    @Test
    void testGetCurrentStateByFleet() {
        // test method
        final FleetRoundState result = testObject.getCurrentStateByFleet(testObject.getParticipatingFleets().get(0));
        // check expectation
        assertNotNull(result);
        assertSame(testObject.getParticipatingFleets().get(0), result.getFleet());
    }

    @Test
    void testPrepareNextCombatRound() {
        // prepare stuff
        final int initialSize = testObject.getHistoryOfRounds().size();
        final CombatRound currentCombatRound = testObject.getCurrentCombatRound();
        final int currentCombatRoundNo = currentCombatRound.getNo();
        // test method
        testObject.prepareNextCombatRound(currentCombatRound);
        // check expectation
        final int nextSize = testObject.getHistoryOfRounds().size();
        final CombatRound nextCombatRound = testObject.getCurrentCombatRound();
        final int nextCombatRoundNo = nextCombatRound.getNo();
        assertEquals(1, nextCombatRoundNo - currentCombatRoundNo);
        assertEquals(testObject.getParticipatingFleets().size(), nextSize - initialSize);
    }
}
