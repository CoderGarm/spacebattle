package de.yuga.spacebattle.backend.combat.dto;

import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.BeamState;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static de.yuga.spacebattle.TestDataProviderUtils.cage;
import static de.yuga.spacebattle.backend.combat.enums.EDamageResult.DAMAGE_APPLIED;
import static org.junit.jupiter.api.Assertions.*;

@Disabled("broken by out of date")
class BeamVolleyTest {

    Fleet actor;
    Fleet target;

    private BeamVolley testObject;
    private static final Distance DISTANCE = new Distance(10000, EDistanceMetric.M);

    @BeforeEach
    void setUp() {
        final Cage cage = cage();
        actor = cage.getParticipatingFleets().get(0);
        target = cage.getParticipatingFleets().get(1);
        testObject = new BeamVolley(cage, actor, target);
    }

    @Test
    void testApplyDamage() {
        // prepare stuff
        final WarShip attacker = actor.getAliveShips().stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(attacker);
        final WarShip defender = target.getAliveShips().stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(defender);
        final BeamState beamState = new BeamState(attacker, defender, 1, BigDecimal.ONE);
        ReflectionTestUtils.setField(testObject, "firedShots", List.of(beamState));
        ReflectionTestUtils.setField(testObject, "distance", BigDecimal.TEN);
        assertSame(ECombatPhase.ECombatSubPhase.BEAM_FIRE_PHASE, testObject.getCombatSubPhase());
        final RangeDefinition rangeDefinition = new RangeDefinition(DISTANCE.getCoordinate(), DISTANCE.getCoordinate(), EDistanceMetric.M);
        final List<DamagePerRangeAndAlignment> damagePerRangePerType = testObject.getActor().getDamagePerRangePerType(rangeDefinition, EWeaponType.BEAM);
        assertFalse(damagePerRangePerType.isEmpty());
        assertTrue(testObject.getAppliedDamage().isEmpty());
        // test method
        testObject.applyDamage();
        // check expectation
        assertSame(ECombatPhase.ECombatSubPhase.BEAM_FIRE_INCOMING_PHASE, testObject.getCombatSubPhase());
        assertSame(DAMAGE_APPLIED, testObject.getResult());
        assertFalse(testObject.getAppliedDamage().isEmpty());
    }

    @Test
    void testTestClone() {
        // prepare stuff
        final UUID uuid = testObject.getUuid();
        final CombatRound combatRound = testObject.getCombatRound();
        // test method
        final BeamVolley clone = testObject.clone();
        // check expectation
        assertEquals(uuid, clone.getUuid());
        assertNotSame(uuid, clone.getUuid());
        assertEquals(combatRound, clone.getCombatRound());
        assertNotSame(combatRound, clone.getCombatRound());
    }
}
