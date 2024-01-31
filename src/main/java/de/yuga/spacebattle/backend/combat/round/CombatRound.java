package de.yuga.spacebattle.backend.combat.round;

import de.yuga.spacebattle.backend.dto.physics.Time;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;

public class CombatRound implements Cloneable, Comparable<CombatRound> {

    /**
     * The duration of a combat round in seconds.
     */
    public static final int COMBAT_ROUND_DURATION = 60;

    public static final Time COMBAT_ROUND = new Time(CombatRound.COMBAT_ROUND_DURATION, ETimeMetric.SECOND);

    public static final ETimeMetric COMBAT_ROUND_METRIC = ETimeMetric.MINUTE;

    /**
     * The battle round number.
     */
    private int no;

    public CombatRound() {
        this.no = 1;
    }

    /**
     * Creates the combat round with the given number.
     *
     * @param roundNumber the round number to set
     */
    public CombatRound(int roundNumber) {
        this.no = roundNumber;
    }

    public int getNo() {
        return no;
    }

    /**
     * Initiates the next combat round.
     */
    public void next() {
        this.no++;
    }

    public void previous() {
        this.no--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CombatRound)) return false;

        CombatRound that = (CombatRound) o;

        return no == that.no;
    }

    @Override
    public int hashCode() {
        return no;
    }

    @Override
    public CombatRound clone() {
        try {
            final CombatRound clone = (CombatRound) super.clone();
            clone.no = no;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public int compareTo(final CombatRound o) {
        return Integer.compare(no, o.no);
    }

    @Override
    public String toString() {
        return "# " + no;
    }
}
