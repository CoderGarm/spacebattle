package de.yuga.spacebattle.backend.combat.dto;

import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static de.yuga.spacebattle.TestDataProviderUtils.cage;
import static org.junit.jupiter.api.Assertions.*;

class BeamVolleyTest {

    private BeamVolley testObject;
    private final static BigDecimal DISTANCE = BigDecimal.valueOf(10000);

    @BeforeEach
    void setUp() {
        final Cage cage = cage();
        final Fleet actor = cage.getParticipatingFleets().get(0);
        final Fleet target = cage.getParticipatingFleets().get(1);
        testObject = new BeamVolley(cage, actor, target);
    }

    @Test
    void testApplyDamage() {
        // prepare stuff
        ReflectionTestUtils.setField(testObject, "distance", BigDecimal.TEN);
        assertSame(ECombatPhase.ECombatSubPhase.BEAM_FIRE_PHASE, testObject.getCombatSubPhase());
        assertTrue(testObject.getActor().getDamagePerRangePerType(DISTANCE, DISTANCE, EWeaponType.BEAM) > 0);
        assertTrue(testObject.getAppliedDamage().isEmpty());
        // test method
        testObject.applyDamage();
        // check expectation
        assertSame(ECombatPhase.ECombatSubPhase.BEAM_FIRE_INCOMING_PHASE, testObject.getCombatSubPhase());
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
