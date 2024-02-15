package de.yuga.spacebattle.backend.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.combat.maneuver.Maneuver;
import de.yuga.spacebattle.backend.combat.maneuver.ManeuverElement;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.devtools.XYSplineChart;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import org.jfree.chart.ui.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service // fixme make non-service
public class BattleLogger {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(BattleLogger.class);

    private final boolean logBattleResult;

    @Nullable
    private CombatRound combatRound;

    @Nonnull
    private final Map<Owner, Maneuver> curves = new HashMap<>();

    @Nullable
    private XYSplineChart demo;

    @Nonnull
    private final String server;

    @Nullable
    private Orbit planetaryOrbit;

    private boolean chartActive = false;

    @Autowired
    public BattleLogger(@Nonnull @Value("${logging.battle-log.write:true}") final String logBattleResult,
                        @Nonnull @Value("${sb.server:localhost}") final String server) {
        Preconditions.checkNotNull(logBattleResult, "logBattleResult shouldn't be null!");

        this.logBattleResult = Boolean.parseBoolean(logBattleResult);
        this.server = Preconditions.checkNotNull(server, "server must not be empty");
    }

    public void init(@Nonnull final CombatRound combatRound) {
        this.combatRound = Preconditions.checkNotNull(combatRound, "combatRound must not be empty");
    }

    public void closeRound() {
        this.combatRound = null;
    }

    public void setChartActive(final boolean chartActive) {
        this.chartActive = chartActive;
    }

    public void createChart(@Nonnull final Owner o1,
                            @Nonnull final Owner o2,
                            @Nonnull final Orbit planetaryOrbit) {
        Preconditions.checkNotNull(o1, "o1 must not be empty");
        Preconditions.checkNotNull(o2, "o2 must not be empty");
        this.planetaryOrbit = Preconditions.checkNotNull(planetaryOrbit, "planetaryOrbit must not be empty");

        final String title = o1.getUsername() + " vs. " + o2.getUsername();

        if (chartActive && isLocalhost()) {
            this.demo = new XYSplineChart(title);
        }
    }

    public void attachToChart(@Nonnull final Owner owner, @Nonnull final Maneuver maneuver) {
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
        Preconditions.checkNotNull(planetaryOrbit, "planetaryOrbit must not be empty");

        if (!chartActive || !isLocalhost()) {
            return;
        }

        curves.put(owner, maneuver);

        final String string = maneuver.getCourseItems().getManeuverElements().stream()
                .map(ManeuverElement::getCurve)
                .map(CubicBezier::toString)
                .collect(Collectors.joining(","));
        logMessage("CubicBezier for " + owner.getUsername() + ": " + string);

        if (demo != null && curves.size() == 2) {
            demo.run(planetaryOrbit, curves);
            demo.pack();
            UIUtils.centerFrameOnScreen(demo);
            demo.setVisible(true);
            runIndefinite();
        }
    }

    private boolean isLocalhost() {
        return this.server.equals("localhost");
    }

    private void runIndefinite() {
        //noinspection InfiniteLoopStatement,StatementWithEmptyBody
        while (true) {

        }
    }

    public void logMessage(@Nonnull final String msg) {
        Preconditions.checkNotNull(msg, "msg must not be empty");
        Preconditions.checkNotNull(combatRound, "combatRound must not be empty");

        if (isLocalhost() || logBattleResult) {
            LOGGER.info(combatRound + ": " + msg);
        }
    }

    public void logWarning(@Nonnull final String msg) {
        Preconditions.checkNotNull(msg, "msg must not be empty");
        Preconditions.checkNotNull(combatRound, "combatRound must not be empty");

        if (isLocalhost() || logBattleResult) {
            LOGGER.warn(combatRound + ": " + msg);
        }
    }

    public void logMessage(@Nonnull final String msg, @Nullable final Long start, @Nullable final Long end) {
        Preconditions.checkNotNull(msg, "msg must not be empty");
        Preconditions.checkNotNull(combatRound, "combatRound must not be empty");

        if (true) {
            // yes, but the logs are annoying
            return;
        }

        if (isLocalhost() || logBattleResult) {
            if (start != null && end != null) {
                final double duration = (double) (end - start) / 1000;
                LOGGER.info(combatRound + ": " + "\t" + msg + "\t\t - duration: " + duration + " seconds");
            } else {
                LOGGER.info(combatRound + ": " + "\t" + msg);
            }
        }
    }

    public void logMessagePlain(@Nonnull final String msg) {
        Preconditions.checkNotNull(msg, "msg must not be empty");

        if (isLocalhost() || logBattleResult) {
            LOGGER.info(msg);
        }
    }
}
