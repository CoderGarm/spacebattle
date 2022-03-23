package de.yuga.spacebattle.backend.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class BattleCalculator {

    private final static MathContext MATH_CONTEXT = new MathContext(4, RoundingMode.DOWN);

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
}
