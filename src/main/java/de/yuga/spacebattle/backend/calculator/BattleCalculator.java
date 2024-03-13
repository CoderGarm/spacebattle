package de.yuga.spacebattle.backend.calculator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.calculator.resource.CoursePlot;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class BattleCalculator {

    private static final MathContext MATH_CONTEXT = new MathContext(4, RoundingMode.DOWN);

    private BattleCalculator() {
    }

    /**
     * Calculates if the eloka effect is big enough to overcome the eloka resistance.<br>
     * But there is a little luck needed.
     *
     * @param elokaResistance  the resistance effect value
     * @param elokaEffectValue the applied effect value itself
     * @return <code>true</code> if the eloka was successful, <code>false</code> if the resistance was successful
     */
    public static boolean calculateElokaImpact(final int elokaResistance, final int elokaEffectValue) {

        final double chanceToEvade = elokaResistance / 100D;
        final double evade = ThreadLocalRandom.current().nextDouble(0, 1);
        boolean hasEvaded = evade <= chanceToEvade;

        final double chanceToHit = elokaEffectValue / 100D;
        final double hit = ThreadLocalRandom.current().nextDouble(0, 1);
        boolean hasHit = hit <= chanceToHit;
        return hasHit && !hasEvaded;
    }

    /**
     * Calculates if the effect of the anti missile system is big enough to overcome the maneuverability of the missile.<br>
     * But there is a little luck needed.
     *
     * @param maneuverabilityResistance the resistance effect value
     * @param maneuverability           the applied effect value itself
     * @return <code>true</code> if the eloka was successful, <code>false</code> if the resistance was successful
     */
    public static boolean calculateAntiMissileImpact(final int maneuverabilityResistance, final int maneuverability) {
        // maneuverabilityResistance / maneuverability = quotient
        // 15 / 500 = 0,03
        // 500 / 15 = 33,33
        final BigDecimal quotient = new BigDecimal(maneuverabilityResistance).divide(new BigDecimal(maneuverability), MATH_CONTEXT);
        if (quotient.compareTo(BigDecimal.ONE) > 0) {
            // if the eloka resistance is heavily overweight, it wins over the effect value
            return false;
        }
        final BigDecimal randomPercentage = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0, 100));
        //noinspection RedundantIfStatement
        if (randomPercentage.compareTo(quotient) < 0) {
            // if the fortune will support the resistance, it wins
            return false;
        }
        // the fortune is besides the electronic warfare
        return true;
    }

    /**
     * The combat value will be calculated the quality of the course's <b>Contact Phase</b>.<br>
     * <br>
     * A <b>Contact Phase</b> is defined by undercutting the outer range limit - not how much it will be undercut.<br>
     * <br>
     * The combat value of the course will be stated by the following measurement:<br>
     * <ol>
     *     <li>how long to achieve contact  - (shorter is better)</li>
     *     <li>how long is the contact phase  - (longer is better)</li>
     * </ol>
     *
     * @param agentsPlot      the planned agents plot
     * @param targetsPlot     the known or assumed targets plot
     * @param outerRangeLimit the range which must be undercut
     * @return the combat value
     */
    public static int calculateCombatValue(@Nonnull final CoursePlot agentsPlot,
                                           @Nonnull final CoursePlot targetsPlot,
                                           @Nonnull final Distance outerRangeLimit) {
        Preconditions.checkNotNull(agentsPlot, "agentsPlot must not be empty");
        Preconditions.checkNotNull(targetsPlot, "targetsPlot must not be empty");
        Preconditions.checkNotNull(outerRangeLimit, "outerRangeLimit must not be empty");

        final List<CourseOrderElement> notExecutedOrders = agentsPlot
                .getManeuver()
                .getCourseOrderElements().stream()
                .filter(coe -> !coe.isCourseOrderExecuted())
                .collect(Collectors.toList());

        final CombatRound currentCombatRound = agentsPlot.getCage().getCurrentCombatRound();

        int roundsInRange = 0;
        Integer firstInRangeFromNowOn = null;
        for (final CourseOrderElement coe : notExecutedOrders) {
            final CombatRound combatRound = coe.getCombatRound();
            final Orbit agentsPosition = coe.getPosition();

            final CourseOrderElement targetsCoe = targetsPlot.getCourseElement(combatRound);
            if (targetsCoe == null) {
                continue;
            }

            final Orbit targetsPosition = targetsCoe.getPosition();
            if (agentsPosition.getDistance(targetsPosition).compareTo(outerRangeLimit) <= 0) {
                roundsInRange++;
                if (firstInRangeFromNowOn == null) {
                    firstInRangeFromNowOn = combatRound.getNo() - currentCombatRound.getNo();
                }
            }
        }

        return roundsInRange == 0 ? 0 : ((roundsInRange * 10) - (firstInRangeFromNowOn / 3));
    }
}
