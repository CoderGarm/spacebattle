package de.yuga.spacebattle.misc;

public class CoordinateOverlapping {

    private final CoordinateElement first;
    private final CoordinateElement second;

    public CoordinateOverlapping(final CoordinateElement first, final CoordinateElement second) {
        this.first = first;
        this.second = second;
    }

    public CoordinateElement getFirst() {
        return first;
    }

    public CoordinateElement getSecond() {
        return second;
    }

    public int getDistance() {
        return first.getPosition().getDistance(second.getPosition());
    }

    @Override
    public String toString() {
        return "first " + first.getName() + ", " + first.getPosition().toString() + "\n"
                + "second " + second.getName() + ", " + second.getPosition().toString() + "\n"
                + "distance: " + first.getPosition().getDistance(second.getPosition()) + "\n\n";
    }
}
