package de.yuga.spacebattle.backend.services.spacecraft;

import de.yuga.spacebattle.SpringBootTestProfile;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTestProfile
@Disabled("not needed for unit or integration testing")
public class BattleServiceTest {

    @Autowired
    private TickService tickService;

    @Autowired
    private BattleService battleService;

    @Autowired
    private BattleReportService battleReportService;

    @Test
    public void testRunBattles() {
        Tick latest = tickService.getLatest();
        if (latest == null) {
            latest = tickService.doTick();
        }
        battleService.runBattles(latest);

        final List<BattleReport> all = battleReportService.findAll();
        assertNotNull(all, "Nice battle!");
    }
}
