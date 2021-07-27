package de.yuga.spacebattle.backend.combat.round;

public class CombatRound implements Cloneable, Comparable<CombatRound> {

    /**
     * The duration of a combat round in seconds.
     */
    public static final int COMBAT_ROUND_DURATION = 60;

    /**
     * The battle round number.
     */
    private int no;

    public CombatRound() {
        this.no = 0;
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
