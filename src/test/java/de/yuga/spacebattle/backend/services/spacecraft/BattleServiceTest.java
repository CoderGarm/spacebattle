package de.yuga.spacebattle.backend.services.spacecraft;

import de.yuga.spacebattle.SpringBootTestProfile;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.CombatRoundKey;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.MovementAction;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTestProfile
@Disabled("not needed for unit or integration testing")
public class BattleServiceTest {

    @Autowired
    private MasterOfTheUniverseService masterOfTheUniverseService;

    @Autowired
    private TickService tickService;

    @Autowired
    private BattleService battleService;

    @Autowired
    private BattleReportService battleReportService;

    private static void logMessage(String msg, final Long start, final Long end) {
        if (start != null && end != null) {
            final double duration = (double) (end - start) / 1000;
            System.out.println("\t" + msg + "\t\t - duration: " + duration + " seconds");
        } else {
            System.out.println("\t" + msg);
        }
    }

    @Test
    public void testRunBattles() {
        Tick latest = tickService.getToday();
        if (latest == null) {
            masterOfTheUniverseService.createInitialData();
            latest = tickService.getToday();
        }
        long start = System.currentTimeMillis();
        battleService.runBattles(latest);
        logMessage("calculating battle overall: ", start, System.currentTimeMillis());

        final List<BattleReport> all = battleReportService.findAll();
        assertNotNull(all, "Nice battle!");
    }

    @Test
    public void testDisplayBattle() {

        final List<BattleReport> all = battleReportService.findAll();
        final int i = 3;
        final List<MovementAction> a = all.get(all.size() - i)
                .getMovementActions()
                .stream()
                .filter(ma -> ma.getActor().getName().startsWith("A"))
                .sorted(Comparator.comparing(CombatRoundKey::getCombatRound))
                .collect(Collectors.toList());

        final List<MovementAction> b = all.get(all.size() - i)
                .getMovementActions()
                .stream()
                .filter(ma -> ma.getActor().getName().startsWith("111"))
                .sorted(Comparator.comparing(CombatRoundKey::getCombatRound))
                .collect(Collectors.toList());
        // todo why the later movement switches the positions after 'Out of ammo'? -> because the position of the state is the pos of the last round -> no movement executed?
        /*for (MovementAction m: a) {
            final Distance distance = m.getDestination().getDistance(m.getInterimDestination());
            final BigDecimal c = distance.getCoordinateInMetric(EDistanceMetric.LS);
            final BigInteger bigInteger = c.toBigInteger();
            final int count = bigInteger.intValue() / 20;
            System.out.println("_".repeat(count));
        }*/

        /*for (int i = 1; i < a.size(); i++) {
            final MovementAction last = a.get(i - 1);
            final MovementAction current = a.get(i);
            final BigDecimal distance = current.getInterimDestination().getDistance(last.getInterimDestination()).getCoordinateInMetric(EDistanceMetric.LS);
            System.out.println(distance);
        }*/

        final List<MovementAction> mA = a.subList(a.size() - 10, a.size());
        final List<MovementAction> mB = b.subList(b.size() - 10, b.size());

        assertNotNull(all, "Nice battle!");
    }
}
