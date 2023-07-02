package de.yuga.spacebattle.backend.services.turn.battle;

import de.yuga.spacebattle.SpringBootTestProfile;
import de.yuga.spacebattle.TestUtils;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTestProfile
@Disabled("not needed for unit or integration testing")
class BattleReportServiceTest {

    @Autowired
    private TickTimeService tickService;

    @Autowired
    private BattleReportService testObject;

    /**
     * report with tick greater or equals than 227 for user 1 needed
     */
    @Test
    void hasNewReportsSince() {
        final Tick tick = new Tick();
        TestUtils.setId(tick, 227);
        final boolean b = testObject.hasNewReportsSince(1, tick);
        assertTrue(b);
    }

    /**
     * report with tick lower than 'today' for user 1 needed
     */
    @Test
    void hasNoNewReportsSince() {
        final Tick tick = tickService.getToday();
        final boolean b = testObject.hasNewReportsSince(1, tick);
        assertFalse(b);
    }
}
