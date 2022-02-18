package de.yuga.spacebattle.backend.calculator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class BattleCalculatorTest {

    @Test
    @Disabled("Just for calculation purposes.")
    void testCalculateElokaImpact() {

        int hitCounter = 0;
        int totalTries = 10000;
        for (int i = 0; i <= totalTries; i++) {
            final boolean elokaHits = BattleCalculator.calculateElokaImpact(10, 10);
            if (elokaHits) {
                hitCounter++;
            }
        }
        final double i = (double) hitCounter / totalTries * 100;
        System.out.println("total: " + totalTries + " hits: " + hitCounter + " quota: " + i);
    }
}
