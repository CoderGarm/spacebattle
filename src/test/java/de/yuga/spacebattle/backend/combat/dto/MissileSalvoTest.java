package de.yuga.spacebattle.backend.combat.dto;

import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetHealthState;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.combat.round.MissileSalvoHealthState;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.EDistanceMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static de.yuga.spacebattle.TestDataProviderUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MissileSalvoTest {

    private Fleet target;

    private MissileSalvo testObject;

    @BeforeEach
    void setUp() {
        final Cage cage = cage();
        final Fleet actor = cage.getParticipatingFleets().get(0);
        target = cage.getParticipatingFleets().get(1);
        testObject = new MissileSalvo(cage, actor, target);
    }

    @Test
    void testConstructor() {
        assertSame(ECombatPhase.ECombatSubPhase.MISSILE_FIRE_PHASE, testObject.getCombatSubPhase());
        final MissileSalvoHealthState missileSalvoHealthState = testObject.getMissileSalvoHealthState();
        assertNotNull(missileSalvoHealthState);
        final Distance initialDistance = testObject.getInitialDistance();
        assertEquals(new Distance(new BigDecimal("8.032E+8"), EDistanceMetric.M), initialDistance);
        final Distance rangePerCombatRound = testObject.getRangePerCombatRound();
        assertEquals(new Distance(81140000, EDistanceMetric.M), rangePerCombatRound);
        final Distance longestOffensiveRange = testObject.getLongestOffensiveRange();
        assertEquals(new Distance(50000, EDistanceMetric.M), longestOffensiveRange);
    }

    @Test
    void testHandleMissilePhase() {
        final MissileSalvo testObject = mock(MissileSalvo.class);
        final MissileSalvoHealthState salvoHealthStateMock = mock(MissileSalvoHealthState.class);
        ReflectionTestUtils.setField(testObject, "missileSalvoHealthState", salvoHealthStateMock);
        // mock methods
        when(salvoHealthStateMock.isActive()).thenReturn(true);
        doCallRealMethod().when(testObject).handleMissilePhase();
        doNothing().when(testObject).handleElokaPhase();
        doNothing().when(testObject).handleCounterMissilePhase();
        doNothing().when(testObject).handleMovement();
        // test method
        testObject.handleMissilePhase();
        // check expectation
        verify(salvoHealthStateMock, times(4)).isActive();
        verify(salvoHealthStateMock, times(1)).clearLosses();
        verify(testObject, times(1)).handleElokaPhase();
        verify(testObject, times(1)).handleCounterMissilePhase();
        verify(testObject, times(1)).handleMovement();
    }

    @Test
    void testTestClone() {
        // prepare stuff
        final UUID uuid = testObject.getUuid();
        final CombatRound combatRound = testObject.getCombatRound();
        final Orbit position = testObject.getPosition();
        final Orbit lastPosition = testObject.getLastPosition();
        final MissileSalvoHealthState missileSalvoHealthState = testObject.getMissileSalvoHealthState();
        // test method
        final MissileSalvo clone = testObject.clone();
        // check expectation
        assertEquals(uuid, clone.getUuid());
        assertNotSame(uuid, clone.getUuid());
        assertEquals(combatRound, clone.getCombatRound());
        assertNotSame(combatRound, clone.getCombatRound());
        assertEquals(position, clone.getPosition());
        assertNotSame(position, clone.getPosition());
        assertEquals(lastPosition, clone.getLastPosition());
        assertNotSame(lastPosition, clone.getLastPosition());
        assertEquals(missileSalvoHealthState.getInitialAmountByType(), clone.getMissileSalvoHealthState().getInitialAmountByType());
        assertEquals(missileSalvoHealthState.getCurrentAmountByType(), clone.getMissileSalvoHealthState().getCurrentAmountByType());
        assertEquals(missileSalvoHealthState.getLossesByType(), clone.getMissileSalvoHealthState().getLossesByType());
        assertNotSame(missileSalvoHealthState, clone.getMissileSalvoHealthState());
    }

    @Test
    void testIsInDetonationRangeByDistanceToTarget() {
        // prepare stuff
        ReflectionTestUtils.setField(testObject, "position", Orbit.getCenterOrbit());
        // mock methods
        final Cage cageMock = mock(Cage.class);
        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        final FleetRoundState frsMock = mock(FleetRoundState.class);
        when(cageMock.getCurrentStateByFleet(target)).thenReturn(frsMock);
        when(frsMock.getPosition()).thenReturn(Orbit.getCenterOrbit());
        // test method
        testObject.handleMovement();
        // check expectation
        final boolean result = testObject.isInDetonationRange();
        assertTrue(result);
    }

    @Test
    void testIsInDetonationRangeByWarheadRange() {
        // prepare stuff
        ReflectionTestUtils.setField(testObject, "position", Orbit.getCenterOrbit());
        // mock methods
        final Cage cageMock = mock(Cage.class);
        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        final FleetRoundState frsMock = mock(FleetRoundState.class);
        when(cageMock.getCurrentStateByFleet(target)).thenReturn(frsMock);
        when(frsMock.getPosition()).thenReturn(new Orbit(new Distance(50000, EDistanceMetric.M), new Distance(50000, EDistanceMetric.M)));
        // test method
        testObject.handleMovement();
        // check expectation
        final boolean result = testObject.isInDetonationRange();
        assertTrue(result);
    }

    @Test
    void testDetonate() {
        // prepare stuff
        final Cage cageMock = mock(Cage.class);
        final FleetRoundState fleetRoundStateMock = mock(FleetRoundState.class);
        final FleetHealthState targetHealthStateMock = mock(FleetHealthState.class);
        final MissileSalvoHealthState missileSalvoHealthStateMock = mock(MissileSalvoHealthState.class);

        final Map<Missile, Integer> currentMissiles = new HashMap<>();
        final Missile missile1 = missile(111);
        final Missile missile2 = missile(222);
        currentMissiles.put(missile1, 1);
        currentMissiles.put(missile2, 1);

        final WarShip warShip = warShip(target, shipClass(1), planet(1, 2));

        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        ReflectionTestUtils.setField(testObject, "missileSalvoHealthState", missileSalvoHealthStateMock);
        // mock methods
        when(cageMock.getCurrentStateByFleet(target)).thenReturn(fleetRoundStateMock);
        when(fleetRoundStateMock.getFleetHealthState()).thenReturn(targetHealthStateMock);
        when(missileSalvoHealthStateMock.getCurrentAmountByType()).thenReturn(currentMissiles);
        when(targetHealthStateMock.applyDamage(warShip, 111, testObject)).thenReturn(Optional.of(warShip));
        when(targetHealthStateMock.applyDamage(warShip, 222, testObject)).thenReturn(Optional.of(warShip));
        when(cageMock.getRandomActiveWarShipOfFleet(target)).thenReturn(warShip);
        // test method
        testObject.detonate();
        // check expectation
        verify(cageMock).getCurrentStateByFleet(target);
        verify(fleetRoundStateMock).getFleetHealthState();
        verify(missileSalvoHealthStateMock, times(2)).getCurrentAmountByType();
        verify(targetHealthStateMock).applyDamage(warShip, 111, testObject);
        verify(targetHealthStateMock).applyDamage(warShip, 222, testObject);
        verify(cageMock).addHistorizable(testObject);
        final Map<WarShip, List<Long>> result = testObject.getAppliedDamage();
        assertNotNull(result);
        assertEquals(1, result.size());
        final List<Long> resultingDamageList = result.get(warShip);
        assertNotNull(resultingDamageList);
        assertTrue(resultingDamageList.contains(111L));
        assertTrue(resultingDamageList.contains(222L));
        assertSame(ECombatPhase.ECombatSubPhase.MISSILE_FIRE_INCOMING_PHASE, testObject.getCombatSubPhase());
    }

    @Test
    void testHandleElokaPhase() {
        // prepare stuff
        final Cage cageMock = mock(Cage.class);
        final FleetRoundState fleetRoundStateMock = mock(FleetRoundState.class);
        final MissileSalvoHealthState missileSalvoHealthStateMock = mock(MissileSalvoHealthState.class);
        final Orbit positionMock = mock(Orbit.class);
        final Fleet targetMock = mock(Fleet.class);

        final Map<Missile, Integer> currentMissiles = new HashMap<>();
        final Missile missile1 = missile(111);
        final Missile missile2 = missile(222);
        currentMissiles.put(missile1, 1);
        currentMissiles.put(missile2, 1);

        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        ReflectionTestUtils.setField(testObject, "target", targetMock);
        ReflectionTestUtils.setField(testObject, "position", positionMock);
        ReflectionTestUtils.setField(testObject, "missileSalvoHealthState", missileSalvoHealthStateMock);

        final Distance distance = new Distance(1, EDistanceMetric.M);
        final Distance elokaRange = new Distance(2, EDistanceMetric.M);
        final int elokaEffectValue = 500;
        // mock methods
        when(cageMock.getCurrentStateByFleet(targetMock)).thenReturn(fleetRoundStateMock);
        when(missileSalvoHealthStateMock.getCurrentAmountByType()).thenReturn(currentMissiles);
        when(positionMock.getDistance(any())).thenReturn(distance);
        when(targetMock.getElokaRange()).thenReturn(elokaRange);
        when(targetMock.getElokaEffectValue()).thenReturn(elokaEffectValue);
        when(missileSalvoHealthStateMock.isActive()).thenReturn(false);
        // test method
        testObject.handleElokaPhase();
        // check expectation
        verify(cageMock).getCurrentStateByFleet(targetMock);
        verify(missileSalvoHealthStateMock, atLeast(1)).getCurrentAmountByType();
        verify(missileSalvoHealthStateMock, atLeast(1)).setNewMissileAmounts(any(), anyInt(), any());
        assertEquals(BigDecimal.ZERO, testObject.getRangePerCombatRound());
        assertEquals(BigDecimal.ZERO, testObject.getLongestOffensiveRange());
        verify(cageMock).addHistorizable(testObject);
        assertSame(ECombatPhase.ECombatSubPhase.ELOKA_PHASE, testObject.getCombatSubPhase());
    }

    @Test
    void testHandleCounterMissilePhase() {
        // prepare stuff
        final Cage cageMock = mock(Cage.class);
        final FleetRoundState targetsStateMock = mock(FleetRoundState.class);
        final MissileSalvoHealthState missileSalvoHealthStateMock = mock(MissileSalvoHealthState.class);
        final Orbit positionMock = mock(Orbit.class);
        final Fleet targetMock = mock(Fleet.class);
        final CounterMissileWeaponry counterMissileWeaponryMock = mock(CounterMissileWeaponry.class);

        final Map<Missile, Integer> currentMissiles = new HashMap<>();
        final Missile missile1 = missile(111);
        final Missile missile2 = missile(222);
        currentMissiles.put(missile1, 1);
        currentMissiles.put(missile2, 1);

        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        ReflectionTestUtils.setField(testObject, "target", targetMock);
        ReflectionTestUtils.setField(testObject, "position", positionMock);
        ReflectionTestUtils.setField(testObject, "missileSalvoHealthState", missileSalvoHealthStateMock);

        final Distance distance = new Distance(1, EDistanceMetric.M);
        final Distance counterMissileRange = new Distance(5, EDistanceMetric.M);
        // mock methods
        when(cageMock.getCurrentStateByFleet(targetMock)).thenReturn(targetsStateMock);
        when(targetMock.getCounterMissileWeaponry()).thenReturn(counterMissileWeaponryMock);
        when(counterMissileWeaponryMock.calculateDestroyedMissiles(any(), anyInt())).thenReturn(1);
        when(missileSalvoHealthStateMock.getCurrentAmountByType()).thenReturn(currentMissiles);
        when(positionMock.getDistance(any())).thenReturn(distance);
        when(targetsStateMock.getCounterMissileRange()).thenReturn(counterMissileRange);
        when(missileSalvoHealthStateMock.isActive()).thenReturn(false);
        // test method
        testObject.handleCounterMissilePhase();
        // check expectation
        verify(cageMock).getCurrentStateByFleet(targetMock);
        verify(missileSalvoHealthStateMock, atLeast(1)).getCurrentAmountByType();
        verify(missileSalvoHealthStateMock, atLeast(1)).setNewMissileAmounts(any(), anyInt(), any());
        assertEquals(Distance.ZERO, testObject.getRangePerCombatRound());
        assertEquals(Distance.ZERO, testObject.getLongestOffensiveRange());
        verify(cageMock).addHistorizable(testObject);
        assertSame(ECombatPhase.ECombatSubPhase.COUNTER_MISSILE_PHASE, testObject.getCombatSubPhase());
    }

    @Test
    void testHandleMovement() {
        // prepare stuff
        final Cage cageMock = mock(Cage.class);
        final FleetRoundState fleetRoundStateMock = mock(FleetRoundState.class);
        final Orbit targetsPositionMock = mock(Orbit.class);
        final Orbit positionMock = mock(Orbit.class);

        ReflectionTestUtils.setField(testObject, "cage", cageMock);
        ReflectionTestUtils.setField(testObject, "position", positionMock);
        ReflectionTestUtils.setField(testObject, "longestOffensiveRange", BigDecimal.ONE);

        final Distance distanceToTarget = new Distance(10, EDistanceMetric.M);
        final BigDecimal rangePerCombatRound = (BigDecimal) ReflectionTestUtils.getField(testObject, "rangePerCombatRound");
        assertNotNull(rangePerCombatRound);
        final Orbit newPosFake = Orbit.getCenterOrbit();
        // mock methods
        when(positionMock.getXCoordinate()).thenReturn(Distance.ZERO);
        when(positionMock.getYCoordinate()).thenReturn(Distance.ZERO);
        when(targetsPositionMock.getXCoordinate()).thenReturn(new Distance(5000, EDistanceMetric.M));
        when(targetsPositionMock.getYCoordinate()).thenReturn(new Distance(5000, EDistanceMetric.M));
        when(cageMock.getCurrentStateByFleet(target)).thenReturn(fleetRoundStateMock);
        when(fleetRoundStateMock.getPosition()).thenReturn(targetsPositionMock);
        when(targetsPositionMock.clone()).thenReturn(targetsPositionMock);
        when(positionMock.getDistance(targetsPositionMock)).thenReturn(distanceToTarget);
        when(positionMock.move(EMovementType.REDUCE_DISTANCE, distanceToTarget, targetsPositionMock)).thenReturn(newPosFake);
        when(positionMock.clone()).thenReturn(positionMock);
        // test method
        testObject.handleMovement();
        // check expectation
        verify(positionMock).move(EMovementType.REDUCE_DISTANCE, distanceToTarget, targetsPositionMock);
        verify(positionMock).clone();
        final Orbit resultLastPosition = (Orbit) ReflectionTestUtils.getField(testObject, "lastPosition");
        assertEquals(positionMock, resultLastPosition);
        verify(positionMock).moveTo(newPosFake);
        verify(cageMock).addHistorizable(testObject);
        assertSame(ECombatPhase.ECombatSubPhase.MISSILE_MOVEMENT_PHASE, testObject.getCombatSubPhase());
    }
}
