package de.yuga.spacebattle.backend.combat.main.handler;

import de.yuga.spacebattle.backend.combat.dto.BeamVolley;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static de.yuga.spacebattle.TestDataProviderUtils.cage;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CombatHandlerTest {

    private CombatHandler testObject;

    private final Cage cage = cage();

    @BeforeEach
    void setUp() {
        testObject = new CombatHandler(cage);
    }

    @Test
    void testHandleMovementPhase() {
        // todo create useful productive code before
    }

    @Test
    void testDetectMovementType() {
        // todo create useful productive code before
    }

    @Test
    void testHandleMissilePhase() {
        // prepare stuff
        final Cage cageMock = mock(Cage.class);
        final MissileSalvo missileSalvoMock = mock(MissileSalvo.class);

        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        // mock methods
        when(cageMock.getFlyingMissileSalvos()).thenReturn(List.of(missileSalvoMock));
        // test method
        testObject.handleMissilePhase();
        // check expectation
        verify(missileSalvoMock, times(1)).handleMissilePhase();
    }

    @Test
    void testHandleIncomingWeaponFirePhase() {
        // prepare stuff
        final CombatHandler combatHandlerMock = mock(CombatHandler.class);
        // mock methods
        doCallRealMethod().when(combatHandlerMock).handleIncomingWeaponFirePhase();
        // test method
        combatHandlerMock.handleIncomingWeaponFirePhase();
        // check expectation
        verify(combatHandlerMock, times(1)).handleMissileDamage();
        verify(combatHandlerMock, times(1)).handleDirectWeaponDamage();
    }

    @Test
    void testHandleDirectWeaponDamage() {
        // prepare stuff
        final Cage cageMock = mock(Cage.class);
        final BeamVolley beamVolleyMock = mock(BeamVolley.class);

        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        // mock methods
        when(cageMock.getFlyingBeamVolleys()).thenReturn(List.of(beamVolleyMock));
        // test method
        testObject.handleDirectWeaponDamage();
        // check expectation
        verify(beamVolleyMock, times(1)).applyDamage();
    }

    @Test
    void testHandleMissileDamage() {
        // prepare stuff
        final Cage cageMock = mock(Cage.class);
        final MissileSalvo missileSalvoMock = mock(MissileSalvo.class);

        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        // mock methods
        when(cageMock.getFlyingMissileSalvos()).thenReturn(List.of(missileSalvoMock));
        when(missileSalvoMock.isInDetonationRange()).thenReturn(true);
        // test method
        testObject.handleMissileDamage();
        // check expectation
        verify(missileSalvoMock, times(1)).detonate();
    }

    @Test
    void testHandleFireWeaponPhase() {
        // prepare stuff
        final Cage cageMock = mock(Cage.class);
        final CombatHandler testObjectMock = mock(CombatHandler.class);
        final Fleet fleetOneMock = mock(Fleet.class);
        final FleetRoundState roundStateOneMock = mock(FleetRoundState.class);
        final Fleet fleetTwoMock = mock(Fleet.class);
        final FleetRoundState roundStateTwoMock = mock(FleetRoundState.class);

        final Orbit positionOne = Orbit.getCenterOrbit();
        final Orbit positionTwo = Orbit.getCenterOrbit();

        ReflectionTestUtils.setField(testObjectMock, "cage", cageMock);
        // mock methods
        doCallRealMethod().when(testObjectMock).handleFireWeaponPhase();
        when(cageMock.getFleetOne()).thenReturn(fleetOneMock);
        when(cageMock.getFleetTwo()).thenReturn(fleetTwoMock);
        when(cageMock.getCurrentStateByFleet(fleetOneMock)).thenReturn(roundStateOneMock);
        when(cageMock.getCurrentStateByFleet(fleetTwoMock)).thenReturn(roundStateTwoMock);
        when(roundStateOneMock.getPosition()).thenReturn(positionOne);
        when(roundStateTwoMock.getPosition()).thenReturn(positionTwo);
        // test method
        testObjectMock.handleFireWeaponPhase();
        // check expectation
        verify(testObjectMock, times(1)).fireBeams(fleetOneMock, fleetTwoMock);
        verify(testObjectMock, times(1)).fireMissiles(fleetOneMock, fleetTwoMock);
    }

    @Test
    void testFireBeams() {
        // prepare stuff
        final Fleet actor = cage.getFleetOne();
        final Fleet target = cage.getFleetTwo();

        final Cage cageMock = mock(Cage.class);
        final FleetRoundState roundStateOneMock = mock(FleetRoundState.class);
        final FleetRoundState roundStateTwoMock = mock(FleetRoundState.class);
        final Orbit position = Orbit.getCenterOrbit();
        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        ReflectionTestUtils.setField(cageMock, "flyingBeamVolleys", new ArrayList<>());
        // mock methods
        when(cageMock.getCurrentStateByFleet(actor)).thenReturn(roundStateOneMock);
        when(roundStateOneMock.getPosition()).thenReturn(position);
        when(cageMock.getCurrentStateByFleet(target)).thenReturn(roundStateTwoMock);
        when(roundStateTwoMock.getPosition()).thenReturn(position);
        doCallRealMethod().when(cageMock).addToFlyingBeamVolleys(any());
        doCallRealMethod().when(cageMock).getFlyingBeamVolleys();
        // test method
        testObject.fireBeams(actor, target);
        // check expectation
        final List<BeamVolley> result = cageMock.getFlyingBeamVolleys();
        assertNotNull(result);
        assertEquals(1, result.size());
        final BeamVolley resultingVolleyOne = result.get(0);
        assertEquals(actor, resultingVolleyOne.getActor());
        assertEquals(target, resultingVolleyOne.getTarget());
        assertSame(ECombatPhase.ECombatSubPhase.BEAM_FIRE_PHASE, resultingVolleyOne.getCombatSubPhase());
    }

    @Test
    void testFireMissiles() {
        // prepare stuff
        final Fleet actor = cage.getFleetOne();
        final Fleet target = cage.getFleetTwo();
        final Cage cageMock = mock(Cage.class);
        final FleetRoundState roundStateOneMock = mock(FleetRoundState.class);
        final FleetRoundState roundStateTwoMock = mock(FleetRoundState.class);

        final Orbit position = Orbit.getCenterOrbit();
        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        ReflectionTestUtils.setField(cageMock, "flyingMissileSalvos", new ArrayList<>());
        // mock methods
        when(cageMock.getCurrentStateByFleet(actor)).thenReturn(roundStateOneMock);
        when(roundStateOneMock.getPosition()).thenReturn(position);
        when(cageMock.getCurrentStateByFleet(target)).thenReturn(roundStateTwoMock);
        when(roundStateTwoMock.getPosition()).thenReturn(position);
        doCallRealMethod().when(cageMock).addToFlyingMissileSalvos(any());
        doCallRealMethod().when(cageMock).getFlyingMissileSalvos();
        // test method
        testObject.fireMissiles(actor, target);
        // check expectation
        final List<MissileSalvo> result = cageMock.getFlyingMissileSalvos();
        assertNotNull(result);
        assertEquals(1, result.size());
        final MissileSalvo resultingVolleyOne = result.get(0);
        assertEquals(actor, resultingVolleyOne.getActor());
        assertEquals(target, resultingVolleyOne.getTarget());
        assertSame(ECombatPhase.ECombatSubPhase.MISSILE_FIRE_PHASE, resultingVolleyOne.getCombatSubPhase());
    }
}
